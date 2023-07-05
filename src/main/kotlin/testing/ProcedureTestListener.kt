package testing

import pt.iscte.strudel.model.IArrayElementAssignment
import pt.iscte.strudel.model.ILoop
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.vm.*
import tsl.*
import kotlin.reflect.KClass

typealias Invocation = Pair<IProcedure, List<IValue>>

class ProcedureTestListener(private val vm: IVirtualMachine, private val specification: ProcedureTestSpecification): IVirtualMachine.IListener {
    private val values: MutableMap<Pair<IProcedure, List<IValue>>, MutableMap<KClass<out ITestParameter>, Any>> = mutableMapOf()

    private val previousArgumentsForProcedure: MutableMap<IProcedure, List<IValue>> = mutableMapOf()

    private val memoryBefore: MutableMap<IProcedure, Int> = mutableMapOf()

    // Count used memory
    override fun procedureCall(procedure: IProcedure, args: List<IValue>, caller: IProcedure?) {
        previousArgumentsForProcedure[procedure] = args
        if (specification.contains<CountMemoryUsage>())
            memoryBefore[procedure] = vm.usedMemory
    }

    override fun procedureEnd(procedure: IProcedure, args: List<IValue>, result: IValue?) {
        if (specification.contains<CountMemoryUsage>())
            setMetric<CountMemoryUsage>(procedure, vm.usedMemory - memoryBefore[procedure]!!)
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

        val current = getOrDefault(procedure, CountArrayAllocations::class, 0)
        setMetric<CountArrayAllocations>(procedure, current + 1)
    }

    // TODO: Count array read accesses

    // Count array write (assignment) accesses
    override fun arrayElementAssignment(a: IArrayElementAssignment, index: Int, value: IValue) {
        if (!specification.contains<CountArrayWriteAccesses>()) return

        val procedure = a.ownerProcedure

        val current = getOrDefault(procedure, CountArrayWriteAccesses::class, 0)
        setMetric<CountArrayWriteAccesses>(procedure, current + 1)
    }
}