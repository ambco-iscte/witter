package tester

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.Call
import pt.iscte.witter.dsl.Stateless
import pt.iscte.witter.dsl.Suite
import pt.iscte.witter.tsl.CheckSideEffects
import pt.iscte.witter.tsl.CountLoopIterations
import pt.iscte.witter.tsl.plus
import kotlin.test.assertEquals

class TestBinarySearch: BaseTest(
    "src/test/java/reference/BinarySearch.java",
    "src/test/java/submission/LinearSearch.java"
) {

    private val expected = """
[pass] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected result: 0

[fail] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected loop iterations: 3
	Found: 1

[pass] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected side effects of a: [1, 2, 3, 4, 5, 6, 7]

[pass] search([1, 2, 3, 4, 5, 6, 7], 1)
	Expected side effects of e: 1

[pass] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected result: -1

[fail] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected loop iterations: 4
	Found: 8

[pass] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected side effects of a: [1, 3, 7, 9, 11, 13, 17, 19]

[pass] search([1, 3, 7, 9, 11, 13, 17, 19], 18)
	Expected side effects of e: 18""".trimIndent()

    @Test
    fun testTSL() {
        val results = tester.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Stateless(CountLoopIterations() + CheckSideEffects) {
                Call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1)
                Call("search", listOf(1, 3, 7, 9, 11, 13, 17, 19), 18)
            }
        }

        val results = dsl.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }
}