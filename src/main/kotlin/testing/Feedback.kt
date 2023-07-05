package testing

sealed class Feedback(val message: String)

data class IncorrectInvocationResult(val method: String, val args: Iterable<Any?>, val expected: Any?, val actual: Any?):
    Feedback("Result of invocation $method(${args.joinToString(", ")}) does not match reference solution" +
            "\n\texpected: $expected\n\tgot: $actual")

data class MeasuredValueNotInRange(val description: String, val method: String, val args: Iterable<Any?>, val expected: Int, val margin: Int, val actual: Int):
    Feedback("Too many $description for invocation $method(${args.joinToString(", ")}): " +
            "should have measured $expected ± $margin $description, but measured $actual.")