package tsl

import assertEquivalent
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.INT
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.witter.testing.EvaluationMetricListener
import pt.iscte.witter.tsl.*
import reference.Stack
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTSLParser {

    @Test
    fun parseArgumentsString() {
        val args = listOf(2, -3, 5.1, -3.4, listOf(1, 2, 3, 4, 5))

        assertEquivalent(
            args,
            TestSpecifier.javaToKotlin("2, -3, 5.1, -3.4, new int[] {1,2,3,4,5}")
        )
    }

    @Test
    fun testTranslateDocumentation() {
        val ref = "src/test/java/reference/BinarySearch.java"
        val procedure = Java2Strudel().load(File(ref)).getProcedure("search")

        val case = TestCaseStatement(
            procedure.module!!,
            listOf(
                ProcedureCall(procedure, listOf(listOf(1, 2, 3, 4, 5, 6, 7), 1)),
                ProcedureCall(procedure, listOf(listOf(1, 3, 7, 9, 11, 13, 17, 19), 18))
            ),
            "",
            setOf(CountLoopIterations())
        )

        val translated: TestCaseStatement? = TestSpecifier.translate(procedure)

        assertNotNull(translated)
        assertEquals(translated.module, case.module)
        assertEquals(translated.statements(), case.statements())
        assertEquals(translated.description, case.description)
        assertEquals(translated.metrics, case.metrics)
    }
}