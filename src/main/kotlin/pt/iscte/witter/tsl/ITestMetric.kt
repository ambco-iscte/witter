package pt.iscte.witter.tsl

sealed interface ITestMetric {
    val description: String
}

operator fun ITestMetric.plus(other: ITestMetric): Set<ITestMetric> = setOf(this, other)

data class CountLoopIterations(val margin: Int = 0): ITestMetric {
    override val description: String = "loop iterations"
}

data class CountArrayReadAccesses(val margin: Int = 0): ITestMetric {
    override val description: String  = "array reads"
}

data class CountArrayWriteAccesses(val margin: Int = 0): ITestMetric {
    override val description: String  = "array writes"
}

data class CountMemoryUsage(val margin: Int = 0): ITestMetric {
    override val description: String  = "used memory bytes"
}

data class TrackParameterStates(val parameterID: String) : ITestMetric {
    override val description: String  = "parameter states of $parameterID"
}

data class CountRecursiveCalls(val margin: Int = 0): ITestMetric {
    override val description: String  = "recursive calls"
}

object CheckObjectAllocations : ITestMetric {
    override val description: String = "object allocations"
}

object CheckArrayAllocations : ITestMetric {
    override val description: String = "array allocations"
}

object CheckSideEffects: ITestMetric {
    override val description: String  = "side effects"
}