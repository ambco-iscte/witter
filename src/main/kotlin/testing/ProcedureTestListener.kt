package testing

import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import tsl.*
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

    // Count used memory
    override fun procedureCall(procedure: IProcedure, args: List<IValue>, caller: IProcedure?) {
        previousArgumentsForProcedure[procedure] = args
        if (specification.contains<CountMemoryUsage>())
            vmMemoryBefore[procedure] = vm.usedMemory

        if (specification.contains<TrackParameterStates>()) {
            val init = mutableMapOf<IParameter, List<IValue>>()
            procedure.parameters.forEachIndexed { i, param ->
                init[param] = listOf(args[i])
            }
            setMetric<TrackParameterStates>(procedure, init)
        }

        if (!specification.contains<CountArrayReadAccesses>()) return

        // Count read accesses for argument arrays
        args.forEach {
            if (it is IArray)  {
                it.addListener(ArrayReadAccessListener(procedure))
            } else if (it is IReference<*> && it.target is IArray) {
                (it.target as IArray).addListener(ArrayReadAccessListener(procedure))
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
    override fun recordAllocated(ref: IReference<IRecord>) {
        if (!specification.contains<CountRecordAllocations>()) return

        val procedure = vm.callStack.topFrame.procedure
        val current = getOrDefault(procedure, CountRecordAllocations::class, 0)
        setMetric<CountRecordAllocations>(procedure, current + 1)
    }

    // Count array allocations
    override fun arrayAllocated(ref: IReference<IArray>) {
        if (!specification.contains<CountArrayAllocations>()) return

        val procedure = vm.callStack.topFrame.procedure

        // Count array read accesses
        ref.target.addListener(ArrayReadAccessListener(procedure))

        val current = getOrDefault(procedure, CountArrayAllocations::class, 0)
        setMetric<CountArrayAllocations>(procedure, current + 1)
    }

    // Count array write (assignment) accesses
    override fun arrayElementAssignment(ref: IReference<IArray>, index: Int, expression: IExpression, value: IValue) {
        val procedure = vm.callStack.topFrame.procedure

        if (specification.contains<CountArrayWriteAccesses>()) {
            val current = getOrDefault(procedure, CountArrayWriteAccesses::class, 0)
            setMetric<CountArrayWriteAccesses>(procedure, current + 1)
        }

        if (!specification.contains<TrackParameterStates>()) return

        procedure.parameters.find { it == ref || it == ref.target }?.let { param ->
            val allStates = getOrDefault(procedure, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
            allStates[param] = (allStates[param] ?: listOf()) + listOf(ref.target)
            setMetric<TrackParameterStates>(procedure, allStates)
        }
    }

    override fun variableAssignment(a: IVariableAssignment, value: IValue) {
        val procedure = a.ownerProcedure

        // Check parameter mutability
        // TODO - more in-depth, say which parameter was modified when it shouldn't have been?
        if (specification.contains<CheckParameterMutability>()) {
            if (a.target in procedure.parameters)
                setMetric<CheckParameterMutability>(procedure, true)
        }

        // Store argument states
        if (!specification.contains<TrackParameterStates>()) return

        procedure.parameters.find { it == a.target }?.let { param ->
            val allStates = getOrDefault(procedure, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
            allStates[param] = (allStates[param] ?: listOf()) + listOf(value)
            setMetric<TrackParameterStates>(procedure, allStates)
        }
    }
}