package tester

import assertEquivalent
import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.*
import pt.iscte.witter.testing.ITestResult
import pt.iscte.witter.testing.TestResult
import pt.iscte.witter.testing.TestSuite
import pt.iscte.witter.tsl.CountArrayReadAccesses
import pt.iscte.witter.tsl.CountLoopIterations
import pt.iscte.witter.tsl.plus
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class TestStack {

    private val reference = "src/test/java/reference/Stack.java"
    private val subject = "src/test/java/submission/Stack.java"

    private fun assert(results: List<ITestResult>) {
        println(results)

        assertEquals(1, results.size)

        assertIs<TestResult>(results[0])

        assertFalse((results[0] as TestResult).passed)

        assertEquivalent(3, (results[0] as TestResult).expected)
        assertEquivalent(0, (results[0] as TestResult).actual)
    }

    @Test
    fun testDSL() {
        val dsl = TestSuite(reference) {
            Case {
                val stack = ref {
                    new("Stack", 5) {
                        call("push", 1)
                        call("push", 2)
                        call("push", 3)
                    }
                }
                call("size", stack, expected = 3)
            }
        }
        //println(dsl)
        assert(dsl.apply(subject))
    }
}