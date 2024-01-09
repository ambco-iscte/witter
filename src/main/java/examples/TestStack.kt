package examples

import pt.iscte.witter.dsl.*

private const val STACK_REFERENCE = "src/main/java/examples/reference/Stack.java"
private const val STACK_SUBMISSION = "src/main/java/examples/submission/Stack.java"

fun main() {
    val test = Suite(referencePath = STACK_REFERENCE) {
        Stateful {
            val t = Var("t") {
                Object("Stack", 5) {
                    Call("push", 1)
                    Call("push", 2)
                    Call("push", 3)
                }
            }
            Call("size", t)
        }
    }

    val results = test.apply(STACK_SUBMISSION)
    results.forEach { println(it.toString() + "\n") }
}