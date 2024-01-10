package tsl

import assertEquivalent
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.INT
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.witter.testing.EvaluationMetricListener
import pt.iscte.witter.tsl.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class TestTSLParser {

    @Test
    fun parseArgumentsString() {
        val ref = "src/test/java/reference/Stack.java"

        val test = pt.iscte.witter.testing.Test(ref)
        val vm: IVirtualMachine = IVirtualMachine.create()
        val module: IModule = Java2Strudel().load(File(ref))
        val listener = EvaluationMetricListener(
            vm,
            TestModule(module, listOf(), "", setOf(), false)
        )

        val obj = vm.allocateRecord(module.getRecordType("Stack"))
        val constructor = module.getProcedure("\$init")
        vm.execute(constructor, obj, vm.getValue(5))
        val args = listOf(
            vm.getValue(2),
            vm.getValue(-3),
            vm.getValue(5.1),
            vm.getValue(-3.4),
            vm.allocateArrayOf(INT, 1, 2, 3, 4, 5),
            obj
        )

        assertEquivalent(
            args,
            TestSpecifier.parseArgumentsString(
                test, vm, module, listener,
                "2, -3, 5.1, -3.4, new int[] {1,2,3,4,5}, new Stack(5)"
            )
        )
    }

    @Test
    fun testTranslateDocumentation() {
        val ref = "src/test/java/reference/BinarySearch.java"
        val procedure = Java2Strudel().load(File(ref)).getProcedure("search")

        val module = TestModule(
            procedure.module!!,
            listOf(
                ProcedureCall(procedure, "new int[] { 1, 2, 3, 4, 5, 6, 7 }, 1", false),
                ProcedureCall(procedure, "new int[] { 1, 3, 7, 9, 11, 13, 17, 19 }, 18", false)
            ),
            "",
            CountLoopIterations() + CheckSideEffects,
            false
        )

        assertEquals(TestSpecifier.translate(procedure), module)
    }
}