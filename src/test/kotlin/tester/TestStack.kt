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
                val x = Var("x") {
                    Object("Stack", 5) {
                        Call("push", 1)
                        Call("push", 2)
                        Call("push", 3)
                    }
                }
                Call("size", x)
            }
        }
        assert(dsl.apply(subject))
    }
}