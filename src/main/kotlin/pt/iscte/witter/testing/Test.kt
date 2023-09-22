package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.witter.tsl.*
import java.io.File

class Test(referenceFilePath: String) {
    constructor(referenceFile: File) : this(referenceFile.path)

    private val loader = Java2Strudel()
    private val ref: IModule = loader.load(File(referenceFilePath))

    private fun Int.inRange(start: Int, margin: Int): Boolean = this >= start - margin && this <= start + margin

    fun execute(file: File): List<TestResult> = execute(loader.load(file))

    fun execute(filePath: String): List<TestResult> = execute(loader.load(File(filePath)))

    fun executeSource(source: String): List<TestResult> = execute(loader.load(source))

    private fun execute(subject: IModule): List<TestResult> {
        val results = mutableListOf<TestResult>()

        ref.definedWitterTests.forEach { specification ->
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
                    results.add(
                        TestResult(
                            actual == expected,
                            subjectProcedure,
                            unmodified,
                            "result",
                            expected,
                            null,
                            actual
                        )
                    )

                // ---------------------
                //       WHITE-BOX
                // ---------------------

                // TODO ugly duplicate code

                // Check loop iterations
                specification.get<CountLoopIterations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp,
                            parameter.margin,
                            act
                        )
                    )
                }

                // Check record allocations
                specification.get<CheckObjectAllocations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, mutableMapOf<IType, Int>())
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, mutableMapOf<IType, Int>())

                    val passed = act.keys.all { it in exp.keys && act[it] == exp[it] }
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp.describe { "${it.value} allocation(s) of ${it.key}" },
                            null,
                            act.describe { "${it.value} allocation(s) of ${it.key}" }
                        )
                    )
                }

                // Check array allocations
                specification.get<CheckArrayAllocations>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, mutableMapOf<IType, Int>())
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, mutableMapOf<IType, Int>())

                    val passed = act.keys.all { it in exp.keys && act[it] == exp[it] }
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp.describe { "${it.value} allocation(s) of ${it.key}" },
                            null,
                            act.describe { "${it.value} allocation(s) of ${it.key}" }
                        )
                    )
                }

                // Check array read accesses
                specification.get<CountArrayReadAccesses>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp,
                            parameter.margin,
                            act
                        )
                    )
                }

                // Check array write accesses
                specification.get<CountArrayWriteAccesses>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp,
                            parameter.margin,
                            act
                        )
                    )
                }

                // Check memory usage
                specification.get<CountMemoryUsage>()?.let { parameter ->
                    val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
                    val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp,
                            parameter.margin,
                            act
                        )
                    )
                }

                // Check recursive calls
                // TODO getAll could be used for more stuff when recursion is at play
                specification.get<CountRecursiveCalls>()?.let { parameter ->
                    val exp = listener.getAll<Int>(referenceProcedure, parameter::class).sum()
                    val act = listener.getAll<Int>(subjectProcedure, parameter::class).sum()

                    val passed = act.inRange(exp, parameter.margin)
                    results.add(
                        TestResult(
                            passed,
                            subjectProcedure,
                            unmodified,
                            parameter.description(),
                            exp,
                            parameter.margin,
                            act
                        )
                    )
                }

                // Check variable states for procedure arguments
                specification.get<TrackParameterStates>()?.let { parameter ->
                    val expectedParamStates = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, List<IValue>>())
                    val actualParamStates = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, List<IValue>>())

                    referenceProcedure.parameters.forEachIndexed { i, param ->
                        val exp = expectedParamStates[param] ?: listOf()
                        val act = actualParamStates[subjectProcedure.parameters[i]] ?: listOf()

                        val passed = act.sameAs(exp)
                        results.add(
                            TestResult(
                                passed,
                                subjectProcedure,
                                unmodified,
                                parameter.description() + " of ${param.id}",
                                exp,
                                null,
                                act
                            )
                        )
                    }
                }

                // Check parameter side effects
                specification.get<CheckSideEffects>()?.let { parameter ->
                    val expectedSideEffects = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, IValue>())
                    val actualSideEffects = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, IValue>())

                    referenceProcedure.parameters.forEachIndexed { i, param ->
                        val exp = expectedSideEffects[param] ?: unmodified[i]
                        val act = actualSideEffects[subjectProcedure.parameters[i]] ?: unmodified[i]

                        val passed = act.sameAs(exp)
                        results.add(
                            TestResult(
                                passed,
                                subjectProcedure,
                                unmodified,
                                parameter.description() + " of ${param.id}",
                                exp,
                                null,
                                act
                            )
                        )
                    }
                }
            }
        }

        return results
    }
}