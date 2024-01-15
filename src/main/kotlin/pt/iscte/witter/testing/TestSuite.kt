package pt.iscte.witter.testing

import pt.iscte.witter.tsl.TestCase

class TestSuite(val referencePath: String, val description: String, cases: List<TestCase> = listOf()) {
    private val modules = mutableListOf<TestCase>()

    init {
        this.modules.addAll(cases)
    }

    fun add(case: TestCase) = modules.add(case)

    fun remove(case: TestCase) = modules.remove(case)

    fun cases(): List<TestCase> = modules.toList()

    fun apply(subjectPath: String): List<ITestResult> = Test(referencePath).apply(subjectPath, this)
}