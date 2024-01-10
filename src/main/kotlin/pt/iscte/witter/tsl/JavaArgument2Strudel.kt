package pt.iscte.witter.tsl

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import pt.iscte.strudel.model.*
import pt.iscte.strudel.vm.*
import pt.iscte.witter.testing.EvaluationMetricListener
import pt.iscte.witter.testing.Test

class JavaArgument2Strudel(
    private val tester: Test,
    private val vm: IVirtualMachine,
    private val module: IModule,
    private val listener: EvaluationMetricListener
) {

    fun translate(expression: String): IValue =
        StaticJavaParser.parseExpression<Expression>(expression).translateExpression()

    // TODO: any way we could support more expression types?
    private fun Expression.translateExpression(): IValue = when (this) {
        is IntegerLiteralExpr -> vm.getValue(asNumber().toInt())
        is DoubleLiteralExpr -> vm.getValue(asDouble())
        is CharLiteralExpr -> vm.getValue(asChar())
        is StringLiteralExpr -> vm.getValue(asString())
        is BooleanLiteralExpr -> vm.getValue(value)
        is NullLiteralExpr -> NULL
        is UnaryExpr -> translateUnaryExpression()
        is ArrayCreationExpr -> translateArrayCreationExpression()
        is ObjectCreationExpr -> translateObjectCreationExpression()
        else -> throw Exception("Unsupported argument expression: $this")
    }

    private fun UnaryExpr.translateUnaryExpression(): IValue {
        if (!isPrefix) throw Exception("Postfix unary expressions not supported: $this")

        val multiplier: Int = when (operator) {
            UnaryExpr.Operator.PLUS -> 1
            UnaryExpr.Operator.MINUS -> -1
            else -> throw Exception("Unsupported unary expression operator: $operator")
        }

        return when (val expr = expression) {
            is IntegerLiteralExpr -> vm.getValue(multiplier * expr.asNumber().toInt())
            is DoubleLiteralExpr -> vm.getValue(multiplier * expr.asDouble())
            else -> throw Exception("Unsupported non-integer and non-literal unary expression operand: $expr")
        }
    }

    // TODO: look into what breaks for multidimensional arrays and whatnot
    private fun ArrayCreationExpr.translateArrayCreationExpression(): IReference<IArray> {
        val type = elementType.translate()

        return if (initializer.isEmpty && levels[0].dimension.isPresent) {
            val dim = levels[0].dimension.get()

            if (dim is IntegerLiteralExpr) vm.allocateArray(type, dim.asNumber().toInt())
            else throw UnsupportedOperationException("Unsupported non-integer literal array dimension expression: $dim")
        }
        else if (initializer.isPresent) {
            vm.allocateArrayOf(type, *initializer.get().toArray())
        }
        else // TODO is this what actually happens here
            throw Exception("Array initialisation must contain array dimension expression as an integer literal")
    }

    // TODO: try to reduce the number of situations that throw unsupported exception?
    private fun Type.translate(): IType =
        if (this is PrimitiveType) when (this.type) {
            PrimitiveType.Primitive.INT -> INT
            PrimitiveType.Primitive.DOUBLE -> DOUBLE
            PrimitiveType.Primitive.CHAR -> CHAR
            PrimitiveType.Primitive.BOOLEAN -> BOOLEAN
            else -> throw UnsupportedOperationException("Unsupported primitive array component type: ${this.type.name}")
        } else throw UnsupportedOperationException("Unsupported non-primitive array component type: ${this::class.simpleName}")

    // TODO: !! feels like a hacky fix
    private fun ArrayInitializerExpr.toArray(): Array<Any> =
        values.map { it.translateExpression().value!! }.toTypedArray()

    private fun ObjectCreationExpr.translateObjectCreationExpression(): IReference<IRecord> {
        val type = module.getRecordType(this.type.nameAsString)
        val ref: IReference<IRecord> = vm.allocateRecord(type)

        val constructor = module.getProcedure("\$init")
        val constructorArguments = this.arguments.map { it.translateExpression() }
        val args = tester.dereference(listOf(ref) + constructorArguments, vm, module, listener)
        vm.execute(constructor, *args.toTypedArray())

        return ref
    }
}