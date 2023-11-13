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

    // TODO: putting code in this class cleaned up Test a lot, but now the mess is here instead :)

    private fun ITestMetric.margin(): Int =
        this::class.declaredMemberProperties.firstOrNull {
            it.name == "margin"
        }?.getter?.call(this) as? Int
            ?: throw Exception("Cannot generate numeric result for non-numerical metric ${this::class.simpleName}")

    fun black(expected: Any?, actual: Any?, arguments: List<IValue>): ITestResult? {
        return if (subjectProcedure.returnType != VOID || referenceProcedure.returnType != VOID)
            return TestResult(
                actual == expected,
                subjectProcedure,
                arguments,
                "result",
                expected,
                null,
                actual
            )
        else null
    }

    fun white(listener: EvaluationMetricListener, arguments: List<IValue>): List<TestResult> {
        val results = mutableListOf<TestResult>()
        listener.specification.metrics.forEach { parameter -> when (parameter) {
            is CountLoopIterations, is CountArrayReadAccesses, is CountArrayWriteAccesses, is CountMemoryUsage -> {
                results.add(numeric(listener, parameter, arguments))
            }
            is CheckObjectAllocations, is CheckArrayAllocations -> {
                results.add(allocations(listener, parameter, arguments))
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

    private fun numeric(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): TestResult {
        val exp = listener.getOrDefault(referenceProcedure, parameter::class, 0)
        val act = listener.getOrDefault(subjectProcedure, parameter::class, 0)

        val margin = parameter.margin()
        val passed = act.inRange(exp, margin)
        return TestResult(
            passed,
            subjectProcedure,
            arguments,
            parameter.description(),
            exp,
            margin,
            act
        )
    }

    private fun allocations(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): TestResult {
        val exp = listener.getOrDefault(referenceProcedure, parameter::class, mutableMapOf<IType, Int>())
        val act = listener.getOrDefault(subjectProcedure, parameter::class, mutableMapOf<IType, Int>())

        val passed = act.keys.all { it in exp.keys && act[it] == exp[it] }
        return TestResult(
            passed,
            subjectProcedure,
            arguments,
            parameter.description(),
            exp.describe { "${it.value} allocation(s) of ${it.key}" },
            null,
            act.describe { "${it.value} allocation(s) of ${it.key}" }
        )
    }

    private fun parameters(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): List<TestResult> {
        val expectedSideEffects = listener.getOrDefault(referenceProcedure, parameter::class, mapOf<IParameter, IValue>())
        val actualSideEffects = listener.getOrDefault(subjectProcedure, parameter::class, mapOf<IParameter, IValue>())

        val results = mutableListOf<TestResult>()

        referenceProcedure.parameters.forEachIndexed { i, param ->
            val exp = expectedSideEffects[param] ?: arguments[i]
            val act = actualSideEffects[subjectProcedure.parameters[i]] ?: arguments[i]

            val passed = act.sameAs(exp)
            results.add(TestResult(
                passed,
                subjectProcedure,
                arguments,
                parameter.description() + " of ${param.id}",
                exp,
                null,
                act
            ))
        }

        return results.toList()
    }

    private fun recursive(listener: EvaluationMetricListener, parameter: ITestMetric, arguments: List<IValue>): TestResult {
        val exp = listener.getAll<Int>(referenceProcedure, parameter::class).sum()
        val act = listener.getAll<Int>(subjectProcedure, parameter::class).sum()

        val margin = parameter.margin()
        val passed = act.inRange(exp, margin)
        return TestResult(
            passed,
            subjectProcedure,
            arguments,
            parameter.description(),
            exp,
            margin,
            act
        )
    }
}