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
import pt.iscte.witter.tsl.CountLoopIterations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBinarySearch: BaseTest(
    "src/test/java/reference/BinarySearch.java",
    "src/test/java/submission/LinearSearch.java"
) {

    private fun assert(results: List<ITestResult>) {
        assertEquals(4, results.size)

        assertTrue(results[0] is TestResult)
        assertTrue(results[1] is WhiteBoxTestResult)
        assertTrue(results[2] is TestResult)
        assertTrue(results[3] is WhiteBoxTestResult)

        assertTrue((results[0] as TestResult).passed)
        assertFalse((results[1] as WhiteBoxTestResult).passed)
        assertTrue((results[2] as TestResult).passed)
        assertFalse((results[3] as WhiteBoxTestResult).passed)

        assertTrue((results[1] as WhiteBoxTestResult).metric is CountLoopIterations)
        assertTrue((results[3] as WhiteBoxTestResult).metric is CountLoopIterations)

        assertEquivalent(0, (results[0] as TestResult).expected)
        assertEquivalent(3, (results[1] as WhiteBoxTestResult).expected)
        assertEquivalent(-1, (results[2] as TestResult).expected)
        assertEquivalent(4, (results[3] as WhiteBoxTestResult).expected)

        assertEquivalent(1, (results[1] as WhiteBoxTestResult).actual)
        assertEquivalent(8, (results[3] as WhiteBoxTestResult).actual)
    }

    @Test
    fun testTSL() {
        assert(tester.apply(subject))
    }

    @Test
    fun testDSL() {
        val dsl = TestSuite(reference) {
            Case(CountLoopIterations()) {
                call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1)
                call("search", listOf(1, 3, 7, 9, 11, 13, 17, 19), 18)
            }
        }
        assert(dsl.apply(subject))
    }
}