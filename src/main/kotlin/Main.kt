import java.io.File

import testing.Tester

fun main() {
    // Initialise tester
    val tester = Tester(reference = File("src/main/java/Reference.java"))

    // Run tests
    val results = tester.execute(file = File("src/main/java/Subject.java"))

    // Print test results for each tested procedure
    results.keys.forEach { procedure ->
        results[procedure]!!.forEach {
            println("[${procedure.id}]: ${it.message}\n")
        }
    }
}