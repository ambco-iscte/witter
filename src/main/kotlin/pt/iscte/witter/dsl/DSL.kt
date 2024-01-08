package pt.iscte.witter.dsl

import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.witter.testing.TestSuite
import pt.iscte.witter.tsl.*
import java.io.File

fun Suite(referencePath: String, description: String = "", configure: TestSuite.() -> Unit = {}): TestSuite {
    val suite = TestSuite(referencePath, description)
    configure(suite)
    return suite
}

fun TestModule.Object(className: String, vararg constructorArguments: Any?, configure: ObjectCreation.() -> Unit): ObjectCreation {
    val obj = ObjectCreation(this, className, constructorArguments.toList())
    configure(obj)
    add(obj)
    return obj
}

fun ObjectCreation.Call(procedureID: String, vararg arguments: Any?): ProcedureCall {
    val call = ProcedureCall(module.module.getProcedure(procedureID), arguments.toList(), true)
    add(call)
    return call
}

fun TestModule.Var(id: String, configure: () -> IExpression): VariableReference {
    add(VariableAssignment(id, configure()))
    return VariableReference(id)
}

fun TestModule.Call(procedureID: String, vararg arguments: Any?): ProcedureCall {
    val call = ProcedureCall(module.getProcedure(procedureID), arguments.toList(), true)
    add(call)
    return call
}

fun TestSuite.Stateless(
    metrics: Set<ITestMetric>,
    description: String = "",
    configure: TestModule.() -> Unit = {}
): TestModule {
    val module = Java2Strudel().load(File(referencePath))
    val s = TestModule(
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

fun TestSuite.Stateful(
    metrics: Set<ITestMetric>,
    description: String = "",
    configure: TestModule.() -> Unit = {}
): TestModule {
    val module = Java2Strudel().load(File(referencePath))
    val s = TestModule(
        module,
        listOf(),
        description,
        metrics,
        stateful = true
    )
    configure(s)
    add(s)
    return s
}

fun TestSuite.Stateless(
    metric: ITestMetric,
    description: String = "",
    configure: TestModule.() -> Unit = {}
): TestModule = Stateless(setOf(metric), description, configure)

fun TestSuite.Stateful(
    metric: ITestMetric,
    description: String = "",
    configure: TestModule.() -> Unit = {}
): TestModule = Stateful(setOf(metric), description, configure)