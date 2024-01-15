package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.IProcedure

/**
 * Procedure test contemplating set of evaluation [metrics].
 * @param module Reference module.
 * @param calls A pair of (Procedure, Argument(s)) calls to be executed for this test sequence.
 * @param description Description of the test module.
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 */
class TestCase(
    val module: IModule,
    statements: List<IStatement>,
    val description: String,
    val metrics: Set<ITestMetric>
) {
    private val statements: MutableList<IStatement> = mutableListOf()

    init {
        this.statements.addAll(statements)
    }

    fun add(statement: IStatement) = statements.add(statement)

    fun statements(): List<IStatement> = statements.toList()

    /**
     * Does this test module contain a given [ITestMetric]?
     */
    inline fun <reified T : ITestMetric> contains(): Boolean = metrics.find { it is T } != null

    /**
     * Returns the [ITestMetric] associated with this test module, or null if such a metric does not exist
     * for the module.
     */
    inline fun <reified T : ITestMetric> get(): T? = metrics.find { it is T } as? T
}

sealed interface IStatement

sealed interface IExpressionStatement: IStatement

data class VariableAssignment(val id: String, val initializer: () -> IExpressionStatement): IStatement {
    override fun toString(): String = "$id=${initializer()}"
}

data class ProcedureCall(val procedure: IProcedure, val arguments: List<Any?>, val metrics: Set<ITestMetric>): IExpressionStatement {
    override fun toString(): String = "${procedure.id}(${arguments.joinToString()})"
}

class ObjectCreation(
    val case: TestCase,
    val className: String,
    val constructorArguments: List<Any>,
    configure: List<ProcedureCall> = listOf()
): IExpressionStatement {
    private val configure: MutableList<ProcedureCall> = mutableListOf()

    init {
        this.configure.addAll(configure)
    }

    fun add(call: ProcedureCall) = configure.add(call)

    fun configure(): List<ProcedureCall> = configure.toList()

    override fun toString(): String = "new $className(${constructorArguments.joinToString()})"
}

data class VariableReference(val case: TestCase, val id: String): IExpressionStatement {
    override fun toString(): String = "Var($id)"
}

data class Literal(val value: Any?): IExpressionStatement {
    override fun toString(): String = value?.toString() ?: "null"
}