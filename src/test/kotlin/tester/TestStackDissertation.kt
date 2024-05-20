package tester

import assertEquivalent
import org.junit.jupiter.api.Test
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.witter.dsl.*
import pt.iscte.witter.testing.ITestResult
import pt.iscte.witter.testing.TestResult
import pt.iscte.witter.testing.TestSuite
import pt.iscte.witter.testing.WhiteBoxTestResult
import pt.iscte.witter.tsl.CountArrayReadAccesses
import pt.iscte.witter.tsl.CountLoopIterations
import pt.iscte.witter.tsl.plus
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestStackDissertation {

    private val reference = "src/test/java/reference/StackDissertation.java"
    private val subject = "src/test/java/submission/StackDissertation.java"

    private fun assert(results: List<ITestResult>) {
        results.forEach { println(it) }

        assertEquals(3, results.size)

        assertIs<TestResult>(results[0])
        assertIs<WhiteBoxTestResult>(results[1])
        assertIs<WhiteBoxTestResult>(results[2])

        assertTrue((results[0] as TestResult).passed)
        assertFalse((results[1] as WhiteBoxTestResult).passed)
        assertFalse((results[2] as WhiteBoxTestResult).passed)

        assertEquivalent(3, (results[0] as TestResult).expected)
        assertEquivalent(3, (results[0] as TestResult).actual)

        assertEquivalent(0, (results[1] as WhiteBoxTestResult).expected)
        assertEquivalent(5, (results[1] as WhiteBoxTestResult).actual)

        assertEquivalent(0, (results[2] as WhiteBoxTestResult).expected)
        assertEquivalent(5, (results[2] as WhiteBoxTestResult).actual)
    }

    @Test
    fun testDSL() {
        val dsl = TestSuite(reference) {
            Case {
                val stack = ref { new("StackDissertation", 5) }

                call("push", stack, 1)
                call("push", stack, 2)
                call("push", stack, 3)

                using (CountLoopIterations() + CountArrayReadAccesses()) {
                    call("size", stack, expected = 3)
                }
            }
        }
        assert(dsl.apply(subject))
    }
}