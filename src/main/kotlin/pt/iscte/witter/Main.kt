package pt.iscte.witter

import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.CountArrayReadAccesses
import pt.iscte.witter.tsl.CountLoopIterations
import pt.iscte.witter.tsl.CountRecursiveCalls
import pt.iscte.witter.tsl.plus


fun main() {
    // TODO: DSL getProcedure by name AND also by parameter count (for now, until better overloading is done)
    //  change TestingUtils matchesSignature function to do the same thing as Extensions matches
    //  Might be enough to support Binary Search Tree which would be pretty cool

    val test = TestSuite(referencePath = "src/test/java/reference/ArrayList.java") {
        Case("testContains") {
            // Create new object and store a reference to it
            val list = ref { new("ArrayList", 3) }

            // Executed without white-box metrics (black-box only)
            list.call("size", expected = 0)
            list.call("add", "hello")
            list.call("size", expected = 1)
            list.call("add", "world")
            list.call("size", expected = 2)

            using(CountLoopIterations() + CountArrayReadAccesses()) {
                // These calls compare loop iterations
                call("add", list, "algorithm")
                call("size", list, expected = 3)
            }
        }

        // All the calls within this case compare loop iterations
        Case(CountLoopIterations(), "testIsEmpty") {
            val list = ref { new("ArrayList", 3) }
            list.call("isEmpty", expected = true)
            list.call("add", "hello")
            list.call("add", "icpec")
            list.call("isEmpty", expected = false)
        }
    }

    val results = test.apply(subjectPath = "src/test/java/submission/ArrayList.java")
    results.forEach { println("$it\n") }
}