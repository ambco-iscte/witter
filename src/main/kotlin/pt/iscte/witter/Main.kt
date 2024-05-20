package pt.iscte.witter

import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.CountLoopIterations
import pt.iscte.witter.tsl.CountRecursiveCalls
import pt.iscte.witter.tsl.plus


fun main() {
    val test = TestSuite(referencePath = "src/test/java/reference/Stack.java") {
        Case {
            val stack = ref("x") {
                new("Stack", 5) {
                    call("push", 3)
                    call("push", 5)
                    call("push", 7)
                }
            }

            using(CountLoopIterations() + CountRecursiveCalls()) {
                call("size", stack)
                call("pop", stack)
                call("size", stack)
            }
        }
    }

    val results = test.apply(subjectPath = "src/test/java/submission/Stack.java")
    results.forEach { println("$it\n") }
}