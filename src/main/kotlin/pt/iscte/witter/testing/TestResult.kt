package pt.iscte.witter.testing

import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.vm.IRecord
import pt.iscte.strudel.vm.IReference

sealed interface ITestResult

fun Iterable<Any?>.pretty(): String = joinToString {
    when (it) {
        null -> "null"
        is IReference<*> -> when(val record = it.target) {
            is IRecord -> {
                val properties = record.properties()
                "${it.target.type.id}(${properties.describe { property ->
                    "${property.key.id}=${property.value}"
                }})"
            }
            else -> it.target.toString()
        }
        else -> it.toString()
    }
}

data class ProcedureNotImplemented(val procedure: IProcedure): ITestResult {
    override fun toString(): String = "[fail] Procedure ${procedure.id}(${procedure.parameters.joinToString { 
        "${it.type}" 
    }}) not implemented."
}

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
        "[${if (passed) "pass" else "fail"}] ${procedure.id}(${args.pretty()})\n" +
                "\tExpected $metricName: $expected${if (margin != null && margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}