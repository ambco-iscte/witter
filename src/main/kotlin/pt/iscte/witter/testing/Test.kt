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

        val results = mutableSetOf<ITestResult>()

        // Initialise empty memory
        fun MutableMap<IModule, MutableMap<String, IValue>>.init() {
            clear()
            put(reference, mutableMapOf())
            put(subject, mutableMapOf())
        }
        memory.init()

        fun run(case: TestCaseStatement) {
            fun execute(vm: IVirtualMachine, listener: EvaluationMetricListener, stmt: IStatement) {
                fun dereference(
                    collection: Collection<Any>,
                    vm: IVirtualMachine,
                    module: IModule,
                    listener: EvaluationMetricListener,
                    evaluator: IExpressionStatement.(IVirtualMachine, IModule, EvaluationMetricListener) -> Result<IValue>
                ): List<IValue> =
                    collection.map { when(it) {
                        is IExpressionStatement -> it.evaluator(vm, module, listener).getOrThrow()
                        else -> getValue(vm, it)
                    } }

                // Allocates a record
                @Suppress("UNCHECKED_CAST")
                fun IModule.allocate(
                    vm: IVirtualMachine,
                    listener: EvaluationMetricListener,
                    expr: ObjectCreation,
                    evaluator: IExpressionStatement.(IVirtualMachine, IModule, EvaluationMetricListener) -> Result<IValue>
                ): IReference<IRecord> {
                    val type: IRecordType = getRecordType(expr.qualifiedName)
                    val ref: IReference<IRecord> = vm.allocateRecord(type)

                    val args = dereference(listOf(ref) + expr.constructorArguments, vm, this, listener, evaluator)

                    val referenceConstructor = reference.findAcceptingProcedure("\$init", args)

                    if (referenceConstructor == null) {
                        println("\tCouldn't find reference constructor \$init matching arguments ${args.joinToString()}")
                        throw NoSuchMethodException("Couldn't find reference constructor \$init matching arguments ${args.joinToString()}")
                    }

                    val constructor: IProcedure? = findMatchingProcedure(referenceConstructor)

                    if (constructor == null) {
                        results.add(ProcedureNotImplemented(referenceConstructor))
                        throw NoSuchMethodException("No procedure found matching ${referenceConstructor.descriptor}")
                    }

                    kotlin.runCatching { vm.execute(constructor, *args.toTypedArray()) }.onFailure {
                        results.add(ExceptionTestResult(constructor, args, null, it))
                        throw RuntimeException(it)
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
                fun IExpressionStatement.evaluate(vm: IVirtualMachine, module: IModule, listener: EvaluationMetricListener): Result<IValue> =
                    runCatching {
                        when (this) {
                            is VariableReference -> (memory[module] ?: throw RuntimeException("Test has no memory for module ${module.id}"))[id] ?: NULL
                            is ProcedureCall -> {
                                val args = dereference(arguments as List<Any>, vm, module, listener, IExpressionStatement::evaluate).toTypedArray()
                                module.findMatchingProcedure(procedure)?.let { vm.execute(it, *args) } ?: NULL
                            }
                            is ObjectCreation -> module.allocate(vm, listener, this, IExpressionStatement::evaluate)
                        }
                    }

                runCatching {
                    when (stmt) {
                        is TestCaseStatement -> run(stmt)
                        is VariableAssignment -> {
                            (memory[reference] ?: throw RuntimeException("Test has no memory for module ${reference.id}"))[stmt.id] = stmt.initializer().evaluate(vm, reference, listener).getOrThrow()
                            (memory[subject] ?: throw RuntimeException("Test has no memory for module ${subject.id}"))[stmt.id] = stmt.initializer().evaluate(vm, subject, listener).getOrThrow()
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
                                val args: Result<List<IValue>> = runCatching { (stmt.arguments as List<Any>).map {
                                    when(it) {
                                    is IExpressionStatement -> it.evaluate(vm, reference, listener).getOrThrow()
                                    else -> getValue(vm, it)
                                } } }

                                //if (args.isFailure)
                                    //println("\tFailed to get arguments for procedure call $stmt: ${args.exceptionOrNull()}")

                                // BLACK-BOX
                                val expected: Result<IValue> = stmt.evaluate(vm, reference, listener)
                                if (expected.isSuccess) {
                                    if (stmt.expected != null && !expected.getOrThrow().sameAs(vm.getValue(stmt.expected)))
                                        throw AssertionError("Reference solution return value does not match " +
                                                "user-specified expected return value for procedure call: $stmt")
                                } else if (stmt.expected != null && expected.getOrThrow() != stmt.expected)
                                    throw AssertionError("Reference solution return value does not match " +
                                            "user-specified expected return value for procedure call: $stmt")

                                val actual = stmt.evaluate(vm, subject, listener)

                                if (args.isSuccess && expected.isSuccess && actual.isSuccess) {
                                    builder.black(expected.getOrThrow(), actual.getOrThrow(), args.getOrThrow())?.let { results.add(it) }
                                }
                                else if (args.isSuccess && actual.exceptionOrNull() !is RuntimeError) {
                                    val ex = ExceptionTestResult(
                                        referenceProcedure,
                                        args.getOrThrow(),
                                        expected.exceptionOrNull(),
                                        actual.exceptionOrNull()
                                    )
                                    results.add(ex)
                                    if (!ex.passed)
                                        return
                                }

                                // WHITE-BOX
                                if (args.isSuccess)
                                    results.addAll(builder.white(listener, args.getOrThrow()))

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

            val vm = IVirtualMachine.create(loopIterationMaximum = 10000) // Stateful tests - one VM for all calls in sequence
            val listener = EvaluationMetricListener(vm, case)
            vm.addListener(listener)
            case.statements().forEach { execute(vm, listener, it) }
        }

        // Run all tests
        (tests.ifEmpty { reference.tests }).forEach {
            memory.init()
            run(it)
        }

        return results.toList()
    }
}