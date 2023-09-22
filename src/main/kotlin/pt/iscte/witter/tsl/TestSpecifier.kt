package pt.iscte.witter.tsl

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import TSLParser
import TSLLexer

object TestSpecifier {

    // Splits strings into tokens separated by commas, but only considering commas NOT enclosed in (), [], or {}.
    private val ARGUMENT_SPLIT_REGEX: Regex = ",(?![^(\\[{]*[)\\]}])".toRegex()

    fun translate(procedure: IProcedure): ProcedureTestSpecification? =
        procedure.documentation?.let { comment ->
            val annotation = comment.lineSequence().map { it.trim() }.joinToString(System.lineSeparator())
            TSLParser(CommonTokenStream(TSLLexer(CharStreams.fromString(annotation)))).specification().translate(procedure)
        }

    fun parseArgumentsString(vm: IVirtualMachine, str: String): List<IValue> =
        str.split(ARGUMENT_SPLIT_REGEX).map { JavaArgument2Strudel(vm).translate(it.trim()) }

    private fun TSLParser.SpecificationContext.translate(procedure: IProcedure): ProcedureTestSpecification {
        val cases: MutableList<String> = mutableListOf()
        val parameters: MutableList<ITestMetric> = mutableListOf()

        annotation().forEach { annotation ->
            when (annotation) {
                is TSLParser.TestCaseAnnotationContext -> cases.add(annotation.args.text)
                else -> parameters.add(annotation.translate())
            }
        }

        return ProcedureTestSpecification(procedure, cases.toList(), parameters.toSet())
    }

    private fun TSLParser.AnnotationContext.translate(): ITestMetric = when (this) {
        is TSLParser.CountLoopIterationsContext -> CountLoopIterations(margin?.text?.toInt() ?: 0)
        is TSLParser.CountObjectAllocationsContext -> CheckObjectAllocations
        is TSLParser.CountArrayAllocationsContext -> CheckArrayAllocations
        is TSLParser.CountArrayReadAccessesContext -> CountArrayReadAccesses(margin?.text?.toInt() ?: 0)
        is TSLParser.CountArrayWriteAccessesContext -> CountArrayWriteAccesses(margin?.text?.toInt() ?: 0)
        is TSLParser.CountMemoryUsageContext -> CountMemoryUsage(margin?.text?.toInt() ?: 0)
        is TSLParser.TrackVariableStatesContext -> TrackParameterStates
        is TSLParser.CheckParameterImmutabilityContext -> CheckSideEffects
        is TSLParser.CountRecursiveCallsContext -> CountRecursiveCalls(margin?.text?.toInt() ?: 0)
        else -> throw Exception("")
    }
}