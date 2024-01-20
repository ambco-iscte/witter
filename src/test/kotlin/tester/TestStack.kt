package tester

import assertEquivalent
import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.*
import pt.iscte.witter.testing.ITestResult
import pt.iscte.witter.testing.TestResult
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestStack {

    private val reference = "src/test/java/reference/Stack.java"
    private val subject = "src/test/java/submission/Stack.java"

    private fun assert(results: List<ITestResult>) {
        println(results)

        assertEquals(1, results.size)

        assertTrue(results[0] is TestResult)

        assertFalse((results[0] as TestResult).passed)

        assertEquivalent(3, (results[0] as TestResult).expected)
        assertEquivalent(0, (results[0] as TestResult).actual)
    }

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Case {
                val stack = ref("x") {
                    new("Stack", 5) {
                        call("push", 1)
                        call("push", 2)
                        call("push", 3)
                    }
                }
                call("size", stack)
            }
        }
        assert(dsl.apply(subject))
    }
}