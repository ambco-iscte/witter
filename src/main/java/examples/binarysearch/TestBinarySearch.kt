package examples.binarysearch

import pt.iscte.witter.testing.Test

fun main() {
    val test = Test("src/main/java/examples/binarysearch/BinarySearch.java")
    val results = test.apply("src/main/java/examples/binarysearch/LinearSearch.java")
    results.forEach { println(it.toString() + "\n") }
}