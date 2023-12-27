package pt.iscte.witter

import pt.iscte.witter.dsl.*
import pt.iscte.witter.testing.Test
import pt.iscte.witter.tsl.*

private const val SORTING_INSERTION = "src/main/java/examples/paper/sorting/InsertionSort.java"
private const val SORTING_SELECTION = "src/main/java/examples/paper/sorting/SelectionSort.java"

private const val SEARCH_BINARY = "src/main/java/examples/paper/binarysearch/BinarySearch.java"
private const val SEARCH_LINEAR = "src/main/java/examples/paper/binarysearch/LinearSearch.java"

private const val FACTORIAL_RECURSIVE = "src/main/java/examples/paper/factorial/RecursiveFactorial.java"
private const val FACTORIAL_ITERATIVE = "src/main/java/examples/paper/factorial/IterativeFactorial.java"

fun main() {
    // Initialise tester
    val test = Test(FACTORIAL_RECURSIVE)

    // Define test suite using DSL
    val testSuite = Suite(FACTORIAL_RECURSIVE, FACTORIAL_ITERATIVE) {
        Static("binarySearch", CountLoopIterations() + CheckSideEffects) {
            Case(listOf(1, 2, 3, 4, 5, 6, 7), 1)
            Case(listOf(1, 3, 7, 9, 11, 13, 17, 19), 18)
        }

        Sequential(CountRecursiveCalls() + CheckArrayAllocations) {
            Before {

            }

            Case("add", 3)
            Case("add", 4)
            Case("contains", 3)
        }
    }

    // Run tests
    // val results = test.execute(FACTORIAL_ITERATIVE)
    val results = test.execute(testSuite)

    // Print test results for each tested procedure
    results.forEach { println(it.toString() + "\n") }
}