import pt.iscte.strudel.model.INT
import pt.iscte.strudel.vm.IVirtualMachine
import testing.StaticMethodTest
import java.io.File

import testing.Tester

fun main() {
    // Create strudel VM
    val vm: IVirtualMachine = IVirtualMachine.create()

    // Prepare method arguments
    val alreadySorted = vm.allocateArrayOf(INT, 1, 2, 3, 4, 5)
    val inverted = vm.allocateArrayOf(INT, 5, 4, 3, 2, 1)
    val singleNumber = vm.allocateArrayOf(INT, 1)

    // Initialise tester
    val tester = Tester(reference = File("src/main/java/Reference.java"))
        .countIterations()
        .countArrayAllocations()
        .countArrayAssignments()
        .countVariableAssignments()

    // Specify method tests
    val tests = listOf(
        StaticMethodTest("sum", alreadySorted),
        StaticMethodTest("sum", inverted),
        StaticMethodTest("sum", singleNumber)
    )

    // Run tests
    val results = tester.evaluate(vm, File("src/main/java/Subject.java"), tests)

    // Print test results
    results.keys.forEach {
        test -> results[test]!!.forEach {
            println("[$test]: ${it.message}\n")
        }
    }
}