import java.io.File

import testing.Tester

private const val SORTING_INSERTION = "src/main/java/examples/paper/sorting/InsertionSort.java"
private const val SORTING_SELECTION = "src/main/java/examples/paper/sorting/SelectionSort.java"

private const val SEARCH_BINARY = "src/main/java/examples/paper/binarysearch/BinarySearch.java"
private const val SEARCH_LINEAR = "src/main/java/examples/paper/binarysearch/LinearSearch.java"

private const val FACTORIAL_RECURSIVE = "src/main/java/examples/paper/factorial/RecursiveFactorial.java"
private const val FACTORIAL_ITERATIVE = "src/main/java/examples/paper/factorial/IterativeFactorial.java"

fun main() {
    // Initialise tester
    val tester = Tester(reference = File(FACTORIAL_RECURSIVE))

    // Run tests
    val results = tester.execute(file = File(FACTORIAL_ITERATIVE))

    // Print test results for each tested procedure
    results.keys.forEach { procedure ->
        results[procedure]!!.forEach {
            println("[${procedure.id}]: ${it.message}\n")
        }
    }
}