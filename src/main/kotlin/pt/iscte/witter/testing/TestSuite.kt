package pt.iscte.witter.testing

import pt.iscte.witter.tsl.TestCaseStatement

class TestSuite(val referencePath: String, val description: String, cases: List<TestCaseStatement> = listOf()) {
    private val modules = mutableListOf<TestCaseStatement>()

    init {
        this.modules.addAll(cases)
    }

    fun add(case: TestCaseStatement) = modules.add(case)

    fun remove(case: TestCaseStatement) = modules.remove(case)

    fun cases(): List<TestCaseStatement> = modules.toList()

    fun apply(subjectPath: String): List<ITestResult> = Test(referencePath).apply(subjectPath, this)
}