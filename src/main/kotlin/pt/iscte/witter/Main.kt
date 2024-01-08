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
    val test = Test(referenceFilePath = SEARCH_BINARY)

    // Define test suite using DSL
    val testSuite = Suite(referencePath = SEARCH_BINARY) {
        Stateful(CountLoopIterations() + CheckSideEffects) {
            val x = Var("x") { Call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1) } // x = 0
            Call("search", listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), x) // search(listOf(), 0), expected 0
        }

        /*
        Stateful(CountRecursiveCalls() + CheckArrayAllocations) {
            val t = Var("t") {
                Object("Stack") {
                    Call("push", 1)
                    Call("push", 2)
                    Call("push", 3)
                }
            }

            Call("contains", 3)
            Call("add", 3)
            Call("contains", 3)

            Var("x") { Call("get", 2) }
        }
         */
    }

    // Run tests
    val results = test.apply(SEARCH_LINEAR, testSuite)

    // Print test results for each tested procedure
    results.forEach { println(it.toString() + "\n") }
}