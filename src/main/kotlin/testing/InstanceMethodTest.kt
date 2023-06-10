package testing

import pt.iscte.strudel.vm.IValue

class InstanceMethodTest(private val method: String, val namespace: String, vararg args: IValue): ITestCase {
    private val arguments: List<IValue> = args.toList()

    override fun getMethodName(): String = method

    override fun getMethodArguments(): List<IValue> = arguments
}
