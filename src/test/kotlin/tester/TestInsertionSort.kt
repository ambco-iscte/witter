package tester

import assertEquivalent
import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.call
import pt.iscte.witter.dsl.Case
import pt.iscte.witter.dsl.Suite
import pt.iscte.witter.testing.ITestResult
import pt.iscte.witter.testing.WhiteBoxTestResult
import pt.iscte.witter.tsl.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestInsertionSort: BaseTest(
    "src/test/java/reference/InsertionSort.java",
    "src/test/java/submission/SelectionSort.java"
) {

    private fun assert(results: List<ITestResult>) {
        assertEquals(3, results.size)

        assertTrue(results[0] is WhiteBoxTestResult)
        assertTrue(results[1] is WhiteBoxTestResult)
        assertTrue(results[2] is WhiteBoxTestResult)

        assertFalse((results[0] as WhiteBoxTestResult).passed)
        assertFalse((results[1] as WhiteBoxTestResult).passed)
        assertTrue((results[2] as WhiteBoxTestResult).passed)

        assertTrue((results[0] as WhiteBoxTestResult).metric is CountArrayReadAccesses)
        assertTrue((results[1] as WhiteBoxTestResult).metric is CountArrayWriteAccesses)
        assertTrue((results[2] as WhiteBoxTestResult).metric is CheckSideEffects)

        assertEquivalent(40, (results[0] as WhiteBoxTestResult).expected)
        assertEquivalent(20, (results[1] as WhiteBoxTestResult).expected)
        assertEquivalent(listOf(5, 4, 3, 2, 1), (results[2] as WhiteBoxTestResult).expected)

        assertEquivalent(28, (results[0] as WhiteBoxTestResult).actual)
        assertEquivalent(8, (results[1] as WhiteBoxTestResult).actual)
    }

    @Test
    fun testTSL() {
        assert(tester.apply(subject))
    }

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Case(CountArrayReadAccesses() + CountArrayWriteAccesses() + CheckSideEffects) {
                call("sort", listOf(5, 4, 3, 2, 1))
            }
        }
        assert(dsl.apply(subject))
    }
}