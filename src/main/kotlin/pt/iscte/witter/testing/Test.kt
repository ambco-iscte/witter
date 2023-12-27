package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.witter.tsl.*
import java.io.File

class Test(referenceFilePath: String) {
    constructor(referenceFile: File) : this(referenceFile.path)

    private val loader = Java2Strudel()

    fun execute(file: File): List<ITestResult> = execute(loader.load(file))

    fun execute(filePath: String): List<ITestResult> = execute(loader.load(File(filePath)))

    fun execute(suite: TestSuite): List<ITestResult> = execute(loader.load(File(suite.subjectPath)), suite.modules())

    private fun execute(subject: IModule, tests: List<TestModule> = listOf()): List<ITestResult> {
        val results = mutableListOf<ITestResult>()

        (tests.ifEmpty { subject.tests }).forEach { test ->
            when(test) {
                is SingleProcedureTestSuite -> {
                    val referenceProcedure = test.procedure // TODO: find a way to include alternative solutions
                    val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)

                    val builder = ResultBuilder(referenceProcedure, subjectProcedure)

                    test.cases().forEach { arguments ->
                        val vm = IVirtualMachine.create()

                        // Allocate arguments twice so listeners don't end up attached to the same record or array
                        val (unmodified, args, argsCopy) =
                            if (test.parsed) getArgumentsFromValues(vm, arguments as List<Any?>)
                            else getArgumentsFromString(vm, arguments.toString())

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

                is StatefulTestSequence -> { // TODO: test
                    val vm = IVirtualMachine.create() // Stateful tests - one VM for all calls in sequence
                    val listener = EvaluationMetricListener(vm, test)
                    test.calls().forEach { (referenceProcedure, arguments) ->
                        val (unmodified, args, argsCopy) = getArgumentsFromValues(vm, arguments)
                        val subjectProcedure = subject.getProcedure(referenceProcedure.id!!)

                        val builder = ResultBuilder(referenceProcedure, subjectProcedure)

                        // BLACK-BOX
                        val expected = vm.execute(referenceProcedure, *args.toTypedArray())?.value
                        val actual = vm.execute(subjectProcedure, *argsCopy.toTypedArray())?.value
                        builder.black(expected, actual, unmodified)?.let { results.add(it) }

                        // WHITE-BOX
                        results.addAll(builder.white(listener, unmodified))
                    }
                }
            }
        }

        return results
    }
}