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

    fun apply(subjectFile: File): List<ITestResult> = apply(subjectFile, listOf())

    fun apply(subjectPath: String): List<ITestResult> = apply(File(subjectPath))

    fun apply(subjectPath: String, suite: TestSuite): List<ITestResult> =
        apply(File(subjectPath), suite.cases())

    fun apply(subjectFile: File, suite: TestSuite): List<ITestResult> = apply(subjectFile, suite.cases())

    private fun apply(subjectFile: File, tests: List<TestCaseStatement> = listOf()): List<ITestResult> {
        val module = runCatching { loader.load(subjectFile) }.onFailure {
            return listOf(FileLoadingError(subjectFile, it))
        }.getOrThrow()
        return apply(module, tests)
    }

    @Suppress("UNCHECKED_CAST")
    private fun apply(subject: IModule, tests: List<TestCaseStatement> = listOf()): List<ITestResult> {
        val reference: IModule = loader.load(referenceFile)

        val results = mutableListOf<ITestResult>()

        // Initialise empty memory
        memory.clear()
        memory[reference] = mutableMapOf()
        memory[subject] = mutableMapOf()

        fun run(case: TestCaseStatement) {
            fun execute(vm: IVirtualMachine, listener: EvaluationMetricListener, stmt: IStatement) {
                fun dereference(
                    collection: Collection<Any>,
                    vm: IVirtualMachine,
                    module: IModule,
                    listener: EvaluationMetricListener,
                    evaluator: IExpressionStatement.(IVirtualMachine, IModule, EvaluationMetricListener) -> IValue
                ): List<IValue> =
                    collection.map { when(it) {
                        is IExpressionStatement -> it.evaluator(vm, module, listener)
                        else -> getValue(vm, it)
                    } }

                // Allocates a record
                @Suppress("UNCHECKED_CAST")
                fun IModule.allocate(
                    vm: IVirtualMachine,
                    listener: EvaluationMetricListener,
                    expr: ObjectCreation,
                    evaluator: IExpressionStatement.(IVirtualMachine, IModule, EvaluationMetricListener) -> IValue
                ): IReference<IRecord> {
                    val type: IRecordType = getRecordType(expr.className)
                    val ref: IReference<IRecord> = vm.allocateRecord(type)

                    val constructor: IProcedure = getProcedure("\$init")
                    val args = dereference(listOf(ref) + expr.constructorArguments, vm, this, listener, evaluator)

                    try {
                        vm.execute(constructor, *args.toTypedArray())
                    } catch (e: Exception) {
                        results.add(ExceptionTestResult(constructor, args, null, e))
                        throw e
                    }

                    // Add $this argument to member function calls
                    val configure = expr.configure().map {
                        val procedure = findMatchingProcedure(it.procedure)
                        if (procedure == null) {
                            results.add(ProcedureNotImplemented(it.procedure))
                            throw NoSuchMethodException("No procedure found matching ${it.procedure.signature}")
                        }

                        ProcedureCall(
                            procedure,
                            dereference(listOf(ref) + it.arguments as List<Any>, vm, this, listener, evaluator),
                            it.metrics
                        )
                    }
                    configure.forEach { call ->
                        val arguments = dereference(call.arguments as List<Any>, vm, this, listener, evaluator)

                        val procedure = findMatchingProcedure(call.procedure)
                        if (procedure == null) {
                            results.add(ProcedureNotImplemented(call.procedure))
                            throw NoSuchMethodException("No procedure found matching ${call.procedure.signature}")
                        }

                        try {
                            vm.execute(procedure, *arguments.toTypedArray())
                        } catch (e: Exception) {
                            results.add(ExceptionTestResult(procedure, arguments, null, e))
                            throw e
                        }
                    }

                    return ref
                }

                // Evaluates a TSL expression.
                @Suppress("UNCHECKED_CAST")
                fun IExpressionStatement.evaluate(vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): IValue {

                    fun ProcedureCall.arguments(): List<IValue> =
                        dereference(arguments as List<Any>, vm, module, listener, IExpressionStatement::evaluate)

                    return when (this) {
                        is VariableReference -> memory[module]!![id] ?: NULL
                        is ProcedureCall -> {
                            val args = arguments().toTypedArray()
                            val procedure = module.findMatchingProcedure(procedure) ?: return NULL
                            vm.execute(procedure, *args) ?: NULL
                        }
                        is ObjectCreation -> module.allocate(vm, listener, this, IExpressionStatement::evaluate)
                    }
                }

                runCatching {
                    when (stmt) {
                        is TestCaseStatement -> run(stmt)
                        is VariableAssignment -> {
                            val value = stmt.initializer().evaluate(vm, reference, listener)
                            memory[reference]!![stmt.id] = value
                            memory[subject]!![stmt.id] = stmt.initializer().evaluate(vm, subject, listener)
                        }
                        is IExpressionStatement -> when(stmt) {
                            is ProcedureCall -> {
                                listener.extend(case.metrics + stmt.metrics)

                                val referenceProcedure = reference.findMatchingProcedure(stmt.procedure) ?:
                                throw AssertionError("Reference solution does not implement procedure matching ${stmt.procedure.signature}")

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
                                val expected: Result<IValue> = runCatching { stmt.evaluate(vm, reference, listener) }
                                if (expected.isSuccess) {
                                    if (stmt.expected != null && !expected.getOrThrow().sameAs(vm.getValue(stmt.expected)))
                                        throw AssertionError("Reference solution return value does not match " +
                                                "user-specified expected return value for procedure call: $stmt")
                                } else if (stmt.expected != null && expected.getOrThrow() != stmt.expected)
                                    throw AssertionError("Reference solution return value does not match " +
                                            "user-specified expected return value for procedure call: $stmt")

                                val actual = runCatching { stmt.evaluate(vm, subject, listener) }

                                if (expected.isSuccess && actual.isSuccess) {
                                    builder.black(expected.getOrThrow(), actual.getOrThrow(), args)?.let { results.add(it) }
                                }
                                else {
                                    val ex = ExceptionTestResult(
                                        referenceProcedure,
                                        args,
                                        expected.exceptionOrNull(),
                                        actual.exceptionOrNull()
                                    )
                                    results.add(ex)
                                    if (!ex.passed)
                                        return
                                }

                                // WHITE-BOX
                                results.addAll(builder.white(listener, args))

                                // Reset listener so metrics aren't cumulative between procedure calls
                                listener.reset()
                                listener.rebase()
                            }
                            else -> {
                                stmt.evaluate(vm, reference, listener)
                                stmt.evaluate(vm, subject, listener)
                            }
                        }
                    }
                }
            }

            val vm = IVirtualMachine.create() // Stateful tests - one VM for all calls in sequence
            val listener = EvaluationMetricListener(vm, case)
            vm.addListener(listener)
            case.statements().forEach { execute(vm, listener, it) }
        }

        // Run all tests
        (tests.ifEmpty { reference.tests }).forEach { run(it) }

        return results
    }
}