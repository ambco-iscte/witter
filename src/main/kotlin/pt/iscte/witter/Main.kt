package pt.iscte.witter

import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.AssertConstantComplexity


fun main() {
    val test = Suite(referencePath = "src/test/java/reference/Stack.java") {
        Case {
            val stack = ref("x") {
                new("Stack", 5) {
                    call("push", 3)
                    call("push", 5)
                    call("push", 7)
                }
            }

            using(AssertConstantComplexity) {
                call("size", stack)
                call("pop", stack)
                call("size", stack)
            }

            // TODO:
            //  VariableReference.call adds ProcedureCall to VariableReference's TestCase
            //  How do I make it so calling VariableReference.call inside a using() TestCase
            //  adds the call to that block/case and not to VariableReference's TestCase?
            /*
            using(AssertConstantComplexity) {
                stack.call("size")
                stack.call("pop")
                stack.call("size")
            }
             */
        }
    }

    val results = test.apply(subjectPath = "src/test/java/submission/Stack.java")
    results.forEach { println("$it\n") }
}