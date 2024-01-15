package pt.iscte.witter.testing

import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.model.IType
import pt.iscte.strudel.vm.IRecord
import pt.iscte.strudel.vm.IReference
import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.tsl.ITestMetric

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

sealed interface ITestResult

data class ProcedureNotImplemented(val procedure: IProcedure): ITestResult {
    override fun toString(): String = "[fail] Procedure ${procedure.id}(${procedure.parameters.joinToString { 
        "${it.type}" 
    }}) not implemented."
}

open class TestResult(
    open val passed: Boolean,
    open val procedure: IProcedure,
    open val args: Iterable<IValue>,
    open val expected: IValue,
    open val margin: Number,
    open val actual: IValue
): ITestResult {
    open val message: String
        get() = "${procedure.id}(${args.pretty()})\n" +
                "\tExpected: " + "$expected${if (margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""

    override fun toString(): String =
        "[${if (passed) "pass" else "fail"}] $message"
}

open class WhiteBoxTestResult(
    override val passed: Boolean,
    override val procedure: IProcedure,
    override val args: Iterable<IValue>,
    open val metric: ITestMetric,
    override val expected: IValue,
    override val margin: Number,
    override val actual: IValue,
): TestResult(passed, procedure, args, expected, margin, actual) {

    override val message: String
        get() = "${procedure.id}(${args.pretty()})\n" +
                "\tExpected ${metric.description}: " + "$expected${if (margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}

data class AllocationsTestResult(
    override val passed: Boolean,
    override val procedure: IProcedure,
    override val args: Iterable<IValue>,
    val type: IType,
    override val expected: IValue,
    override val margin: Number,
    override val actual: IValue,
    override val metric: ITestMetric
): WhiteBoxTestResult(passed, procedure, args, metric, expected, margin, actual) {

    override val message: String
        get() = "${procedure.id}(${args.pretty()})\n" +
                "\tExpected ${metric.description} of $type: " + "$expected${if (margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}