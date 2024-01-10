package tester

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.*
import kotlin.test.assertEquals

class TestStack {

    private val reference = "src/test/java/reference/Stack.java"
    private val subject = "src/test/java/submission/Stack.java"

    private val expected = """
[fail] size(Stack(stack=[1, 2, 3, 0, 0], size=3))
	Expected result: 3
	Found: 0
        """.trimIndent()

    @Test
    fun testDSL() {
        val dsl = Suite(reference) {
            Stateful {
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

        val results = dsl.apply(subject)
        assertEquals(expected, results.joinToString("\n\n"))
    }
}