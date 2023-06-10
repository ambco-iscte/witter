package testing

import pt.iscte.strudel.vm.IValue

sealed interface ITestCase {
    fun getMethodName(): String

    fun getMethodArguments(): List<IValue>
}