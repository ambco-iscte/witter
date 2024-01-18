package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.StrudelUnsupported
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.model.IType
import pt.iscte.strudel.vm.IRecord
import pt.iscte.strudel.vm.IReference
import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.tsl.IStatement
import pt.iscte.witter.tsl.ITestMetric
import java.io.File

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

private val Throwable.msg: String
    get() = message ?: cause?.message ?: "No exception message was provided."

sealed interface ITestResult {
    val message: String
}

data class FileLoadingError(val file: File, val cause: Throwable): ITestResult {

    override val message: String = when (cause) {
        is StrudelUnsupported -> "${cause::class.simpleName} occurred when parsing source code file $file: ${cause.msg}\n\tat: ${cause.locations.joinToString()}"
        else -> "${cause::class.simpleName} occurred when loading source code file $file:\n\t${cause.msg}"
    }

    override fun toString(): String = "[fail] $message"
}

data class ExceptionThrown(val stmt: IStatement, val exception: Exception): ITestResult {

    override val message: String =
        if (exception.cause == null)
            "${exception::class.simpleName} thrown when executing statement $stmt: ${exception.msg}"
        else
            "${exception.cause!!::class.simpleName} thrown when executing statement $stmt: ${exception.cause!!.msg}"

    override fun toString(): String = "[fail] $message"
}

data class ProcedureNotImplemented(val procedure: IProcedure): ITestResult {

    override val message: String
        get() = "Procedure ${procedure.id}(${procedure.parameters.joinToString {
            "${it.type}"
        }}) not implemented."

    override fun toString(): String = "[fail] $message"
}

open class TestResult(
    open val passed: Boolean,
    open val procedure: IProcedure,
    open val args: Iterable<IValue>,
    open val expected: IValue,
    open val margin: Number,
    open val actual: IValue
): ITestResult {
    override val message: String
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