package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.*
import pt.iscte.witter.tsl.IExpressionStatement
import pt.iscte.witter.tsl.IStatement
import java.io.File

const val LOOP_ITERATION_LIMIT = 1000

class Test(private val referenceFile: File) {
    constructor(referenceFilePath: String) : this(File(referenceFilePath))

    var currentSubject: File? = null

    private val loader = Java2Strudel(preprocessing = {
        removePackageDeclaration()
        removeMainMethod()
    })

    private val memory = mutableMapOf<IModule, MutableMap<VariableAssignment, IValue>>()

    private val machines = mutableMapOf<TestCaseStatement, Pair<IVirtualMachine, EvaluationMetricListener>>()

    fun apply(subjectFile: File): List<ITestResult> = apply(subjectFile, listOf())

    fun apply(subjectPath: String): List<ITestResult> = apply(File(subjectPath))

    fun apply(subjectPath: String, suite: TestSuite): List<ITestResult> =
        apply(File(subjectPath), suite.cases())

    fun apply(subjectFile: File, suite: TestSuite): List<ITestResult> = apply(subjectFile, suite.cases())

    private fun apply(subjectFile: File, tests: List<TestCaseStatement> = listOf()): List<ITestResult> {
        val module = runCatching { loader.load(subjectFile) }.onFailure {
            return listOf(FileLoadingError(subjectFile, it))
        }.getOrThrow()
        currentSubject = subjectFile
        return apply(module, tests)
    }

    @Suppress("UNCHECKED_CAST")
    private fun apply(subject: IModule, tests: List<TestCaseStatement> = listOf()): List<ITestResult> {
        val reference: IModule = loader.load(referenceFile)

        val results = mutableSetOf<ITestResult>()

        // Initialise empty memory
        fun MutableMap<IModule, MutableMap<VariableAssignment, IValue>>.init() {
            clear()
            put(reference, mutableMapOf())
            put(subject, mutableMapOf())
        }
        memory.init()
        machines.clear()

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
                            is VariableReference -> {
                                val value = (memory[module] ?: throw RuntimeException("Test has no memory for module ${module.id}"))[assignment]
                                 if (value == null)
                                     throw RuntimeException("No value stored for variable reference $this")
                                value
                            }
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
                            val refValue = stmt.initializer().evaluate(vm, reference, listener)
                            val subValue = stmt.initializer().evaluate(vm, subject, listener)
                            (memory[reference] ?: throw RuntimeException("Test has no memory for module ${reference.id}"))[stmt] = refValue.getOrThrow()
                            (memory[subject] ?: throw RuntimeException("Test has no memory for module ${subject.id}"))[stmt] = subValue.getOrThrow()
                        }
                        is IExpressionStatement -> when(stmt) {
                            is ProcedureCall -> {
                                listener.extend(case.metrics + stmt.metrics)

                                // println("Executing $stmt with metrics ${case.metrics + stmt.metrics}")

                                val referenceProcedure = reference.findMatchingProcedure(stmt.procedure) ?:
                                throw AssertionError("Reference solution does not implement procedure matching ${stmt.procedure.signature}")

                                val subjectProcedure = subject.findMatchingProcedure(stmt.procedure)

                                if (subjectProcedure == null) {
                                    if (results.none { it is ProcedureNotImplemented && it.procedure == stmt.procedure })
                                        results.add(ProcedureNotImplemented(stmt.procedure))
                                    return
                                }

                                val builder = ResultBuilder(referenceProcedure, subjectProcedure, stmt)

                                val args: Result<List<IValue>> = runCatching {
                                    (stmt.arguments as List<Any>).map {
                                        when(it) {
                                            is IExpressionStatement -> it.evaluate(vm, reference, listener).getOrThrow().deepCopy(vm)
                                            else -> getValue(vm, it).deepCopy(vm)
                                        }
                                    }
                                }.onFailure { println("Error cloning arguments: ${it.stackTraceToString()}") }

                                val referenceArguments = dereference(stmt.arguments as List<Any>, vm, reference, listener, IExpressionStatement::evaluate).toTypedArray()
                                listener.setProcedure(referenceProcedure)
                                val expected: Result<IValue> = kotlin.runCatching { vm.execute(referenceProcedure, *referenceArguments) ?: NULL }

                                // BLACK-BOX
                                if (expected.isSuccess) {
                                    if (stmt.expected != null && !expected.getOrThrow().sameAs(vm.getValue(stmt.expected)))
                                        throw AssertionError("Reference solution return value does not match " +
                                                "user-specified expected return value for procedure call: $stmt")
                                } else if (stmt.expected != null && expected.getOrThrow() != stmt.expected)
                                    throw AssertionError("Reference solution return value does not match " +
                                            "user-specified expected return value for procedure call: $stmt")

                                val subjectArguments = dereference(stmt.arguments, vm, subject, listener, IExpressionStatement::evaluate).toTypedArray()
                                listener.setProcedure(subjectProcedure)
                                val actual = kotlin.runCatching { vm.execute(subjectProcedure, *subjectArguments) ?: NULL }

                                if (args.isSuccess && expected.isSuccess && actual.isSuccess) {
                                    val black = builder.black(expected.getOrThrow(), actual.getOrThrow(), args.getOrThrow())
                                    if (black != null)
                                        results.add(black)
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
                                else
                                    System.err.println("Failed to collect arguments: ${args.exceptionOrNull()!!.stackTraceToString()}")

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
            }

            if (!machines.containsKey(case.root)) {
                val vm = IVirtualMachine.create(loopIterationMaximum = LOOP_ITERATION_LIMIT) // Stateful tests - one VM for all calls in sequence
                val listener = EvaluationMetricListener(vm, case)
                vm.addListener(listener)
                machines[case.root] = Pair(vm, listener)
            }
            val (vm, listener) = machines[case.root]!!
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