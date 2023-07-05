package testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
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

                val args = TestSpecifier.parseArgumentsString(vm, arguments)

                val listener = ProcedureTestListener(vm, specification)
                vm.addListener(listener)

                val expected = vm.execute(referenceProcedure, *args.toTypedArray())?.value
                val actual = vm.execute(subjectProcedure, *args.toTypedArray())?.value

                // Check result equality
                // TODO: find a way to include alternative solutions
                if (actual != expected)
                    results[subjectProcedure]!!.add(IncorrectInvocationResult(method, args, expected, actual))

                // Check loop iterations
                specification.get<CountLoopIterations>()?.let { parameter ->
                    val expectedLoopIterations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualLoopIterations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualLoopIterations.notInRange(expectedLoopIterations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "loop iterations",
                            method, args, expectedLoopIterations, parameter.margin, actualLoopIterations))
                }

                // Check record allocations
                specification.get<CountRecordAllocations>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "record allocations",
                            method, args, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check array allocations
                specification.get<CountArrayAllocations>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "array allocations",
                            method, args, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check array read accesses
                specification.get<CountArrayReadAccesses>()?.let { parameter ->
                    TODO("Not yet implemented in strudel")
                }

                // Check array write accesses
                specification.get<CountArrayWriteAccesses>()?.let { parameter ->
                    val expectedRecordAllocations = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualRecordAllocations = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualRecordAllocations.notInRange(expectedRecordAllocations, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "array write accesses",
                            method, args, expectedRecordAllocations, parameter.margin, actualRecordAllocations))
                }

                // Check memory usage
                // TODO: I don't think this is working, check listener
                specification.get<CountMemoryUsage>()?.let { parameter ->
                    val expectedMemoryUsage = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val actualMemoryUsage = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    if (actualMemoryUsage.notInRange(expectedMemoryUsage, parameter.margin))
                        results[subjectProcedure]!!.add(MeasuredValueNotInRange(
                            "used memory bytes",
                            method, args, expectedMemoryUsage, parameter.margin, actualMemoryUsage))
                }

                // Check variable states
                specification.get<TrackVariableStates>()?.let { parameter ->
                    // TODO
                }

                // Check parameter immutability
                specification.get<CheckParameterImmutability>()?.let { parameter ->
                    // TODO
                }
            }
        }

        return results
    }
}