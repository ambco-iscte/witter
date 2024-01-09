package pt.iscte.witter

import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.*

private const val SEARCH_BINARY = "src/main/java/examples/paper/binarysearch/BinarySearch.java"
private const val SEARCH_LINEAR = "src/main/java/examples/paper/binarysearch/LinearSearch.java"

fun main() {

    // Define test suite using DSL
    val test = Suite(referencePath = SEARCH_BINARY) {
        Stateful(CountLoopIterations()) {
            val x = Var("x") { Call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1) } // x = 0
            Call("search", listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), x) // search(listOf(), 0), expected 0
        }
    }

    // Run tests
    val results = test.apply(SEARCH_LINEAR)

    // Print test results for each tested procedure
    results.forEach { println(it.toString() + "\n") }
}