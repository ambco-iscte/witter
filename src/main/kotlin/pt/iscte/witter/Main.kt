package pt.iscte.witter

import pt.iscte.witter.dsl.*

fun main() {
    val test = Suite("src/test/java/reference/Stack.java") {
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
    val results = test.apply("src/test/java/submission/Stack.java")
    results.forEach { println(it.toString() + "\n") }
}