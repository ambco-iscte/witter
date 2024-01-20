package pt.iscte.witter.testing

import pt.iscte.strudel.model.*
import pt.iscte.strudel.model.VOID
import pt.iscte.strudel.vm.*
import pt.iscte.witter.tsl.TestCaseStatement
import pt.iscte.witter.tsl.TestSpecifier

internal fun Int.inRange(start: Int, margin: Int): Boolean = this >= start - margin && this <= start + margin

@Suppress("UNCHECKED_CAST")
internal fun IValue.sameAs(other: IValue): Boolean =
    if (this is IArray && other is IArray) (value as Array<IValue>).sameAs(other.value as Array<IValue>)
    else if (this is IRecord && other is IRecord) properties().values.sameAs(other.properties().values)
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
        procedures.forEach { procedure ->
            TestSpecifier.translate(procedure)?.let { tests.add(it) }
        }
        return tests.toList()
    }

fun IModule.findMatchingProcedure(procedure: IProcedure): IProcedure? =
    if (procedure.module == this) procedure
    else runCatching { getProcedure(procedure.id!!) }.getOrNull()
/*
{
    val matches = procedures.filter {
        it.returnType.id == procedure.returnType.id
                && it.parameters.map { p -> p.type.id } == procedure.parameters.map { p -> p.type.id }
    }
    matches.minByOrNull { it.id!!.compareTo(procedure.id!!) }
}
 */

internal fun getValue(vm: IVirtualMachine, value: Any): IValue = when (value) {
    is Collection<*> -> {
        if (value.isEmpty()) TODO("Cannot allocate empty Strudel array!")
        else {
            val type = vm.getValue(value.first()).type
            vm.allocateArrayOf(type, *value.map {
                it ?: TODO("Cannot allocate Strudel array with null elements!")
            }.toTypedArray())
        }
    }
    is IValue -> value
    else -> vm.getValue(value)
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