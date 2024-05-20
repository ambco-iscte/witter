package tester

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.Case
import pt.iscte.witter.dsl.TestSuite
import pt.iscte.witter.dsl.call
import pt.iscte.witter.tsl.CountLoopIterations
import java.io.File

class TestAverage : BaseTest(
    "src/test/java/reference/Average.java",
    "src/test/java/submission/Average.java"
) {

    @Test
    fun test() {
        val tests = TestSuite(reference) {
            Case(CountLoopIterations()) {
                call("average", listOf(1,2,3,4,5), expected = 3.0)
                call("average", listOf(0,2,3,5,7), expected = 3.4)
            }
        }
        tests.apply(subject).forEach { println("$it\n") }
    }
}