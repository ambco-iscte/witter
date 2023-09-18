package pt.iscte.witter.testing

import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.*
import kotlin.reflect.KClass

typealias Invocation = Pair<IProcedure, List<IValue>>

class ProcedureTestListener(private val vm: IVirtualMachine, private val specification: ProcedureTestSpecification): IVirtualMachine.IListener {
    private val values: MutableMap<Invocation, MutableMap<KClass<out ITestParameter>, Any>> = mutableMapOf()

    private val previousArgumentsForProcedure: MutableMap<IProcedure, List<IValue>> = mutableMapOf()

    private val vmMemoryBefore: MutableMap<IProcedure, Int> = mutableMapOf()

    private inner class ArrayReadAccessListener(private val procedure: IProcedure) : IArray.IListener {
        override fun elementRead(index: Int, value: IValue) {
            val current = getOrDefault(procedure, CountArrayReadAccesses::class, 0)
            setMetric<CountArrayReadAccesses>(procedure, current + 1)
        }
    }

    override fun procedureCall(procedure: IProcedure, args: List<IValue>, caller: IProcedure?) {
        // Count used memory
        previousArgumentsForProcedure[procedure] = args
        if (specification.contains<CountMemoryUsage>())
            vmMemoryBefore[procedure] = vm.usedMemory

        // Track initial argument states
        if (specification.contains<TrackParameterStates>()) {
            val init = mutableMapOf<IParameter, List<IValue>>()
            procedure.parameters.forEachIndexed { i, param ->
                val arg = args[i]
                init[param] = listOf((if (arg is IReference<*>) arg.target else arg).copy())
            }
            setMetric<TrackParameterStates>(procedure, init)
        }

        // Count recursive calls
        if (specification.contains<CountRecursiveCalls>() && caller == procedure) {
            val rec = getOrDefault(procedure, CountRecursiveCalls::class, 0)
            setMetric<CountRecursiveCalls>(procedure, rec + 1)
        }

        // Count read accesses for argument arrays
        if (specification.contains<CountArrayReadAccesses>()) {
            args.forEach {
                if (it is IArray)  {
                    it.addListener(ArrayReadAccessListener(procedure))
                } else if (it is IReference<*> && it.target is IArray) {
                    (it.target as IArray).addListener(ArrayReadAccessListener(procedure))
                }
            }
        }
    }

    override fun procedureEnd(procedure: IProcedure, args: List<IValue>, result: IValue?) {
        if (specification.contains<CountMemoryUsage>())
            setMetric<CountMemoryUsage>(procedure, vm.usedMemory - vmMemoryBefore[procedure]!!)
    }

    private fun invocation(procedure: IProcedure): Invocation = Pair(procedure, previousArgumentsForProcedure[procedure]!!)

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(procedure: IProcedure, parameter: KClass<out ITestParameter>, default: T): T {
        val invocation = invocation(procedure)
        return if (values.containsKey(invocation) && values[invocation]!!.containsKey(parameter))
            values[invocation]!![parameter] as T
        else default
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getAll(procedure: IProcedure, parameter: KClass<out ITestParameter>): List<T> {
        val all = mutableListOf<T>()
        values.forEach { (invocation, metric) ->
            if (invocation.first == procedure) metric.forEach { (type, value) ->
                if (type == parameter)
                    all.add(value as T)
            }
        }
        return all
    }

    private inline fun <reified T : ITestParameter> setMetric(procedure: IProcedure, value: Any) {
        val invocation = invocation(procedure)
        if (!values.containsKey(invocation))
            values[invocation] = mutableMapOf()
        values[invocation]!![T::class] = value
    }

    // Count loop iterations
    override fun loopIteration(loop: ILoop) {
        if (!specification.contains<CountLoopIterations>()) return

        val procedure = loop.ownerProcedure
        val current = getOrDefault(procedure, CountLoopIterations::class, 0)
        setMetric<CountLoopIterations>(procedure, current + 1)
    }

    // Count record allocations
    // TODO track types along with count
    override fun recordAllocated(ref: IReference<IRecord>) {
        if (!specification.contains<CheckObjectAllocations>()) return

        val procedure = vm.callStack.topFrame.procedure
        val current = getOrDefault(procedure, CheckObjectAllocations::class, 0)
        setMetric<CheckObjectAllocations>(procedure, current + 1)
    }

    // Count array allocations
    // TODO track types along with count
    override fun arrayAllocated(ref: IReference<IArray>) {
        if (!specification.contains<CheckArrayAllocations>()) return

        val procedure = vm.callStack.topFrame.procedure

        // Count array read accesses
        ref.target.addListener(ArrayReadAccessListener(procedure))

        val current = getOrDefault(procedure, CheckArrayAllocations::class, 0)
        setMetric<CheckArrayAllocations>(procedure, current + 1)
    }

    // Count array write (assignment) accesses
    override fun arrayElementAssignment(a: IArrayElementAssignment, ref: IReference<IArray>, index: Int, value: IValue) {
        val procedure = vm.callStack.topFrame.procedure

        if (specification.contains<CountArrayWriteAccesses>()) {
            val current = getOrDefault(procedure, CountArrayWriteAccesses::class, 0)
            setMetric<CountArrayWriteAccesses>(procedure, current + 1)
        }

        if (!specification.contains<TrackParameterStates>()) return

        // TODO using id seems iffy... better way?
        procedure.parameters.find { it.id == a.arrayAccess.target.id }?.let { param ->
            val allStates = getOrDefault(procedure, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
            allStates[param] = (allStates[param] ?: listOf()) + listOf(ref.target.copy())
            setMetric<TrackParameterStates>(procedure, allStates)
        }
    }

    override fun variableAssignment(a: IVariableAssignment, value: IValue) {
        val procedure = a.ownerProcedure

        // Check parameter mutability
        // TODO - more in-depth, say which parameter was modified when it shouldn't have been?
        if (specification.contains<CheckSideEffects>()) {
            if (a.target in procedure.parameters)
                setMetric<CheckSideEffects>(procedure, true)
        }

        // Store argument states
        if (!specification.contains<TrackParameterStates>()) return

        procedure.parameters.find { it == a.target }?.let { param ->
            val allStates = getOrDefault(procedure, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
            allStates[param] = (allStates[param] ?: listOf()) + listOf(value.copy())
            setMetric<TrackParameterStates>(procedure, allStates)
        }
    }
}