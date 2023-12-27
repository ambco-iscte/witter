package pt.iscte.witter.dsl

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.witter.tsl.*

fun Suite(referencePath: String, subjectPath: String, description: String = "", configure: TestSuite.() -> Unit = {}): TestSuite {
    val suite = TestSuite(referencePath, subjectPath, description)
    configure(suite)
    return suite
}

fun TestSuite.Static(
    procedureID: String,
    metrics: Set<ITestMetric>,
    configure: SingleProcedureTestSuite.() -> Unit = {}
): SingleProcedureTestSuite {
    val module = Java2Strudel().load(referencePath)
    val s = SingleProcedureTestSuite(
        module,
        module.getProcedure(procedureID),
        listOf(),
        true,
        metrics,
        description
    )
    configure(s)
    add(s)
    return s
}

fun TestSuite.Static(
    procedureID: String,
    metric: ITestMetric,
    configure: SingleProcedureTestSuite.() -> Unit = {}
): SingleProcedureTestSuite = Static(procedureID, setOf(metric), configure)

fun SingleProcedureTestSuite.Case(vararg arguments: Any?): SingleProcedureTestSuite {
    addCase(arguments.toList())
    return this
}

fun TestSuite.Sequential(
    metrics: Set<ITestMetric>,
    description: String = "",
    configure: StatefulTestSequence.() -> Unit = {}
): StatefulTestSequence {
    val module = Java2Strudel().load(referencePath)
    val m = StatefulTestSequence(
        module,
        listOf(),
        metrics,
        description
    )
    configure(m)
    add(m)
    return m
}

fun TestSuite.Sequential(
    metric: ITestMetric,
    description: String = "",
    configure: StatefulTestSequence.() -> Unit = {}
): StatefulTestSequence = Sequential(setOf(metric), description, configure)

fun StatefulTestSequence.Case(procedureID: String, vararg arguments: Any?): StatefulTestSequence {
    addCall(module.getProcedure(procedureID), arguments.toList())
    return this
}

fun StatefulTestSequence.Before(configure: StatefulTestSequence.() -> Unit) {
    // TODO
}