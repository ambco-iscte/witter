package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.*
import pt.iscte.witter.tsl.IExpressionStatement
import pt.iscte.witter.tsl.IStatement
import java.io.File

class Test(private val referenceFile: File) {
    constructor(referenceFilePath: String) : this(File(referenceFilePath))

    private val loader = Java2Strudel()

    private val memory = mutableMapOf<IModule, MutableMap<String, IValue>>()

    fun apply(subjectFile: File): List<ITestResult> = apply(loader.load(subjectFile))

    fun apply(subjectPath: String): List<ITestResult> = apply(loader.load(File(subjectPath)))

    fun apply(subjectPath: String, suite: TestSuite): List<ITestResult> =
        apply(loader.load(File(subjectPath)), suite.cases())

    fun apply(subjectFile: File, suite: TestSuite): List<ITestResult> = apply(loader.load(subjectFile), suite.cases())

    fun dereference(collection: Collection<Any>, vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): List<IValue> =
        collection.map { when(it) {
            is IExpressionStatement -> it.evaluate(vm, module, listener)
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
                it.metrics
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
    private fun IExpressionStatement.evaluate(vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): IValue {

        fun ProcedureCall.arguments(): List<IValue> = dereference(arguments as List<Any>, vm, module, listener)

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
    private fun apply(subject: IModule, tests: List<TestCase> = listOf()): List<ITestResult> {
        val reference: IModule = loader.load(referenceFile)

        val results = mutableListOf<ITestResult>()

        // Initialise empty memory
        memory.clear()
        memory[reference] = mutableMapOf()
        memory[subject] = mutableMapOf()

        // Executes instruction and expression evaluation statements.
        fun execute(vm: IVirtualMachine, listener: EvaluationMetricListener, stmt: IStatement) {
            when (stmt) {
                is VariableAssignment -> {
                    val value = stmt.initializer().evaluate(vm, reference, listener)
                    memory[reference]!![stmt.id] = value
                    memory[subject]!![stmt.id] = stmt.initializer().evaluate(vm, subject, listener)
                }
                is IExpressionStatement -> when(stmt) {
                    is ProcedureCall -> {
                        val referenceProcedure = reference.findMatchingProcedure(stmt.procedure)!!
                        val subjectProcedure = subject.findMatchingProcedure(stmt.procedure)

                        if (subjectProcedure == null) {
                            if (results.none { it is ProcedureNotImplemented && it.procedure == stmt.procedure })
                                results.add(ProcedureNotImplemented(stmt.procedure))
                            return
                        }

                        val builder = ResultBuilder(referenceProcedure, subjectProcedure)

                        // TODO: Bug
                        //  Reference to record in arguments is always the same, so every call will show the
                        //  final, modified record, even for calls that were made before the object reached
                        //  its final state. :/
                        val args = (stmt.arguments as List<Any>).map { when(it) {
                            is IExpressionStatement -> it.evaluate(vm, reference, listener)
                            else -> getValue(vm, it)
                        } }

                        // BLACK-BOX
                        val expected = stmt.evaluate(vm, reference, listener)
                        val actual = stmt.evaluate(vm, subject, listener)
                        builder.black(expected, actual, args)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, args))

                        // Reset listener so metrics aren't cumulative between procedure calls
                        listener.reset()
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
            val vm = IVirtualMachine.create() // Stateful tests - one VM for all calls in sequence
            val listener = EvaluationMetricListener(vm, test)
            vm.addListener(listener)
            test.statements().forEach { execute(vm, listener, it) }
        }

        return results
    }
}