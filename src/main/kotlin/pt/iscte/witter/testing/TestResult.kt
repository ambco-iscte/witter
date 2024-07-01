package pt.iscte.witter.testing

import pt.iscte.strudel.javaparser.StrudelUnsupportedException
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.model.IType
import pt.iscte.strudel.vm.IRecord
import pt.iscte.strudel.vm.IReference
import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.tsl.CheckSideEffects
import pt.iscte.witter.tsl.IStatement
import pt.iscte.witter.tsl.ITestMetric
import pt.iscte.witter.tsl.ProcedureCall
import java.io.File
import java.util.*
import kotlin.reflect.full.isSuperclassOf

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

private fun Throwable.getRootCause(): Throwable {
    var current = this
    while (current.cause != null)
        current = current.cause!!
    return current
}

private val Throwable.msg: String
    get() = message ?: getRootCause().message ?: "No exception message was provided."

sealed interface ITestResult {
    val passed: Boolean
        get() = false
    val message: String
}

val ITestResult.failed: Boolean
    get() = !passed

val ITestResult.isError: Boolean
    get() = this is FileLoadingError || this is ProcedureNotImplemented

val ITestResult.isBlackbox: Boolean
    get() = !isError && (this !is WhiteBoxTestResult || this.metric is CheckSideEffects)

val ITestResult.isWhitebox: Boolean
    get() = !isError && this is WhiteBoxTestResult && this.metric !is CheckSideEffects

data class FileLoadingError(val file: File, val cause: Throwable): ITestResult {

    override val message: String = when (cause) {
        is StrudelUnsupportedException ->
            "${cause::class.simpleName} occurred when parsing source code file $file at node of type ${cause.getFirstNodeType()?.simpleName}\n\t${cause.msg}\n\tat: \n\t  ${cause.stackTrace.joinToString("\n\t  ")}"
        else ->
            "${cause::class.simpleName} occurred when loading source code file $file:\n\t${cause.msg}\n\tat: \n\t  ${cause.stackTrace.joinToString("\n\t  ")}"
    }

    override fun toString(): String = "[error] $message"
}

data class ProcedureNotImplemented(val procedure: IProcedure): ITestResult {
    override val message: String
        get() = "Procedure ${procedure.id}(${procedure.parameters.joinToString {
            "${it.type} ${it.id}"
        }}) not implemented."

    override fun toString(): String = "[error] $message"

}

data class ExceptionTestResult(
    val procedure: IProcedure,
    val args: Iterable<IValue>,
    val expected: Throwable?,
    val actual: Throwable?
): ITestResult {

    override val passed: Boolean
        get() = expected == actual || (expected != null && actual != null && expected::class.isSuperclassOf(actual::class))

    override val message: String
        get() {
            val exp = if (expected == null) "null" else expected::class.qualifiedName
            val act = if (actual == null) "null" else actual::class.qualifiedName
            return "${procedure.id}(${args.pretty()})\n\tExpected exception: $exp (${expected?.message})\n\tFound: $act (${actual?.message})"
        }

    override fun toString(): String = "[${if (passed) "pass" else "error"}] $message"
}

open class TestResult(
    open val procedureCall: ProcedureCall,
    override val passed: Boolean,
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
    override val procedureCall: ProcedureCall,
    override val passed: Boolean,
    override val procedure: IProcedure,
    override val args: Iterable<IValue>,
    open val metric: ITestMetric,
    override val expected: IValue,
    override val margin: Number,
    override val actual: IValue,
): TestResult(procedureCall, passed, procedure, args, expected, margin, actual) {

    override val message: String
        get() = "${procedure.id}(${args.pretty()})\n" +
                "\tExpected ${metric.description}: " + "$expected${if (margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}

data class AllocationsTestResult(
    override val procedureCall: ProcedureCall,
    override val passed: Boolean,
    override val procedure: IProcedure,
    override val args: Iterable<IValue>,
    val type: IType,
    override val expected: IValue,
    override val margin: Number,
    override val actual: IValue,
    override val metric: ITestMetric
): WhiteBoxTestResult(procedureCall, passed, procedure, args, metric, expected, margin, actual) {

    override val message: String
        get() = "${procedure.id}(${args.pretty()})\n" +
                "\tExpected ${metric.description} of $type: " + "$expected${if (margin != 0) " (± $margin)" else ""}" +
                if (!passed) "\n\tFound: $actual" else ""
}