package pt.iscte.witter.testing

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.Visitable
import pt.iscte.strudel.javaparser.extensions.getString
import pt.iscte.strudel.model.*
import pt.iscte.strudel.model.VOID
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.TestCaseStatement
import pt.iscte.witter.tsl.TestSpecifier

internal fun CompilationUnit.removeMainMethod() {
    val visitor = object : ModifierVisitor<Void>() {
        private fun isMain(n: MethodDeclaration): Boolean =
            n.isPublic
                    && n.isStatic
                    && n.typeAsString == "void"
                    && n.nameAsString == "main"
                    && n.parameters.size == 1
                    && n.parameters.first().typeAsString == "String[]"

        override fun visit(n: MethodDeclaration?, arg: Void?): Visitable? {
            if (n != null && isMain(n)) {
                n.body.ifPresent { n.setComment(JavadocComment(it.toString())) }
                n.setBody(BlockStmt())
            }
            return super.visit(n, arg)
        }
    }
    accept(visitor, null)
}

internal fun Int.inRange(start: Int, margin: Int): Boolean = this >= start - margin && this <= start + margin

@Suppress("UNCHECKED_CAST")
internal fun IValue.sameAs(other: IValue): Boolean =
    if (this is IArray && other is IArray) (value as Array<IValue>).sameAs(other.value as Array<IValue>)
    else if (this is IRecord && other is IRecord) type.id == other.type.id && properties().values.sameAs(other.properties().values)
    else if (this is IReference<*> && other is IReference<*>) target.sameAs(other.target)
    else value == other.value

internal fun Array<IValue>.sameAs(other: Array<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

internal fun Iterable<IValue>.sameAs(other: Iterable<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

@Suppress("UNCHECKED_CAST")
fun equivalent(a: Any?, b: Any?): Boolean =
    if (a is IValue && b is IValue)
        a.sameAs(b)
    else if (a is IValue) when (a) {
        is IArray -> a == (a.value as Array<IValue>).map { it.value }
        is IReference<*> -> equivalent(a.target, b)
        else -> a.value == b
    }
    else if (b is IValue) when (b) {
        is IArray -> a == (b.value as Array<IValue>).map { it.value }
        is IReference<*> -> equivalent(a, b.target)
        else -> a == b.value
    }
    else a == b

internal fun <K, V> Map<K, V>.describe(descriptor: (Map.Entry<K, V>) -> String): String =
    if (isEmpty()) "None"
    else map { descriptor(it) }.joinToString()

val IModule.tests: List<TestCaseStatement>
    get() {
        val tests = mutableListOf<TestCaseStatement>()
        procedures.filterIsInstance<IProcedure>().forEach { procedure ->
            TestSpecifier.translate(procedure)?.let { tests.add(it) }
        }
        return tests.toList()
    }

fun IProcedureDeclaration.isInstanceMethod(): Boolean = kotlin.runCatching { thisParameter }.isSuccess

fun IProcedureDeclaration.matchesSignature(other: IProcedureDeclaration): Boolean = // FIXME refactor
    if (isInstanceMethod() && other.isInstanceMethod()) {
        val paramsMatch = parameters.subList(1, parameters.size).map { p -> p.type.id } == other.parameters.subList(1, other.parameters.size).map { p -> p.type.id }
        val thisMatch = runCatching { thisParameter.type.id }.getOrNull() == runCatching { other.thisParameter.type.id }.getOrNull()
        returnType.id == other.returnType.id && paramsMatch // FIXME qualified names: && thisMatch
    } else if (!isInstanceMethod() && !other.isInstanceMethod()) {
        val paramsMatch = parameters.map { p -> p.type.id } == other.parameters.map { p -> p.type.id }
        returnType.id == other.returnType.id && paramsMatch
    } else
        false

fun IModule.findMatchingProcedure(procedure: IProcedure): IProcedure? =
    procedures.filterIsInstance<IProcedure>().filter { it.matchesSignature(procedure) }.firstOrNull { it.id == procedure.id }

fun IModule.findAcceptingProcedure(id: String, arguments: List<IValue>): IProcedure? =
    procedures.filterIsInstance<IProcedure>().firstOrNull {
        it.id == id && it.parameters.map { p -> p.type.id } == arguments.map { a -> a.type.id }
    }

/*
{
    val matches = procedures.filter {
        it.returnType.id == procedure.returnType.id
                && it.parameters.map { p -> p.type.id } == procedure.parameters.map { p -> p.type.id }
    }
    return matches.minByOrNull { it.id!!.compareTo(procedure.id!!) }
}
 */

internal fun IVirtualMachine.allocateStringArray(vararg values: String): IReference<IArray> =
    allocateArrayOf(getString("").type, *values.map { getString(it) }.toTypedArray())

internal fun getValue(vm: IVirtualMachine, value: Any): IValue = when (value) {
    is Collection<*> -> {
        if (value.isEmpty()) TODO("Cannot allocate empty Strudel array!")
        else if (runCatching { vm.getValue(value.first()) }.isSuccess) {
            val type = vm.getValue(value.first()).type
            vm.allocateArrayOf(type, *value.map {
                it ?: TODO("Cannot allocate Strudel array with null elements!")
            }.toTypedArray())
        } else if (value.first() is String) {
            vm.allocateStringArray(*(value as Collection<String>).toTypedArray())
        } else {
            val type = HostRecordType(value.first()!!::class.java.canonicalName)
            vm.allocateArrayOf(type, *value.map { it ?: TODO("Cannot allocate Strudel array with null elements!") }.toTypedArray())
        }
    }
    is IValue -> value
    is String -> getString(value)
    else -> vm.getValue(value)
}

internal fun IValue.deepCopy(vm: IVirtualMachine): IValue = when (this) {
    is IRecord -> {
        val record = vm.allocateRecord(type.asRecordType).target
        this.properties().forEach { (field, value) ->
            val f = record.type.asRecordType.fields.find { it.type == field.type && it.id == field.id }
            if (f != null)
                record.setField(f, value.deepCopy(vm))
        }
        record
    }
    is IReference<*> -> target.deepCopy(vm).reference()
    else -> copy()
}

internal fun IRecord.properties(): Map<IVariableDeclaration<IRecordType>, IValue> {
    val type: IRecordType = when (val t = this.type) {
        is IRecordType -> t
        else -> t.asRecordType
    }
    return type.fields.associateWith { getField(it) }
}

internal val IProcedure.signature: String
    get() = "${id}(${parameters.joinToString { it.type.id ?: "null" }})"

// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
internal fun IType.descriptor(): String = when (this) {
        VOID -> "V"
        INT -> "I"
        DOUBLE -> "D"
        BOOLEAN -> "Z"
        CHAR -> "C"
        is IRecordType -> "L${this.id};"
        is IArrayType -> "[${this.componentType.descriptor()}"
        is IReferenceType -> target.descriptor()
        is UnboundType -> this.id!!
        else -> throw UnsupportedOperationException("Unsupported descriptor for type: ${this.id}")
    }

internal val IProcedure.descriptor: String
    get() = "(" + parameters.joinToString("") { it.type.descriptor() } + ")" + returnType.descriptor()