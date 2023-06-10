package testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IArray
import pt.iscte.strudel.vm.IReference
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import java.io.File
import kotlin.reflect.full.primaryConstructor

class Tester(reference: File) {
    private val loader = Java2Strudel()
    private val ref: IModule = loader.load(reference)

    private enum class Flags {
        COUNT_ITERATIONS,
        COUNT_ARRAY_ALLOCATIONS,
        COUNT_ARRAY_ASSIGNMENTS,
        COUNT_VARIABLE_ASSIGNMENTS
    }

    private val flags: MutableList<Flags> = mutableListOf()

    // Very meh builder pattern
    fun countIterations(): Tester {
        if (!flags.contains(Flags.COUNT_ITERATIONS)) flags.add(Flags.COUNT_ITERATIONS)
        return this
    }

    fun countArrayAllocations(): Tester {
        if (!flags.contains(Flags.COUNT_ARRAY_ALLOCATIONS)) flags.add(Flags.COUNT_ARRAY_ALLOCATIONS)
        return this
    }

    fun countArrayAssignments(): Tester {
        if (!flags.contains(Flags.COUNT_ARRAY_ASSIGNMENTS)) flags.add(Flags.COUNT_ARRAY_ASSIGNMENTS)
        return this
    }

    fun countVariableAssignments(): Tester {
        if (!flags.contains(Flags.COUNT_VARIABLE_ASSIGNMENTS)) flags.add(Flags.COUNT_VARIABLE_ASSIGNMENTS)
        return this
    }

    private fun IModule.getProcedure(test: ITestCase): IProcedure = when (test) {
        is StaticMethodTest -> getProcedure(test.getMethodName())
        is InstanceMethodTest -> getProcedure(test.getMethodName(), test.namespace)
    }

    fun evaluate(vm: IVirtualMachine, file: File, tests: List<ITestCase>): Map<String, List<Evaluation>> {
        val subject: IModule = loader.load(file)
        val results = mutableMapOf<String, MutableList<Evaluation>>()

        val iterations = mutableMapOf<IProcedure, Int>()
        val arrayAllocations = mutableMapOf<IProcedure, MutableList<IArray>>()
        val arrayAssignments = mutableMapOf<IProcedure, MutableList<IArrayElementAssignment>>()
        val variableAssignments = mutableMapOf<IProcedure, MutableList<IVariableAssignment>>()

        // Add listener
        vm.addListener(object : IVirtualMachine.IListener {
            override fun loopIteration(loop: ILoop) {
                if (!flags.contains(Flags.COUNT_ITERATIONS)) return

                val procedure = loop.ownerProcedure
                if (!iterations.containsKey(procedure)) iterations[procedure] = 0
                iterations[procedure] = iterations[procedure]!! + 1
            }

            override fun arrayAllocated(ref: IReference<IArray>) {
                if (!flags.contains(Flags.COUNT_ARRAY_ALLOCATIONS)) return

                val procedure = vm.callStack.topFrame.procedure
                if (!arrayAllocations.containsKey(procedure)) arrayAllocations[procedure] = mutableListOf()
                arrayAllocations[procedure]!!.add(ref.target)
            }

            override fun arrayElementAssignment(a: IArrayElementAssignment, index: Int, value: IValue) {
                if (!flags.contains(Flags.COUNT_ARRAY_ASSIGNMENTS)) return

                val procedure = a.ownerProcedure
                if (!arrayAssignments.containsKey(procedure)) arrayAssignments[procedure] = mutableListOf()
                arrayAssignments[procedure]!!.add(a)
            }

            override fun variableAssignment(a: IVariableAssignment, value: IValue) {
                if (!flags.contains(Flags.COUNT_VARIABLE_ASSIGNMENTS)) return

                val procedure = a.ownerProcedure
                if (!variableAssignments.containsKey(procedure)) variableAssignments[procedure] = mutableListOf()
                variableAssignments[procedure]!!.add(a)
            }
        })

        tests.forEach { test ->
            val method: String = test.getMethodName()

            if (!results.containsKey(method)) results[method] = mutableListOf()

            val args = test.getMethodArguments().toTypedArray()

            val referenceProcedure = ref.getProcedure(test)
            val subjectProcedure = subject.getProcedure(test)

            val expected = vm.execute(referenceProcedure, *args)?.value
            val actual = vm.execute(subjectProcedure, *args)?.value

            // Check result equality
            if (actual != expected)
                results[method]!!.add(IncorrectInvocationResult(method, test.getMethodArguments(), expected, actual))

            // Check method iterations
            // TODO - know which loop specifically iterated too many times
            // Problem: how to know that two loops are "the same" if they can be in different positions
            // in different procedures?
            if (flags.contains(Flags.COUNT_ITERATIONS)) {
                val expectedIterations = iterations[referenceProcedure] ?: 0
                val actualIterations = iterations[subjectProcedure] ?: 0

                if (actualIterations > expectedIterations)
                    results[method]!!.add(TooManyIterations(method, test.getMethodArguments(), expectedIterations, actualIterations))
            }

            // Check array allocations
            // TODO - feedback like "your array b[] was unnecessary"
            // Problem: how to know two arrays with possibly different names are "the same" in both procedures?
            if (flags.contains(Flags.COUNT_ARRAY_ALLOCATIONS)) {
                val expectedArrayAllocations = arrayAllocations[referenceProcedure]?.size ?: 0
                val actualArrayAllocations = arrayAllocations[subjectProcedure]?.size ?: 0

                if (actualArrayAllocations > expectedArrayAllocations)
                    results[method]!!.add(TooManyArrayAllocations(method, test.getMethodArguments(), expectedArrayAllocations, actualArrayAllocations))
            }

            // Check array assignments
            // TODO - feedback like "you didn't need to assign b[3] = item"
            // Problem: how to know if the assigned array is "the same" + how the items are "the same"
            if (flags.contains(Flags.COUNT_ARRAY_ASSIGNMENTS)) {
                val expectedArrayAssignments = arrayAssignments[referenceProcedure]?.size ?: 0
                val actualArrayAssignments = arrayAssignments[subjectProcedure]?.size ?: 0

                if (actualArrayAssignments > expectedArrayAssignments)
                    results[method]!!.add(TooManyArrayAssignments(method, test.getMethodArguments(), expectedArrayAssignments, actualArrayAssignments))
            }

            // Check variable assignments
            // TODO - feedback like "you didn't need your 'count' variable"
            // Problem: same thing as loopIteration
            if (flags.contains(Flags.COUNT_VARIABLE_ASSIGNMENTS)) {
                val expectedVariableAssignments = variableAssignments[referenceProcedure]?.size ?: 0
                val actualVariableAssignments = variableAssignments[subjectProcedure]?.size ?: 0

                if (actualVariableAssignments > expectedVariableAssignments)
                    results[method]!!.add(TooManyVariableAssignments(method, test.getMethodArguments(), expectedVariableAssignments, actualVariableAssignments))
            }
        }

        return results
    }
}