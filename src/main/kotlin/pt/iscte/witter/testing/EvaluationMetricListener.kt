package pt.iscte.witter.testing

import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class EvaluationMetricListener(val vm: IVirtualMachine, private val specification: TestCaseStatement): IVirtualMachine.IListener {
    private var procedureBeingMeasured: IProcedureDeclaration? = null

    private val values: MutableMap<IProcedureDeclaration, MutableMap<KClass<out ITestMetric>, Any>> = mutableMapOf()
    private val vmMemoryBefore: MutableMap<IProcedureDeclaration, Int> = mutableMapOf()
    private val extended: MutableSet<ITestMetric> = mutableSetOf()

    fun setProcedure(procedure: IProcedureDeclaration) {
        this.procedureBeingMeasured = procedure
    }

    fun reset() {
        procedureBeingMeasured = null
        values.clear()
        vmMemoryBefore.clear()
        extended.clear()
    }

    fun extend(metrics: Set<ITestMetric>) = extended.addAll(metrics)

    fun metrics(): Set<ITestMetric> = specification.metrics.plus(extended)

    private inline fun <reified T : ITestMetric> contains(): Boolean = metrics().any { it is T }

    private inner class ArrayAccessListener(private val ref: IReference<IArray>) : IArray.IListener {

        override fun elementRead(index: Int, value: IValue) {
            if (contains<CountArrayReadAccesses>())
                increment<CountArrayReadAccesses>()
        }

        override fun elementChanged(index: Int, oldValue: IValue, newValue: IValue) {
            if (contains<CountArrayWriteAccesses>())
                increment<CountArrayWriteAccesses>()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(procedure: IProcedureDeclaration, parameter: KClass<out ITestMetric>, default: T): T {
        return if (values.containsKey(procedure) && values[procedure]!!.containsKey(parameter))
            values[procedure]!![parameter] as T
        else default
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getAll(procedure: IProcedureDeclaration, parameter: KClass<out ITestMetric>): List<T> {
        val all = mutableListOf<T>()
        values.forEach { (proc, metric) ->
            if (proc == procedure) metric.forEach { (type, value) ->
                if (type == parameter)
                    all.add(value as T)
            }
        }
        return all
    }

    private inline fun <reified T : ITestMetric> setMetric(value: Any) {
        if (procedureBeingMeasured != null) {
            if (!values.containsKey(procedureBeingMeasured!!))
                values[procedureBeingMeasured!!] = mutableMapOf()
            values[procedureBeingMeasured!!]!![T::class] = value
        } else
            System.err.println("Cannot set metric ${T::class.simpleName}: procedure being measured is null")
    }

    private inline fun <reified T : ITestMetric> increment() {
        if (procedureBeingMeasured != null) {
            val current = getOrDefault(procedureBeingMeasured!!, T::class, 0)
            setMetric<T>(current + 1)
        } else
            System.err.println("Cannot set metric ${T::class.simpleName}: procedure being measured is null")
    }

    override fun procedureCall(procedure: IProcedureDeclaration, args: List<IValue>, caller: IProcedure?) {
        // Count used memory
        if (procedure == procedureBeingMeasured && contains<CountMemoryUsage>())
            vmMemoryBefore[procedure] = vm.usedMemory

        // Track initial argument states
        if (procedure == procedureBeingMeasured && contains<TrackParameterStates>()) {
            val init = mutableMapOf<IParameter, List<IValue>>()
            procedureBeingMeasured!!.parameters.forEachIndexed { i, param ->
                val arg = args[i]
                init[param] = listOf((if (arg is IReference<*>) arg.target else arg).copy())
            }
            setMetric<TrackParameterStates>(init)
        }

        // Count recursive calls
        if (caller == procedure && contains<CountRecursiveCalls>())
            increment<CountRecursiveCalls>()

        if (contains<CountArrayReadAccesses>() || contains<CountArrayWriteAccesses>()) {
            // Count read accesses for argument arrays
            args.forEach {
                if (it is IReference<*> && it.target is IArray)
                    (it.target as IArray).addListener(ArrayAccessListener(it as IReference<IArray>))
            }
        }
    }

    override fun procedureEnd(procedure: IProcedureDeclaration, args: List<IValue>, result: IValue?) {
        if (procedure == procedureBeingMeasured && contains<CountMemoryUsage>())
            setMetric<CountMemoryUsage>(vm.usedMemory - vmMemoryBefore[procedure]!!)
    }

    // Count loop iterations
    override fun loopIteration(loop: ILoop) {
        if (contains<CountLoopIterations>())
            increment<CountLoopIterations>()
    }

    // Count record allocations
    override fun recordAllocated(ref: IReference<IRecord>) {
        if (contains<CheckObjectAllocations>()) {
            val current = getOrDefault(procedureBeingMeasured!!, CheckObjectAllocations::class, mutableMapOf<IType, Int>())
            current[ref.target.type] = (current[ref.target.type] ?: 0) + 1
            setMetric<CheckObjectAllocations>(current)
        }
    }

    // Count array allocations
    override fun arrayAllocated(ref: IReference<IArray>) {
        if (contains<CheckArrayAllocations>()) {
            val current = getOrDefault(procedureBeingMeasured!!, CheckArrayAllocations::class, mutableMapOf<IType, Int>())
            current[ref.target.type] = (current[ref.target.type] ?: 0) + 1
            setMetric<CheckArrayAllocations>(current)
        }
    }

    // Count array write (assignment) accesses
    override fun arrayElementAssignment(a: IArrayElementAssignment, ref: IReference<IArray>, index: Int, value: IValue) {
        val procedure = vm.callStack.topFrame.procedure
        if (procedure == procedureBeingMeasured && contains<TrackParameterStates>()) {
            if (a.arrayAccess.target is IVariableExpression && (a.arrayAccess.target as IVariableExpression).variable in procedureBeingMeasured!!.parameters) {
                procedureBeingMeasured!!.parameters.find { it.id == a.arrayAccess.target.id }?.let { param ->
                    val allStates = getOrDefault(procedureBeingMeasured!!, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
                    allStates[param] = (allStates[param] ?: listOf()) + listOf(ref.target.copy())
                    setMetric<TrackParameterStates>(allStates)
                }
            }
        }
    }

    override fun fieldAssignment(a: IRecordFieldAssignment, ref: IReference<IRecord>, value: IValue) {
        // Add array listener to allocated array
        if (a.expression is IArrayAllocation && value.type.isArrayReference)
            ((value as IReference<*>).target as IArray).addListener(ArrayAccessListener(value as IReference<IArray>))
    }

    override fun variableAssignment(a: IVariableAssignment, value: IValue) {
        // Check parameter side effects
        if (a.target in procedureBeingMeasured!!.parameters && contains<CheckSideEffects>()) {
            procedureBeingMeasured!!.parameters.find { it.id == a.target.id }?.let { param ->
                val allSideEffects = getOrDefault(procedureBeingMeasured!!, CheckSideEffects::class, mutableMapOf<IParameter, IValue>())
                allSideEffects[param] = value
                setMetric<CheckSideEffects>(allSideEffects)
            }
        }

        if (a.expression is IArrayAllocation && (contains<CountArrayReadAccesses>() || contains<CountArrayWriteAccesses>())) {
            if (value.type.isArrayReference)
                ((value as IReference<*>).target as IArray).addListener(ArrayAccessListener(value as IReference<IArray>))
        }

        // Store argument states
        if (contains<TrackParameterStates>()) {
            procedureBeingMeasured!!.parameters.find { it == a.target }?.let { param ->
                val allStates = getOrDefault(procedureBeingMeasured!!, TrackParameterStates::class, mutableMapOf<IParameter, List<IValue>>())
                allStates[param] = (allStates[param] ?: listOf()) + listOf(value.copy())
                setMetric<TrackParameterStates>(allStates)
            }
        }
    }
}