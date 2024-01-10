package tester

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.Call
import pt.iscte.witter.dsl.Stateless
import pt.iscte.witter.dsl.Suite
import pt.iscte.witter.tsl.CheckSideEffects
import pt.iscte.witter.tsl.CountArrayReadAccesses
import pt.iscte.witter.tsl.CountArrayWriteAccesses
import pt.iscte.witter.tsl.plus
import kotlin.test.assertEquals

class TestInsertionSort: BaseTest(
    "src/test/java/reference/InsertionSort.java",
    "src/test/java/submission/SelectionSort.java"
) {

    private val expected = """
[fail] sort([5, 4, 3, 2, 1])
	Expected array reads: 40
	Found: 28

[fail] sort([5, 4, 3, 2, 1])
	Expected array writes: 20
	Found: 8

[pass] sort([5, 4, 3, 2, 1])
	Expected side effects of a: [5, 4, 3, 2, 1]""".trimIndent()

    @Test
    fun testTSL() {
        val results = tester.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Stateless(CountArrayReadAccesses() + CountArrayWriteAccesses() + CheckSideEffects) {
                Call("sort", listOf(5, 4, 3, 2, 1))
            }
        }

        val results = dsl.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }
}