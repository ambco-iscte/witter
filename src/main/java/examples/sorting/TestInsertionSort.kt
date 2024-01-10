package examples.sorting

import pt.iscte.witter.testing.Test

fun main() {
    val test = Test("src/main/java/examples/sorting/InsertionSort.java")
    val results = test.apply("src/main/java/examples/sorting/SelectionSort.java")
    results.forEach { println(it.toString() + "\n") }
}