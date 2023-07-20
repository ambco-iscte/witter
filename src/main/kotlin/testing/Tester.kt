package testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import tsl.*
import java.io.File

class Tester(reference: File) {
    private val loader = Java2Strudel()
    private val ref: IModule = loader.load(reference)

    private fun Int.notInRange(start: Int, margin: Int): Boolean = !(this >= start - margin && this <= start + margin)

    private val IModule.definedTests: List<ProcedureTestSpecification>
        get() {
            val tests = mutableListOf<ProcedureTestSpecification>()
            procedures.forEach { procedure ->
                TestSpecifier.translate(procedure)?.let { tests.add(it) }
            }
            return tests.toList()
        }

    fun execute(file: File): Map<IProcedure, List<Feedback>> {
        val subject: IModule = loader.load(file)
        val results = mutableMapOf<IProcedure, MutableList<Feedback>>()

        ref.definedTests.forEach { specification ->
            val referenceProcedure = specification.procedure
            val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)
            val method = subjectProcedure.id!!

            if (!results.containsKey(subjectProcedure)) results[subjectProcedure] = mutableListOf()

            specification.cases.forEach { arguments ->
                val vm = IVirtualMachine.create()

                // Allocate arguments twice so listeners don't end up attached to the same record or array
                val unmodified = TestSpecifier.parseArgumentsString(vm, arguments)
                val args = TestSpecifier.parseArgumentsString(vm, arguments)
                val argsCopy = TestSpecifier.parseArgumentsString(vm, arguments)

                val listener = ProcedureTestListener(vm, specification)
                vm.addListener(listener)

                val expected = vm.execute(referenceProcedure, *args.toTypedArray())?.value
                val actual = vm.execute(subjectProcedure, *argsCopy.toTypedArray())?.value

                // ---------------------
                //       BLACK-BOX
                // ---------------------

                // Check result equality
                // TODO: find a way to include alternative solutions
                if (actual != expected)
                    results[subjectProcedure]!!.add(IncorrectInvocationResult(method, unmodified, expected, actual))

                // ---------------------
                //       WHITE-BOX
                // ---------------------

                // Check loop iterations
                specification.get<CountLoopIterations>()?.let { parameter ->
                    val expectedLoopIterations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualLoopIterations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualLoopIterations.notInRange(expectedLoopIterations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "loop iterations",
                            method, unmodified, expectedLoopIterations, parameter.margin, actualLoopIterations))
                }

                // Check record allocations
                specification.get<CountRecordAllocations>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "record allocations",
                            method, unmodified, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check array allocations
                specification.get<CountArrayAllocations>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "array allocations",
                            method, unmodified, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check array read accesses
                specification.get<CountArrayReadAccesses>()?.let { parameter ->
                    val expectedArrayReads = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualArrayReads = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualArrayReads.notInRange(expectedArrayReads, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "array read accesses",
                            method, unmodified, expectedArrayReads, parameter.margin, actualArrayReads))
                }

                // Check array write accesses
                specification.get<CountArrayWriteAccesses>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "array write accesses",
                            method, unmodified, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check memory usage
                specification.get<CountMemoryUsage>()?.let { parameter ->
                    val expectedMemoryUsage = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualMemoryUsage = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualMemoryUsage.notInRange(expectedMemoryUsage, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "allocated memory bytes",
                            method, unmodified, expectedMemoryUsage, parameter.margin, actualMemoryUsage))
                }

                // Check recursive calls
                // TODO getAll could be used for more stuff when recursion is at play
                specification.get<CountRecursiveCalls>()?.let { parameter ->
                    val expectedRecursiveCalls = listener.getAll<Int>(referenceProcedure, parameter::class).sum()
                    val actualRecursiveCalls = listener.getAll<Int>(subjectProcedure, parameter::class).sum()

                    if (actualRecursiveCalls.notInRange(expectedRecursiveCalls, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "recursive calls",
                            method, unmodified, expectedRecursiveCalls, parameter.margin, actualRecursiveCalls))
                }

                // Check variable states for procedure arguments
                specification.get<TrackParameterStates>()?.let { parameter ->
                    val expectedParamStates = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, List<IValue>>())
                    val actualParamStates = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, List<IValue>>())

                    referenceProcedure.parameters.forEachIndexed { i, param ->
                        val e = expectedParamStates[param] ?: listOf()
                        val a = actualParamStates[subjectProcedure.parameters[i]] ?: listOf()

                        if (a != e)
                            results[subjectProcedure]!!.add(InconsistentArgumentStates(
                                method,
                                unmodified,
                                param,
                                e,
                                a
                            ))
                    }
                }

                // Check parameter immutability
                specification.get<CheckParameterMutability>()?.let { parameter ->
                    val expectedChangesParameters = listener.getOrDefault(referenceProcedure, parameter::class, false)
                    val actualChangesParameters = listener.getOrDefault(subjectProcedure, parameter::class, false)

                    if (actualChangesParameters != expectedChangesParameters)
                        results[subjectProcedure]!!.add(InconsistentParameterMutability(
                            method,
                            unmodified,
                            expectedChangesParameters,
                            actualChangesParameters
                        ))
                }
            }
        }

        return results
    }
}