package pt.iscte.witter.testing

import pt.iscte.witter.tsl.TestCaseStatement
import java.io.File

class TestSuite(val referencePath: String, val description: String, cases: List<TestCaseStatement> = listOf()) {
    private val modules = mutableListOf<TestCaseStatement>()

    init {
        this.modules.addAll(cases)
    }

    fun add(case: TestCaseStatement) = modules.add(case)

    fun remove(case: TestCaseStatement) = modules.remove(case)

    fun cases(): List<TestCaseStatement> = modules.toList()

    fun apply(subjectPath: String): List<ITestResult> = Test(referencePath).apply(subjectPath, this)

    fun apply(subject: File): List<ITestResult> = Test(referencePath).apply(subject, this)

    fun walk(root: File): TestReport {
        val results = mutableMapOf<String, List<ITestResult>>()
        val ref = File(referencePath)
        root.walkTopDown().forEach {
            if (it.isFile && it.path != referencePath && it.name == ref.name) {
                results[it.path] = apply(it)
            }
        }
        return TestReport(results.toMap())
    }
}