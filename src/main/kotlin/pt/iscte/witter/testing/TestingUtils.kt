package pt.iscte.witter.testing

import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.vm.IArray
import pt.iscte.strudel.vm.IValue
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.strudel.vm.NULL
import pt.iscte.witter.tsl.TestModule
import pt.iscte.witter.tsl.TestSpecifier
import kotlin.reflect.KClass

internal fun Int.inRange(start: Int, margin: Int): Boolean = this >= start - margin && this <= start + margin

@Suppress("UNCHECKED_CAST")
internal fun IValue.sameAs(other: IValue): Boolean =
    if (this is IArray && other is IArray) (value as Array<IValue>).sameAs(other.value as Array<IValue>)
    else value == other.value

internal fun Array<IValue>.sameAs(other: Array<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

internal fun Iterable<IValue>.sameAs(other: Iterable<IValue>): Boolean = zip(other).all { it.first.sameAs(it.second) }

internal fun <K, V> Map<K, V>.describe(descriptor: (Map.Entry<K, V>) -> String): String =
    if (isEmpty()) "None"
    else map { descriptor(it) }.joinToString(", ")

val IModule.tests: List<TestModule>
    get() {
        val tests = mutableListOf<TestModule>()
        procedures.forEach { procedure ->
            TestSpecifier.translate(procedure)?.let { tests.add(it) }
        }
        return tests.toList()
    }

internal fun getArgumentsFromString(vm: IVirtualMachine, arguments: String): Triple<List<IValue>, List<IValue>, List<IValue>> =
    Triple(
        TestSpecifier.parseArgumentsString(vm, arguments),
        TestSpecifier.parseArgumentsString(vm, arguments),
        TestSpecifier.parseArgumentsString(vm, arguments)
    )

internal fun getArgumentsFromValues(vm: IVirtualMachine, arguments: List<Any>): Triple<List<IValue>, List<IValue>, List<IValue>> {
    fun getValue(value: Any): IValue = when (value) {
        is Collection<*> -> {
            if (value.isEmpty()) TODO("Cannot allocate empty Strudel array!")
            else {
                val type = vm.getValue(value.first()).type
                vm.allocateArrayOf(type, *value.map {
                    it ?: throw Exception("Cannot allocate Strudel array with null elements.")
                }.toTypedArray())
            }
        }
        is IValue -> value
        else -> vm.getValue(value)
    }
    return Triple(
        arguments.map { getValue(it) },
        arguments.map { getValue(it) },
        arguments.map { getValue(it) }
    )
}