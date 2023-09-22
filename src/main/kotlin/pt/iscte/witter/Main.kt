package pt.iscte.witter

import pt.iscte.witter.testing.Test

private const val SORTING_INSERTION = "src/main/java/examples/paper/sorting/InsertionSort.java"
private const val SORTING_SELECTION = "src/main/java/examples/paper/sorting/SelectionSort.java"

private const val SEARCH_BINARY = "src/main/java/examples/paper/binarysearch/BinarySearch.java"
private const val SEARCH_LINEAR = "src/main/java/examples/paper/binarysearch/LinearSearch.java"

private const val FACTORIAL_RECURSIVE = "src/main/java/examples/paper/factorial/RecursiveFactorial.java"
private const val FACTORIAL_ITERATIVE = "src/main/java/examples/paper/factorial/IterativeFactorial.java"

fun main() {
    // Initialise tester
    val test = Test(FACTORIAL_RECURSIVE)

    // Run tests
    val results = test.execute(FACTORIAL_ITERATIVE)

    // Print test results for each tested procedure
    results.forEach { println(it.toString() + "\n") }
}