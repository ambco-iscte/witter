package pt.iscte.witter.testing

import pt.iscte.witter.tsl.TestModule

class TestSuite(val referencePath: String, val description: String, modules: List<TestModule> = listOf()) {
    private val modules = mutableListOf<TestModule>()

    init {
        this.modules.addAll(modules)
    }

    fun add(module: TestModule) = modules.add(module)

    fun remove(module: TestModule) = modules.remove(module)

    fun modules(): List<TestModule> = modules.toList()

    fun apply(subjectPath: String): List<ITestResult> = Test(referencePath).apply(subjectPath, this)
}