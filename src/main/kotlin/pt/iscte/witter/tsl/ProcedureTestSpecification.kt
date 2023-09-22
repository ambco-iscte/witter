package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IProcedure

sealed interface ITestMetric {
    fun description(): String
}

data class CountLoopIterations(val margin: Int): ITestMetric {
    override fun description(): String  = "loop iterations"
}

object CheckObjectAllocations : ITestMetric {
    override fun description(): String = "object allocations"
}

object CheckArrayAllocations : ITestMetric {
    override fun description(): String = "array allocations"
}

data class CountArrayReadAccesses(val margin: Int): ITestMetric {
    override fun description(): String  = "array reads"
}

data class CountArrayWriteAccesses(val margin: Int): ITestMetric {
    override fun description(): String  = "array writes"
}

data class CountMemoryUsage(val margin: Int): ITestMetric {
    override fun description(): String  = "used memory bytes"
}

object TrackParameterStates : ITestMetric {
    override fun description(): String  = "parameter states"
}

object CheckSideEffects: ITestMetric {
    override fun description(): String  = "side effects"
}

data class CountRecursiveCalls(val margin: Int): ITestMetric {
    override fun description(): String  = "recursive calls"
}

class ProcedureTestSpecification(val procedure: IProcedure, val cases: List<String>, val metrics: Set<ITestMetric>) {

    inline fun <reified T : ITestMetric> contains(): Boolean = metrics.find { it is T } != null

    inline fun <reified T : ITestMetric> get(): T? = metrics.find { it is T } as? T
}