package pt.iscte.witter.tsl

sealed interface ITestMetric {
    fun description(): String
}

operator fun ITestMetric.plus(other: ITestMetric): Set<ITestMetric> = setOf(this, other)

data class CountLoopIterations(val margin: Int = 0): ITestMetric {
    override fun description(): String  = "loop iterations"
}

data class CountArrayReadAccesses(val margin: Int = 0): ITestMetric {
    override fun description(): String  = "array reads"
}

data class CountArrayWriteAccesses(val margin: Int = 0): ITestMetric {
    override fun description(): String  = "array writes"
}

data class CountMemoryUsage(val margin: Int = 0): ITestMetric {
    override fun description(): String  = "used memory bytes"
}

object CheckObjectAllocations : ITestMetric {
    override fun description(): String = "object allocations"
}

object CheckArrayAllocations : ITestMetric {
    override fun description(): String = "array allocations"
}

object TrackParameterStates : ITestMetric {
    override fun description(): String  = "parameter states"
}

object CheckSideEffects: ITestMetric {
    override fun description(): String  = "side effects"
}

data class CountRecursiveCalls(val margin: Int = 0): ITestMetric {
    override fun description(): String  = "recursive calls"
}