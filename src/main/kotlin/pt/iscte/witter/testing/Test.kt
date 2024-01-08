package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.strudel.vm.NULL
import pt.iscte.witter.tsl.*
import pt.iscte.witter.tsl.IExpression
import pt.iscte.witter.tsl.IStatement
import java.io.File

class Test(private val referenceFile: File) {
    constructor(referenceFilePath: String) : this(File(referenceFilePath))

    private val loader = Java2Strudel()

    fun apply(subjectFile: File): List<ITestResult> = apply(loader.load(subjectFile))

    fun apply(subjectPath: String): List<ITestResult> = apply(loader.load(File(subjectPath)))

    fun apply(subjectPath: String, suite: TestSuite): List<ITestResult> =
        apply(loader.load(File(subjectPath)), suite.modules())

    fun apply(subjectFile: File, suite: TestSuite): List<ITestResult> = apply(loader.load(subjectFile), suite.modules())

    @Suppress("UNCHECKED_CAST")
    private fun apply(subject: IModule, tests: List<TestModule> = listOf()): List<ITestResult> {
        val reference: IModule = loader.load(referenceFile)

        val results = mutableListOf<ITestResult>()
        val memory = mutableMapOf<String, IValue>()

        // Evaluates a TSL expression.
        fun evaluate(vm: IVirtualMachine, listener: EvaluationMetricListener, expr: IExpression): IValue = when (expr) {
            is VariableReference -> vm.getValue(memory[expr.id]?.value)
            is Literal -> vm.getValue(expr.value)
            is ProcedureCall -> {
                val (unmodified, args, argsCopy) =
                    if (expr.parsed) // Arguments are passed as Any
                        getArgumentsFromValues(vm, (expr.arguments as List<Any>).map {
                            if (it is IExpression) evaluate(vm, listener, it) else it
                        })
                    else // Arguments are passed as a single Java source code string
                        getArgumentsFromString(vm, expr.arguments.toString())

                // Try to find the subject procedure, or log a ProcedureNotImplemented result if not found
                val subjectProcedure = runCatching { subject.getProcedure(expr.procedure.id!!) }.onFailure {
                    if (results.none { it is ProcedureNotImplemented && it.procedure == expr.procedure })
                        results.add(ProcedureNotImplemented(expr.procedure))
                }.getOrNull()

                if (subjectProcedure == null) NULL // Target procedure not implemented
                else {
                    val builder = ResultBuilder(expr.procedure, subjectProcedure)

                    // BLACK-BOX
                    val expected = vm.execute(expr.procedure, *args.toTypedArray())?.value
                    val actualIValue = vm.execute(subjectProcedure, *argsCopy.toTypedArray())
                    val actual = actualIValue?.value
                    builder.black(expected, actual, unmodified)?.let { results.add(it) }

                    // WHITE-BOX
                    results.addAll(builder.white(listener, unmodified))

                    actualIValue ?: NULL
                }
            }
            is ObjectCreation -> {
                TODO("Interpreting object creation expressions not yet supported! :(")
            }
        }

        // Processes a TSL instruction (e.g. variable assignments).
        fun process(vm: IVirtualMachine, listener: EvaluationMetricListener, instruction: Instruction): IValue =
            when (instruction) {
                is VariableAssignment -> {
                    val value = evaluate(vm, listener, instruction.initializer)
                    memory[instruction.id] = value
                    value
                }
            }

        // Executes instruction and expression evaluation statements.
        fun execute(vm: IVirtualMachine, listener: EvaluationMetricListener, stmt: IStatement): IValue =
            when (stmt) {
                is Instruction -> process(vm, listener, stmt)
                is IExpression -> evaluate(vm, listener, stmt)
            }

        // Run all tests
        (tests.ifEmpty { reference.tests }).forEach { test ->
            if (test.stateful) {
                val vm = IVirtualMachine.create() // Stateful tests - one VM for all calls in sequence
                val listener = EvaluationMetricListener(vm, test)
                vm.addListener(listener)
                test.statements().forEach { execute(vm, listener, it) }
            } else {
                test.statements().forEach {
                    val vm = IVirtualMachine.create() // Stateless tests - one VM for each call in sequence
                    val listener = EvaluationMetricListener(vm, test)
                    vm.addListener(listener)
                    execute(vm, listener, it)
                }
            }
        }

        return results
    }
}