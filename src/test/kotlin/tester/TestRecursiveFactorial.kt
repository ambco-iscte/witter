package tester

import assertEquivalent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.call
import pt.iscte.witter.dsl.Case
import pt.iscte.witter.dsl.TestSuite
import pt.iscte.witter.testing.ITestResult
import pt.iscte.witter.testing.TestResult
import pt.iscte.witter.testing.WhiteBoxTestResult
import pt.iscte.witter.tsl.CountRecursiveCalls
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestRecursiveFactorial: BaseTest(
    "src/test/java/reference/RecursiveFactorial.java",
    "src/test/java/submission/IterativeFactorial.java"
) {

    private fun assert(results: List<ITestResult>) {
        assertEquals(2, results.size)

        assertTrue(results[0] is TestResult)
        assertTrue(results[1] is WhiteBoxTestResult)

        assertFalse((results[0] as TestResult).passed)
        assertFalse((results[1] as WhiteBoxTestResult).passed)

        assertTrue((results[1] as WhiteBoxTestResult).metric is CountRecursiveCalls)
        assertEquals(1, (results[1] as WhiteBoxTestResult).margin)

        assertEquivalent(120, (results[0] as TestResult).expected)
        assertEquivalent(4, (results[1] as WhiteBoxTestResult).expected)

        assertEquivalent(0, (results[0] as TestResult).actual)
        assertEquivalent(0, (results[1] as WhiteBoxTestResult).actual)
    }

    @Test
    fun testTSL() {
        assert(tester.apply(subject))
    }

    @Test
    fun testDSL() {
        val tests = TestSuite(reference) {
            Case(CountRecursiveCalls(1)) {
                call("factorial", 5)
            }
        }
        assert(tests.apply(subject))
    }
}