package tester

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.Call
import pt.iscte.witter.dsl.Stateless
import pt.iscte.witter.dsl.Suite
import pt.iscte.witter.tsl.CountRecursiveCalls
import kotlin.test.assertEquals

class TestRecursiveFactorial: BaseTest(
    "src/test/java/reference/RecursiveFactorial.java",
    "src/test/java/submission/IterativeFactorial.java"
) {

    private val expected = """
[fail] factorial(5)
	Expected result: 120
	Found: 0

[fail] factorial(5)
	Expected recursive calls: 4 (± 1)
	Found: 0""".trimIndent()

    @Test
    fun testTSL() {
        val results = tester.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Stateless(CountRecursiveCalls(1)) {
                Call("factorial", 5)
            }
        }

        val results = dsl.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }
}