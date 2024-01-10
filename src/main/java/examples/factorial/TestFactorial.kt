package examples.factorial

import pt.iscte.witter.testing.Test

fun main() {
    val test = Test("src/main/java/examples/factorial/RecursiveFactorial.java")
    val results = test.apply("src/main/java/examples/factorial/IterativeFactorial.java")
    results.forEach { println(it.toString() + "\n") }
}