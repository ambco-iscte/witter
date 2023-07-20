package tsl

import TSLLexer
import TSLParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine

object TestSpecifier {

    // I, obviously, did not come up with this myself
    // Splits strings into tokens separated by commas, but only considering commas NOT enclosed in (), [], or {}.
    private val ARGUMENT_SPLIT_REGEX: Regex = ",(?![^(\\[{]*[)\\]}])".toRegex()

    fun translate(procedure: IProcedure): ProcedureTestSpecification? =
        procedure.documentation?.let {
            TSLParser(CommonTokenStream(TSLLexer(CharStreams.fromString(it)))).specification().translate(procedure)
        }

    fun parseArgumentsString(vm: IVirtualMachine, str: String): List<IValue> =
        str.split(ARGUMENT_SPLIT_REGEX).map { JavaArgument2Strudel(vm).translate(it.trim()) }

    private fun TSLParser.SpecificationContext.translate(procedure: IProcedure): ProcedureTestSpecification {
        val cases: MutableList<String> = mutableListOf()
        val parameters: MutableList<ITestParameter> = mutableListOf()

        annotation().forEach { annotation ->
            when (annotation) {
                is TSLParser.TestCaseAnnotationContext -> {
                    val text = annotation.TEST_ARGUMENTS().text
                    cases.add(text.substring(1, text.length - 1))
                }
                else -> parameters.add(annotation.translate())
            }
        }

        return ProcedureTestSpecification(procedure, cases.toList(), parameters.toSet())
    }

    private fun TSLParser.AnnotationContext.translate(): ITestParameter = when (this) {
        is TSLParser.CountLoopIterationsContext -> CountLoopIterations(margin.text.toInt())
        is TSLParser.CountObjectAllocationsContext -> CountRecordAllocations(margin.text.toInt())
        is TSLParser.CountArrayAllocationsContext -> CountArrayAllocations(margin.text.toInt())
        is TSLParser.CountArrayReadAccessesContext -> CountArrayReadAccesses(margin.text.toInt())
        is TSLParser.CountArrayWriteAccessesContext -> CountArrayWriteAccesses(margin.text.toInt())
        is TSLParser.CountMemoryUsageContext -> CountMemoryUsage(margin.text.toInt())
        is TSLParser.TrackVariableStatesContext -> TrackParameterStates
        is TSLParser.CheckParameterImmutabilityContext -> CheckParameterMutability
        is TSLParser.CountRecursiveCallsContext -> CountRecursiveCalls(margin.text.toInt())
        else -> throw Exception("")
    }
}