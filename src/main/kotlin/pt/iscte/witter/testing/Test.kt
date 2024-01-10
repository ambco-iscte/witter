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

    private val memory = mutableMapOf<IModule, MutableMap<String, IValue>>()

    fun apply(subjectFile: File): List<ITestResult> = apply(loader.load(subjectFile))

    fun apply(subjectPath: String): List<ITestResult> = apply(loader.load(File(subjectPath)))

    fun apply(subjectPath: String, suite: TestSuite): List<ITestResult> =
        apply(loader.load(File(subjectPath)), suite.modules())

    fun apply(subjectFile: File, suite: TestSuite): List<ITestResult> = apply(loader.load(subjectFile), suite.modules())

    fun dereference(collection: Collection<Any>, vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): List<IValue> =
        collection.map { when(it) {
            is IExpression -> it.evaluate(vm, module, listener)
            else -> getValue(vm, it)
        } }

    // Allocates a record
    @Suppress("UNCHECKED_CAST")
    private fun IModule.allocate(
        vm: IVirtualMachine,
        listener: EvaluationMetricListener,
        expr: ObjectCreation
    ): IReference<IRecord> {
        val type: IRecordType = getRecordType(expr.className)
        val ref: IReference<IRecord> = vm.allocateRecord(type)

        val constructor: IProcedure = getProcedure("\$init")
        val args = dereference(listOf(ref) + expr.constructorArguments, vm, this, listener)
        vm.execute(constructor, *args.toTypedArray())

        // Add $this argument to member function calls
        val configure = expr.configure().map {
            ProcedureCall(
                findMatchingProcedure(it.procedure)!!,
                dereference(listOf(ref) + it.arguments as List<Any>, vm, this, listener),
                true
            )
        }
        configure.forEach { call ->
            val arguments = dereference(call.arguments as List<Any>, vm, this, listener)
            vm.execute(findMatchingProcedure(call.procedure)!!, *arguments.toTypedArray())
        }

        return ref
    }

    // Evaluates a TSL expression.
    @Suppress("UNCHECKED_CAST")
    private fun IExpression.evaluate(vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): IValue {
        fun ProcedureCall.arguments(): List<IValue> =
            if (parsed) // Arguments are passed as Any
                dereference(arguments as List<Any>, vm, module, listener)
            else // Arguments are passed as a single Java source code string
                TestSpecifier.parseArgumentsString(this@Test, vm, module, listener, arguments.toString())

        return when (this) {
            is VariableReference -> memory[module]!![id] ?: NULL
            is Literal -> vm.getValue(this.value)
            is ProcedureCall -> {
                val args = arguments().toTypedArray()
                val procedure = module.findMatchingProcedure(procedure) ?: return NULL
                vm.execute(procedure, *args) ?: NULL
            }
            is ObjectCreation -> module.allocate(vm, listener, this)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun apply(subject: IModule, tests: List<TestModule> = listOf()): List<ITestResult> {
        val reference: IModule = loader.load(referenceFile)

        val results = mutableListOf<ITestResult>()

        // Initialise empty memory
        memory.clear()
        memory[reference] = mutableMapOf()
        memory[subject] = mutableMapOf()

        fun process(vm: IVirtualMachine, listener: EvaluationMetricListener, instruction: Instruction): IValue =
            when (instruction) {
                is VariableAssignment -> {
                    val value = instruction.initializer().evaluate(vm, reference, listener)
                    memory[reference]!![instruction.id] = value
                    memory[subject]!![instruction.id] = instruction.initializer().evaluate(vm, subject, listener)
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
                                is IExpression -> it.evaluate(vm, reference, listener)
                                else -> getValue(vm, it)
                            } }
                            else TestSpecifier.parseArgumentsString(
                                this, vm, reference, listener, stmt.arguments.toString()
                            )

                        // BLACK-BOX
                        val expected = stmt.evaluate(vm, reference, listener)
                        val actual = stmt.evaluate(vm, subject, listener)
                        builder.black(expected, actual, args)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, args))
                    }
                    else -> {
                        stmt.evaluate(vm, reference, listener)
                        stmt.evaluate(vm, subject, listener)
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