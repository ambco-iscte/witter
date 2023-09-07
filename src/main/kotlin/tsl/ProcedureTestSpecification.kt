package tsl

import pt.iscte.strudel.model.IProcedure

sealed interface ITestParameter {
    fun description(): String
}

data class CountLoopIterations(val margin: Int): ITestParameter {
    override fun description(): String  = "loop iterations"
}

object CheckObjectAllocations : ITestParameter {
    override fun description(): String = "object allocations"
}

object CheckArrayAllocations : ITestParameter {
    override fun description(): String = "array allocations"
}

data class CountArrayReadAccesses(val margin: Int): ITestParameter {
    override fun description(): String  = "array reads"
}

data class CountArrayWriteAccesses(val margin: Int): ITestParameter {
    override fun description(): String  = "array writes"
}

data class CountMemoryUsage(val margin: Int): ITestParameter {
    override fun description(): String  = "used memory bytes"
}

object TrackParameterStates : ITestParameter {
    override fun description(): String  = "parameter states"
}

object CheckSideEffects: ITestParameter {
    override fun description(): String  = "side effects"
}

data class CountRecursiveCalls(val margin: Int): ITestParameter {
    override fun description(): String  = "recursive calls"
}

class ProcedureTestSpecification(val procedure: IProcedure, val cases: List<String>, val parameters: Set<ITestParameter>) {

    inline fun <reified T : ITestParameter> contains(): Boolean = parameters.find { it is T } != null

    inline fun <reified T : ITestParameter> get(): T? = parameters.find { it is T } as? T
}