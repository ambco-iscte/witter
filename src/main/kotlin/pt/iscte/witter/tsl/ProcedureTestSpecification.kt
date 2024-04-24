package pt.iscte.witter.tsl

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.IProcedure

@DslMarker
annotation class WitterDSL

/**
 * Procedure test contemplating set of evaluation [metrics].
 * @param module Reference module.
 * @param statements A pair of test specification statements.
 * @param description Description of the test module.
 * @param metrics A set of the evaluation metrics ([ITestMetric]) that should be calculated.
 */
@WitterDSL
class TestCaseStatement(
    val module: IModule,
    statements: List<IStatement>,
    val description: String,
    val metrics: Set<ITestMetric>
): IStatement {
    private val statements: MutableList<IStatement> = mutableListOf()

    init {
        this.statements.addAll(statements)
    }

    fun add(statement: IStatement) = statements.add(statement)

    fun remove(statement: IStatement) = statements.remove(statement)

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

@WitterDSL
data class VariableAssignment(val id: String, val initializer: () -> IExpressionStatement): IStatement {
    override fun toString(): String = "$id = ${initializer()}"
}

@WitterDSL
data class ProcedureCall(
    val procedure: IProcedure,
    val arguments: List<Any?>,
    val metrics: Set<ITestMetric> = setOf(),
    val expected: Any? = null
): IExpressionStatement {

    override fun toString(): String = "${procedure.id}(${arguments.joinToString()})"
}

@WitterDSL
class ObjectCreation(
    val case: TestCaseStatement,
    val qualifiedName: String,
    val constructorArguments: List<Any>,
    configure: List<ProcedureCall> = listOf()
): IExpressionStatement {
    private val configure: MutableList<ProcedureCall> = mutableListOf()

    init {
        this.configure.addAll(configure)
    }

    fun add(call: ProcedureCall) = configure.add(call)

    fun configure(): List<ProcedureCall> = configure.toList()

    override fun toString(): String =
        if (configure.isEmpty())
            "$qualifiedName(${constructorArguments.joinToString()})"
        else
            "$qualifiedName(${constructorArguments.joinToString()}).apply {\n\t${configure.joinToString("\n\t")}\n}"
}

@WitterDSL
data class VariableReference(val case: TestCaseStatement, val id: String): IExpressionStatement {

    companion object {
        private var uuid: Long = 0

        fun uuid(): Long = uuid++
    }

    override fun toString(): String = "Var($id)"
}