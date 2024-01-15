package pt.iscte.witter

import pt.iscte.witter.dsl.*


fun main() {
    val ref = "src/test/java/reference/Stack.java"

    val test = Suite(referencePath = ref) {
        Case {
            val stack = Var("x") {
                Object("Stack", 5) {
                    Call("push", 3)
                    Call("push", 5)
                    Call("push", 7)
                }
            }
            stack.Call("size")
            stack.Call("pop")
            stack.Call("size")

            /*
            TODO: dentro do case/TestModule ter blocos de métricas em vez de ter as métricas logo no início para todo o case
            AssertConstantComplexity {

            }
             */
        }
    }

    val results = test.apply(subjectPath = "src/test/java/submission/Stack.java")
    results.forEach { println("$it\n") }
}