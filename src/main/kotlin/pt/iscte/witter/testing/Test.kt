package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.witter.tsl.*
import java.io.File

class Test(referenceFilePath: String) {
    constructor(referenceFile: File) : this(referenceFile.path)

    private val loader = Java2Strudel()
    private val ref: IModule = loader.load(File(referenceFilePath))

    fun execute(file: File): List<ITestResult> = execute(loader.load(file))

    fun execute(filePath: String): List<ITestResult> = execute(loader.load(File(filePath)))

    fun executeSource(source: String): List<ITestResult> = execute(loader.load(source))

    private fun execute(subject: IModule, tests: List<TestModule> = listOf()): List<ITestResult> {
        val results = mutableListOf<ITestResult>()

        (ref.staticProcedureTests + tests).forEach { test ->
            when(test) {
                is StaticProcedureTest -> {
                    val referenceProcedure = test.procedure // TODO: find a way to include alternative solutions
                    val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)

                    val builder = ResultBuilder(referenceProcedure, subjectProcedure)

                    test.cases.forEach { arguments ->
                        val vm = IVirtualMachine.create()

                        // Allocate arguments twice so listeners don't end up attached to the same record or array
                        val (unmodified, args, argsCopy) = getArguments(vm, arguments)

                        // Create and attach white-box listener
                        val listener = EvaluationMetricListener(vm, test)
                        vm.addListener(listener)

                        // BLACK-BOX
                        val expected = vm.execute(referenceProcedure, *args.toTypedArray())?.value
                        val actual = vm.execute(subjectProcedure, *argsCopy.toTypedArray())?.value
                        builder.black(expected, actual, unmodified)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, unmodified))
                    }
                }

                is SequentialProcedureTest -> {
                    val vm = IVirtualMachine.create() // Stateful tests - one VM for all calls in sequence
                    val listener = EvaluationMetricListener(vm, test)
                    test.calls.forEach { (referenceProcedure, arguments) ->
                        val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)
                        // TODO
                    }
                }
            }
        }

        return results
    }
}