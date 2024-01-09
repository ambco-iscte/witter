package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
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

        // Initialise empty memory
        val memory = mutableMapOf<IModule, MutableMap<String, IValue>>()
        memory[reference] = mutableMapOf()
        memory[subject] = mutableMapOf()

        // Evaluates a TSL expression.
        fun evaluate(vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener, expr: IExpression): IValue {

            fun Collection<Any>.dereference(): List<IValue> =
                map { when(it) {
                    is IExpression -> evaluate(vm, module, listener, it)
                    else -> getValue(vm, it)
                } }

            // Allocates a record
            fun allocate(expr: ObjectCreation): IReference<IRecord> {
                val type: IRecordType = module.getRecordType(expr.className)
                val ref: IReference<IRecord> = vm.allocateRecord(type)

                val constructor: IProcedure = module.getProcedure("\$init")
                val args = (listOf(ref) + expr.constructorArguments).dereference()
                vm.execute(constructor, *args.toTypedArray())

                // Add $this argument to member function calls
                val configure = expr.configure().map {
                    ProcedureCall(
                        module.findMatchingProcedure(it.procedure)!!,
                        (listOf(ref) + it.arguments as List<Any>).dereference(),
                        true
                    )
                }
                configure.forEach { call ->
                    val arguments = (call.arguments as List<Any>).dereference()
                    vm.execute(module.findMatchingProcedure(call.procedure)!!, *arguments.toTypedArray())
                }

                return ref
            }

            fun ProcedureCall.arguments(): List<IValue> =
                if (parsed) // Arguments are passed as Any
                    (arguments as List<Any>).dereference()
                else // Arguments are passed as a single Java source code string
                    TestSpecifier.parseArgumentsString(vm, arguments.toString())

            return when (expr) {
                is VariableReference -> memory[module]!![expr.id] ?: NULL
                is Literal -> vm.getValue(expr.value)
                is ProcedureCall -> {
                    vm.execute(module.findMatchingProcedure(expr.procedure)!!, *expr.arguments().toTypedArray()) ?: NULL
                    /*
                    val (originalArguments, referenceArguments, subjectArguments) = expr.arguments()

                    // Try to find the subject procedure, or log a ProcedureNotImplemented result if not found
                    val subjectProcedure = runCatching { subject.getProcedure(expr.procedure.id!!) }.onFailure {
                        if (results.none { it is ProcedureNotImplemented && it.procedure == expr.procedure })
                            results.add(ProcedureNotImplemented(expr.procedure))
                    }.getOrNull()

                    if (subjectProcedure == null) NULL // Target procedure not implemented
                    else {
                        val builder = ResultBuilder(expr.procedure, subjectProcedure)

                        // BLACK-BOX
                        val expectedValue = vm.execute(expr.procedure, *referenceArguments.toTypedArray())
                        val actual = vm.execute(subjectProcedure, *subjectArguments.toTypedArray())?.value
                        builder.black(expectedValue?.value, actual, originalArguments)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, originalArguments))

                        expectedValue ?: NULL
                    }
                     */
                }
                is ObjectCreation -> allocate(expr)
            }
        }

        // Processes a TSL instruction (e.g. variable assignments).
        fun process(vm: IVirtualMachine, listener: EvaluationMetricListener, instruction: Instruction): IValue =
            when (instruction) {
                is VariableAssignment -> {
                    val value = evaluate(vm, reference, listener, instruction.initializer)
                    memory[reference]!![instruction.id] = value
                    memory[subject]!![instruction.id] = evaluate(vm, subject, listener, instruction.initializer)
                    value
                }
            }

        // Executes instruction and expression evaluation statements.
        fun execute(vm: IVirtualMachine, listener: EvaluationMetricListener, stmt: IStatement) {
            when (stmt) {
                is Instruction -> process(vm, listener, stmt)
                is IExpression -> when(stmt) {
                    is ProcedureCall -> {
                        val referenceProcedure = reference.findMatchingProcedure(stmt.procedure)!!
                        val subjectProcedure = subject.findMatchingProcedure(stmt.procedure)

                        if (subjectProcedure == null) {
                            if (results.none { it is ProcedureNotImplemented && it.procedure == stmt.procedure })
                                results.add(ProcedureNotImplemented(stmt.procedure))
                            return
                        }

                        val builder = ResultBuilder(referenceProcedure, subjectProcedure)

                        val args =
                            if (stmt.parsed) (stmt.arguments as List<Any>).map { when(it) {
                                is IExpression -> evaluate(vm, reference, listener, it)
                                else -> getValue(vm, it)
                            } }
                            else TestSpecifier.parseArgumentsString(vm, stmt.arguments.toString())

                        // BLACK-BOX
                        val expected = evaluate(vm, reference, listener, stmt)
                        val actual = evaluate(vm, subject, listener, stmt)
                        builder.black(expected, actual, args)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, args)) // TODO no previousArgumentsForProcedure??
                    }
                    else -> {
                        evaluate(vm, reference, listener, stmt)
                        evaluate(vm, subject, listener, stmt)
                    }
                }
            }
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