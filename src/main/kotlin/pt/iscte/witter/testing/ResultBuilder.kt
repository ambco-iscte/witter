package pt.iscte.witter.testing

import pt.iscte.strudel.model.IParameter
import pt.iscte.strudel.model.IProcedure
import pt.iscte.strudel.model.IType
import pt.iscte.strudel.model.VOID
import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.tsl.*
import kotlin.reflect.full.declaredMemberProperties

class ResultBuilder(
    private val referenceProcedure: IProcedure,
    private val subjectProcedure: IProcedure
) {

    private fun ITestMetric.margin(): Int =
        this::class.declaredMemberProperties.firstOrNull {
            it.name == "margin"
        }?.getter?.call(this) as? Int
            ?: throw Exception("Cannot generate numeric result for non-numerical metric ${this::class.simpleName}")

    fun black(expected: IValue, actual: IValue, arguments: List<IValue>): ITestResult? {
        return if (subjectProcedure.returnType != VOID || referenceProcedure.returnType != VOID)
            return TestResult(
                actual.sameAs(expected),
                subjectProcedure,
                arguments,
                expected,
                0, // TODO black-box result margin
                actual
            )
        else null
    }

    fun white(listener: EvaluationMetricListener, arguments: List<IValue>): List<ITestResult> {
        val results = mutableListOf<ITestResult>()
        listener.specification.metrics.forEach { parameter -> when (parameter) {
            is CountLoopIterations, is CountArrayReadAccesses, is CountArrayWriteAccesses, is CountMemoryUsage -> {
                results.add(numeric(listener, parameter, arguments))
            }
            is CheckObjectAllocations, is CheckArrayAllocations -> {
                results.addAll(allocations(listener, parameter, arguments))
            }
            is TrackParameterStates, is CheckSideEffects -> {
                results.addAll(parameters(listener, parameter, arguments))
            }
            is CountRecursiveCalls -> {
                results.add(recursive(listener, parameter, arguments))
            }
        } }
        return results
    }

    private fun numeric(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): WhiteBoxTestResult {
        val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
        val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

        val margin = parameter.margin()
        val passed = act.inRange(exp, margin)
        return WhiteBoxTestResult(
            passed,
            subjectProcedure,
            arguments,
            parameter,
            listener.vm.getValue(exp),
            margin,
            listener.vm.getValue(act)
        )
    }

    private fun allocations(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): List<ITestResult> {
        val res = mutableListOf<ITestResult>()

        val exp = listener.getOrDefault(referenceProcedure, parameter::class, mutableMapOf<IType, Int>())
        val act = listener.getOrDefault(subjectProcedure, parameter::class, mutableMapOf<IType, Int>())

        exp.keys.forEachIndexed { index, type ->
            val passed = exp[type] == act[type]
            res.add(AllocationsTestResult(
                passed,
                subjectProcedure,
                arguments,
                type,
                listener.vm.getValue(exp),
                0,
                listener.vm.getValue(act),
                parameter
            ))
        }

        return res
    }

    private fun parameters(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): List<ITestResult> {
        val expectedSideEffects = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, IValue>())
        val actualSideEffects = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, IValue>())

        val results = mutableListOf<WhiteBoxTestResult>()

        referenceProcedure.parameters.forEachIndexed { i, param ->
            val exp = expectedSideEffects[param] ?: arguments[i]
            val act = actualSideEffects[subjectProcedure.parameters[i]] ?: arguments[i]

            val passed = act.sameAs(exp)
            results.add(WhiteBoxTestResult(
                passed,
                subjectProcedure,
                arguments,
                parameter,
                exp,
                0,
                act
            ))
        }

        return results.toList()
    }

    private fun recursive(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): ITestResult {
        val exp = listener.getAll<Int>(referenceProcedure, parameter::class).sum()
        val act = listener.getAll<Int>(subjectProcedure, parameter::class).sum()

        val margin = parameter.margin()
        val passed = act.inRange(exp, margin)
        return WhiteBoxTestResult(
            passed,
            subjectProcedure,
            arguments,
            parameter,
            listener.vm.getValue(exp),
            margin,
            listener.vm.getValue(act)
        )
    }
}