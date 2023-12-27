package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.IProcedure

class TestSuite(val referencePath: String, val subjectPath: String, val description: String, modules: List<TestModule> = listOf()) {
    private val modules = mutableListOf<TestModule>()

    init {
        this.modules.addAll(modules)
    }

    fun add(module: TestModule) = modules.add(module)

    fun remove(module: TestModule) = modules.remove(module)

    fun modules(): List<TestModule> = modules.toList()
}

/**
 * Abstract procedure test contemplating set of evaluation [metrics].
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 */
abstract class TestModule(open val module: IModule, open val description: String, open val metrics: Set<ITestMetric>) {

    /**
     * Does this test module contain a given [ITestMetric]?
     */
    inline fun <reified T : ITestMetric> contains(): Boolean = metrics.find { it is T } != null

    /**
     * Returns the [ITestMetric] associated with this test module, or null if such a metric does not exist
     * for the module.
     */
    inline fun <reified T : ITestMetric> get(): T? = metrics.find { it is T } as? T
}

/**
 * Represents a suite comprised of several test [cases] that should be executed on a single [procedure].
 * @param procedure The static procedure that should be tested.
 * @param cases A list of TSL-read strings representing the arguments to pass to the procedure in each test case.
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 */
class SingleProcedureTestSuite(
    override val module: IModule,
    val procedure: IProcedure,
    cases: List<Any?>, // if (parsed) List<List<Any?>> else List<String>, where String encodes the entire List<Any?>
    val parsed: Boolean,
    override val metrics: Set<ITestMetric>,
    override val description: String = ""
): TestModule(module, description, metrics) {
    private val cases: MutableList<Any?> = mutableListOf()

    init {
        this.cases.addAll(cases)
    }

    fun addCase(arguments: Any?) = cases.add(arguments)

    fun cases(): List<Any?> = cases.toList()
}

/**
 * Represents a suite of tests that includes a sequence of procedure [calls].
 * @param calls A list of (Procedure, Arguments) pairs.
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 */
class StatefulTestSequence(
    override val module: IModule,
    calls: List<Pair<IProcedure, List<Any?>>>, // List<(Procedure, ListOfArguments)>
    override val metrics: Set<ITestMetric>,
    override val description: String = ""
): TestModule(module, description, metrics) {
    private val calls: MutableList<Pair<IProcedure, List<Any?>>> = mutableListOf()

    init {
        this.calls.addAll(calls)
    }
    
    fun addCall(procedure: IProcedure, arguments: List<Any?>) = calls.add(Pair(procedure, arguments))

    fun calls(): List<Pair<IProcedure, List<Any?>>> = calls.toList()
}