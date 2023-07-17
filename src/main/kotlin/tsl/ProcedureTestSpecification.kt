package tsl

import pt.iscte.strudel.model.IProcedure

sealed interface ITestParameter

data class CountLoopIterations(val margin: Int): ITestParameter

data class CountRecordAllocations(val margin: Int): ITestParameter

data class CountArrayAllocations(val margin: Int): ITestParameter

data class CountArrayReadAccesses(val margin: Int): ITestParameter

data class CountArrayWriteAccesses(val margin: Int): ITestParameter

data class CountMemoryUsage(val margin: Int): ITestParameter

object TrackParameterStates : ITestParameter

object CheckParameterMutability: ITestParameter

class ProcedureTestSpecification(val procedure: IProcedure, val cases: List<String>, val parameters: Set<ITestParameter>) {

    inline fun <reified T : ITestParameter> contains(): Boolean = parameters.find { it is T } != null

    inline fun <reified T : ITestParameter> get(): T? = parameters.find { it is T } as? T
}