package pt.iscte.witter.tsl

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import pt.iscte.strudel.javaparser.getOrNull
import pt.iscte.strudel.model.*
import pt.iscte.strudel.model.dsl.array
import pt.iscte.strudel.vm.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

class Java2Kotlin {

    fun translate(expression: String): Any? =
        StaticJavaParser.parseExpression<Expression>(expression).translateExpression()

    private fun Expression.translateExpression(): Any? = when (this) {
        is IntegerLiteralExpr -> asNumber().toInt()
        is DoubleLiteralExpr -> asDouble()
        is CharLiteralExpr -> asChar()
        is StringLiteralExpr -> asString()
        is BooleanLiteralExpr -> value
        is NullLiteralExpr -> null
        is UnaryExpr -> translateUnaryExpression()
        is ArrayCreationExpr -> translateArrayCreationExpression()
        is ObjectCreationExpr -> translateObjectCreationExpression()
        else -> throw Exception("Unsupported argument expression: $this")
    }

    private fun UnaryExpr.translateUnaryExpression(): Number {
        if (!isPrefix) throw Exception("Postfix unary expressions not supported: $this")

        val multiplier: Int = when (operator) {
            UnaryExpr.Operator.PLUS -> 1
            UnaryExpr.Operator.MINUS -> -1
            else -> throw Exception("Unsupported unary expression operator: $operator")
        }

        return when (val expr = expression) {
            is IntegerLiteralExpr -> multiplier * expr.asNumber().toInt()
            is DoubleLiteralExpr -> multiplier * expr.asDouble()
            else -> throw Exception("Unsupported non-integer and non-literal unary expression operand: $expr")
        }
    }

    private fun getListOf(type: PrimitiveType, vararg elements: Any?): List<*> = when(type.type) {
        PrimitiveType.Primitive.INT -> listOf<Int>(*elements.map { it as Int }.toTypedArray())
        PrimitiveType.Primitive.DOUBLE -> listOf<Double>(*elements.map { it as Double }.toTypedArray())
        PrimitiveType.Primitive.CHAR -> listOf<Char>(*elements.map { it as Char }.toTypedArray())
        PrimitiveType.Primitive.BOOLEAN -> listOf<Boolean>(*elements.map { it as Boolean }.toTypedArray())
        else -> throw UnsupportedOperationException("Unsupported primitive array component type: " + type.type.name)
    }

    private fun ArrayCreationExpr.translateArrayCreationExpression(): List<*> {
        if (elementType !is PrimitiveType)
            throw UnsupportedOperationException("")

        return if (initializer.isEmpty && levels[0].dimension.isPresent) {
            val dim = levels[0].dimension.get()

            if (dim is IntegerLiteralExpr) getListOf(elementType as PrimitiveType)
            else throw UnsupportedOperationException("Unsupported non-integer literal array dimension expression: $dim")
        }
        else if (initializer.isPresent) getListOf(elementType as PrimitiveType, *initializer.get().toArray())
        else
            throw Exception("Array initialisation must contain array dimension expression as an integer literal")
    }

    private fun ArrayInitializerExpr.toArray(): Array<*> = values.map { it.translateExpression() }.toTypedArray()

    private fun KClass<*>.instantiate(vararg arguments: Any?): Any {
        constructors.forEach {
            runCatching { it.call(*arguments) }.onSuccess { return it }
        }
        throw InstantiationException(
            "Could not find constructor for $simpleName accepting arguments: ${arguments.joinToString()}"
        )
    }

    private fun ObjectCreationExpr.translateObjectCreationExpression(): Any {
        val clazz: KClass<*> = runCatching {
            Class.forName(this.type.nameWithScope).kotlin
        }.onFailure {
            throw ClassNotFoundException("Cannot translate object creation expression: $this\n\t" +
                    "Class with identifier ${type.nameAsString} not found")
        }.getOrNull()!!

        val args = this.arguments.map { it.translateExpression() }.toTypedArray()
        return clazz.instantiate(*args)
    }
}