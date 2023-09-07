package testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IArray
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import tsl.*
import java.io.File

@Suppress("UNCHECKED_CAST")
private fun IValue.sameAs(other: IValue): Boolean =
    if (this is IArray && other is IArray) (value as Array<IValue>).sameAs(other.value as Array<IValue>)
    else value == other.value

private fun Array<IValue>.sameAs(other: Array<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

private fun Iterable<IValue>.sameAs(other: Iterable<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

class Tester(referenceFile: String) {
    private val loader = Java2Strudel()
    private val ref: IModule = loader.load(File(referenceFile))

    private fun Int.inRange(start: Int, margin: Int): Boolean = this >= start - margin && this <= start + margin

    private val IModule.definedTests: List<ProcedureTestSpecification>
        get() {
            val tests = mutableListOf<ProcedureTestSpecification>()
            procedures.forEach { procedure ->
                TestSpecifier.translate(procedure)?.let { tests.add(it) }
            }
            return tests.toList()
        }

    fun execute(file: String): List<TestResult> {
        val subject: IModule = loader.load(File(file))
        val results = mutableListOf<TestResult>()
        
        ref.definedTests.forEach { specification ->
            val referenceProcedure = specification.procedure // TODO: find a way to include alternative solutions
            val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)

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
                if (subjectProcedure.returnType != VOID || referenceProcedure.returnType != VOID)
                    results.add(TestResult(
                        actual == expected,
                        subjectProcedure,
                        unmodified,
                        "result",
                        expected,
                        null,
                        actual
                    ))

                // ---------------------
                //       WHITE-BOX
                // ---------------------

                // TODO ugly duplicate code

                // Check loop iterations
                specification.get<CountLoopIterations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        parameter.margin,
                        act
                    ))
                }

                // Check record allocations
                specification.get<CheckObjectAllocations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act == exp
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        null,
                        act
                    ))
                }

                // Check array allocations
                specification.get<CheckArrayAllocations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act == exp
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        null,
                        act
                    ))
                }

                // Check array read accesses
                specification.get<CountArrayReadAccesses>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        parameter.margin,
                        act
                    ))
                }

                // Check array write accesses
                specification.get<CountArrayWriteAccesses>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        parameter.margin,
                        act
                    ))
                }

                // Check memory usage
                specification.get<CountMemoryUsage>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        parameter.margin,
                        act
                    ))
                }

                // Check recursive calls
                // TODO getAll could be used for more stuff when recursion is at play
                specification.get<CountRecursiveCalls>()?.let { parameter ->
                    val exp = listener.getAll<Int>(referenceProcedure, parameter::class).sum()
                    val act = listener.getAll<Int>(subjectProcedure, parameter::class).sum()

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        parameter.margin,
                        act
                    ))
                }

                // Check variable states for procedure arguments
                specification.get<TrackParameterStates>()?.let { parameter ->
                    val expectedParamStates = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, List<IValue>>())
                    val actualParamStates = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, List<IValue>>())

                    referenceProcedure.parameters.forEachIndexed { i, param ->
                        val exp = expectedParamStates[param] ?: listOf()
                        val act = actualParamStates[subjectProcedure.parameters[i]] ?: listOf()

                        val passed = act.sameAs(exp)
                        results.add(TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description() + " of ${param.id}",
                            exp,
                            null,
                            act
                        ))
                    }
                }

                // Check parameter immutability
                specification.get<CheckSideEffects>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, false)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, false)

                    // TODO paper says expected side effects: {an array}
                    //  is this the return value? I was doing it like true/false whether or not params were modified

                    val passed = exp == act
                    results.add(TestResult(
                        passed,
                        subjectProcedure,
                        unmodified,
                        parameter.description(),
                        exp,
                        null,
                        act
                    ))
                }
            }
        }

        return results
    }
}