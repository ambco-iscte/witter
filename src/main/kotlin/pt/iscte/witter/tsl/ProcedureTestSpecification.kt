package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IProcedure

abstract class TestModule(open val metrics: Set<ITestMetric>) {
    inline fun <reified T : ITestMetric> contains(): Boolean = metrics.find { it is T } != null

    inline fun <reified T : ITestMetric> get(): T? = metrics.find { it is T } as? T
}

data class StaticProcedureTest(
    val procedure: IProcedure,
    val cases: List<String>,
    override val metrics: Set<ITestMetric>
): TestModule(metrics)

data class SequentialProcedureTest(
    val calls: List<Pair<IProcedure, String>>,
    override val metrics: Set<ITestMetric>
): TestModule(metrics)