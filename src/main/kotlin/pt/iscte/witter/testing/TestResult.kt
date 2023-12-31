package pt.iscte.witter.testing

import pt.iscte.strudel.model.IProcedure

sealed interface ITestResult

data class TestResult(
    val passed: Boolean,
    val procedure: IProcedure,
    val args: Iterable<Any?>,
    val metricName: String,
    val expected: Any?,
    val margin: Any?,
    val actual: Any?
): ITestResult {

    override fun toString(): String =
        "[${if (passed) "pass" else "fail"}] ${procedure.id}(${args.joinToString(", ")})\n" +
                "\tExpected $metricName: $expected ${if (margin != null && margin != 0) "(± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}