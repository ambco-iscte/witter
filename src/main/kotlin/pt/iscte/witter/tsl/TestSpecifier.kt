package pt.iscte.witter.tsl

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import TSLParser
import TSLLexer
import pt.iscte.strudel.model.IModule
import pt.iscte.witter.testing.EvaluationMetricListener
import pt.iscte.witter.testing.Test

object TestSpecifier {

    // Splits strings into tokens separated by commas, but only considering commas NOT enclosed in (), [], or {}.
    private val ARGUMENT_SPLIT_REGEX: Regex = ",(?![^(\\[{]*[)\\]}])".toRegex()

    fun translate(procedure: IProcedure): TestCaseStatement? =
        procedure.documentation?.let { comment ->
            val annotation = comment.lineSequence().map { it.trim() }.joinToString(System.lineSeparator())
            TSLParser(CommonTokenStream(TSLLexer(CharStreams.fromString(annotation)))).specification().translate(procedure)
        }

    fun javaToStrudel(
        tester: Test,
        vm: IVirtualMachine,
        module: IModule,
        listener: EvaluationMetricListener,
        str: String
    ): List<IValue> = str.split(ARGUMENT_SPLIT_REGEX).map {
        JavaArgument2Strudel(tester, vm, module, listener).translate(it.trim())
    }

    fun javaToKotlin(str: String): List<Any?> =
        str.split(ARGUMENT_SPLIT_REGEX).map { Java2Kotlin().translate(it.trim()) }

    private fun TSLParser.SpecificationContext.translate(procedure: IProcedure): TestCaseStatement {
        val instructions: MutableList<IStatement> = mutableListOf()
        val metrics: MutableList<ITestMetric> = mutableListOf()

        annotation().forEach { annotation ->
            when (annotation) {
                is TSLParser.TestCaseAnnotationContext -> instructions.add(
                    ProcedureCall(procedure, javaToKotlin(annotation.args.text), setOf())
                )
                else -> metrics.add(annotation.translate())
            }
        }

        return TestCaseStatement(procedure.module!!, instructions, "", metrics.toSet())
    }

    private fun TSLParser.AnnotationContext.translate(): ITestMetric = when (this) {
        is TSLParser.CountLoopIterationsContext -> CountLoopIterations(margin?.text?.toInt() ?: 0)
        is TSLParser.CountObjectAllocationsContext -> CheckObjectAllocations
        is TSLParser.CountArrayAllocationsContext -> CheckArrayAllocations
        is TSLParser.CountArrayReadAccessesContext -> CountArrayReadAccesses(margin?.text?.toInt() ?: 0)
        is TSLParser.CountArrayWriteAccessesContext -> CountArrayWriteAccesses(margin?.text?.toInt() ?: 0)
        is TSLParser.CountMemoryUsageContext -> CountMemoryUsage(margin?.text?.toInt() ?: 0)
        is TSLParser.TrackArgumentStatesContext -> TrackParameterStates(parameterID.text)
        is TSLParser.CheckParameterImmutabilityContext -> CheckSideEffects
        is TSLParser.CountRecursiveCallsContext -> CountRecursiveCalls(margin?.text?.toInt() ?: 0)
        else -> throw Exception("")
    }
}