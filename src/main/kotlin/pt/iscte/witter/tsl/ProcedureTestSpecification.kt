package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.IProcedure

/**
 * Procedure test contemplating set of evaluation [metrics].
 * @param module Reference module.
 * @param calls A pair of (Procedure, Argument(s)) calls to be executed for this test sequence.
 * @param description Description of the test module.
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 * @param stateful If true, VM state is preserved between calls. Otherwise, each call is executed in a separate VM state.
 */
class TestModule(
    val module: IModule,
    statements: List<IStatement>,
    val description: String,
    val metrics: Set<ITestMetric>,
    val stateful: Boolean
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

    override fun equals(other: Any?): Boolean = when (other) {
        is TestModule -> module == other.module && description == other.description && metrics == other.metrics &&
                stateful == other.stateful && statements() == other.statements()
        else -> false
    }
}

sealed interface IStatement

sealed interface Instruction: IStatement

sealed interface IExpression: IStatement

data class VariableAssignment(val id: String, val initializer: () -> IExpression): Instruction {
    override fun toString(): String = "$id=${initializer()}"
}

data class ProcedureCall(val procedure: IProcedure, val arguments: Any?, val parsed: Boolean): IExpression {
    override fun toString(): String =
        if (arguments is List<*>) "${procedure.id}(${arguments.joinToString()})"
        else "${procedure.id}(${arguments.toString()})"
}

class ObjectCreation(
    val module: TestModule,
    val className: String,
    val constructorArguments: List<Any>,
    configure: List<ProcedureCall> = listOf()
): IExpression {
    private val configure: MutableList<ProcedureCall> = mutableListOf()

    init {
        this.configure.addAll(configure)
    }

    fun add(call: ProcedureCall) = configure.add(call)

    fun configure(): List<ProcedureCall> = configure.toList()

    override fun toString(): String = "new $className(${constructorArguments.joinToString()})"
}

data class VariableReference(val id: String): IExpression {
    override fun toString(): String = "Var($id)"
}

data class Literal(val value: Any?): IExpression {
    override fun toString(): String = value?.toString() ?: "null"
}