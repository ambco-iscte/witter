package testing

import pt.iscte.strudel.model.IParameter

sealed class Feedback(val message: String)

data class IncorrectInvocationResult(val method: String, val args: Iterable<Any?>, val expected: Any?, val actual: Any?):
    Feedback("Result of invocation $method(${args.joinToString(", ")}) does not match reference solution" +
            "\n\texpected: $expected\n\tgot: $actual"
    )

data class MeasuredValueNotInRange(val description: String, val method: String, val args: Iterable<Any?>, val expected: Int, val margin: Int, val actual: Int):
    Feedback("Incorrect count of $description for invocation $method(${args.joinToString(", ")}): " +
            "should have measured $expected ± $margin $description, but measured $actual."
    )

private fun parameterMutabilityMessage(changed: Boolean, shouldHaveChanged: Boolean): String =
    if (changed && !shouldHaveChanged) "method modified at least one of its parameters, but it shouldn't have"
    else if (!changed && shouldHaveChanged) "method did not modify any its parameters, but it should have"
    else "" // Never happens

data class InconsistentParameterMutability(val method: String, val args: Iterable<Any?>, val expected: Boolean, val actual: Boolean):
        Feedback("Incorrect parameter mutability for invocation $method(${args.joinToString(", ")}): " +
                parameterMutabilityMessage(actual, expected) + "."
        )

data class InconsistentArgumentStates(val method: String, val args: Iterable<Any?>, val parameter: IParameter, val expected: List<Any?>, val actual: List<Any?>):
        Feedback("Inconsistent states for parameter $parameter during execution of $method(${args.joinToString(", ")}):" +
                "\n\texpected: ${expected.joinToString(", ")}\n\tgot: ${actual.joinToString(", ")}")