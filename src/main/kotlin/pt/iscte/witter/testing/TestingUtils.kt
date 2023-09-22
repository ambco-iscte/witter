package pt.iscte.witter.testing

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.vm.IArray
import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.tsl.ProcedureTestSpecification
import pt.iscte.witter.tsl.TestSpecifier

@Suppress("UNCHECKED_CAST")
internal fun IValue.sameAs(other: IValue): Boolean =
    if (this is IArray && other is IArray) (value as Array<IValue>).sameAs(other.value as Array<IValue>)
    else value == other.value

internal fun Array<IValue>.sameAs(other: Array<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

internal fun Iterable<IValue>.sameAs(other: Iterable<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

internal fun <K, V> Map<K, V>.describe(descriptor : (Map.Entry<K, V>) -> String): String =
    if (isEmpty()) "None"
    else map { descriptor(it) }.joinToString(", ")

val IModule.definedWitterTests: List<ProcedureTestSpecification>
    get() {
        val tests = mutableListOf<ProcedureTestSpecification>()
        procedures.forEach { procedure ->
            TestSpecifier.translate(procedure)?.let { tests.add(it) }
        }
        return tests.toList()
    }