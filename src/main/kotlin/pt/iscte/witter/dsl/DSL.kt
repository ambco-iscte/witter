package pt.iscte.witter.dsl

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.javaparser.StrudelUnsupported
import pt.iscte.witter.testing.TestSuite
import pt.iscte.witter.tsl.*
import java.io.File

fun Suite(referencePath: String, description: String = "", configure: TestSuite.() -> Unit = {}): TestSuite {
    val suite = TestSuite(referencePath, description)
    configure(suite)
    return suite
}

fun TestCaseStatement.new(className: String, vararg constructorArguments: Any, configure: ObjectCreation.() -> Unit = { }): ObjectCreation {
    val obj = ObjectCreation(this, className, constructorArguments.toList())
    configure(obj)
    add(obj)
    return obj
}

fun ObjectCreation.call(procedureID: String, vararg arguments: Any?, expected: Any? = null): ProcedureCall {
    val call = ProcedureCall(case.module.getProcedure(procedureID), arguments.toList(), case.metrics, expected)
    add(call)
    return call
}

fun TestCaseStatement.ref(id: String, configure: () -> IExpressionStatement): VariableReference {
    add(VariableAssignment(id, configure))
    return VariableReference(this, id)
}

fun TestCaseStatement.call(procedureID: String, vararg arguments: Any?, expected: Any? = null): ProcedureCall {
    val call = ProcedureCall(module.getProcedure(procedureID), arguments.toList(), metrics, expected)
    add(call)
    return call
}

fun VariableReference.call(procedureID: String, vararg arguments: Any?, expected: Any? = null): ProcedureCall {
    val call = ProcedureCall(
        runCatching { case.module.getProcedure(procedureID) }.onFailure {
            throw AssertionError("Reference solution does not implement procedure with ID $procedureID", it)
        }.getOrThrow(),
        listOf(this) + arguments.toList(),
        case.metrics,
        expected
    )
    case.add(call)
    return call
}

fun TestCaseStatement.using(metrics: Set<ITestMetric>, description: String = "", configure: TestCaseStatement.() -> Unit): TestCaseStatement {
    val case = TestCaseStatement(this.module, listOf(), description, metrics)
    configure(case)
    add(case)
    return case
}

fun TestCaseStatement.using(metric: ITestMetric, description: String = "", configure: TestCaseStatement.() -> Unit): TestCaseStatement =
    using(setOf(metric), description, configure)

fun TestCaseStatement.using(description: String = "", configure: TestCaseStatement.() -> Unit): TestCaseStatement =
    using(setOf(), description, configure)

fun TestSuite.Case(
    metrics: Set<ITestMetric>,
    description: String = "",
    configure: TestCaseStatement.() -> Unit = {}
): TestCaseStatement {
    val module = runCatching {
        Java2Strudel().load(File(referencePath))
    }.onFailure {
        when (it) {
            is StrudelUnsupported -> throw Exception(
                "Strudel could not load the file $referencePath: ${it.message}\n\t${it.locations.joinToString("\n\t")}",
                it
            )
            else -> throw Exception("Exception thrown when loading file $referencePath: ${it.message}", it)
        }
    }.getOrThrow()
    val s = TestCaseStatement(
        module,
        listOf(),
        description,
        metrics
    )
    configure(s)
    add(s)
    return s
}

/*
fun TestSuite.Stateless(
    metrics: Set<ITestMetric>,
    description: String = "",
    configure: TestCase.() -> Unit = {}
): TestCase {
    val module = Java2Strudel().load(File(referencePath))
    val s = TestCase(
        module,
        listOf(),
        description,
        metrics,
        stateful = false
    )
    configure(s)
    add(s)
    return s
}

fun TestSuite.Stateless(
    metric: ITestMetric,
    description: String = "",
    configure: TestCase.() -> Unit = {}
): TestCase = Stateless(setOf(metric), description, configure)

fun TestSuite.Stateless(
    description: String = "",
    configure: TestCase.() -> Unit = {}
): TestCase = Stateless(setOf(), description, configure)
 */

fun TestSuite.Case(
    metric: ITestMetric,
    description: String = "",
    configure: TestCaseStatement.() -> Unit = {}
): TestCaseStatement = Case(setOf(metric), description, configure)

fun TestSuite.Case(
    description: String = "",
    configure: TestCaseStatement.() -> Unit = {}
): TestCaseStatement = Case(setOf(), description, configure)