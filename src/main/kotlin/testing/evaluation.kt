package testing

sealed class Evaluation(val message: String)

data class IncorrectInvocationResult(val method: String, val args: Collection<Any?>, val expected: Any?, val actual: Any?):
    Evaluation("Result of invocation $method(${args.joinToString(", ")}) does not match reference solution" +
            "\n\texpected: $expected\n\tgot: $actual")

data class TooManyIterations(val method: String, val args: Collection<Any?>, val expected: Int, val actual: Int):
    Evaluation("Too many iterations for invocation $method(${args.joinToString(", ")}): reference iterated " +
            "$expected times, but subject iterated $actual times")

data class TooManyArrayAllocations(val method: String, val args: Collection<Any?>, val expected: Int, val actual: Int):
    Evaluation("Too many array allocations for invocation $method(${args.joinToString(", ")})" +
            "\n\texpected: $expected\n\tgot: $actual")

data class TooManyArrayAssignments(val method: String, val args: Collection<Any?>, val expected: Int, val actual: Int):
    Evaluation("Too many array assignments for invocation $method(${args.joinToString(", ")})" +
            "\n\texpected: $expected\n\tgot: $actual")

data class TooManyVariableAssignments(val method: String, val args: Collection<Any?>, val expected: Int, val actual: Int):
    Evaluation("Too many variable assignments for invocation $method(${args.joinToString(", ")})" +
            "\n\texpected: $expected\n\tgot: $actual")