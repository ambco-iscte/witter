package pt.iscte.witter.testing

import pt.iscte.witter.extensions.compile
import pt.iscte.witter.testing.report.TestReport
import pt.iscte.witter.testing.report.TimedResult
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
        val results = mutableMapOf<String, TimedResult>()
        val ref = File(referencePath)
        root.walkTopDown().forEach {
            if (it != ref && it.name == ref.name && compile(it)) {
                val start = System.currentTimeMillis()
                val res: List<ITestResult> = apply(it)
                val end = System.currentTimeMillis()
                results[it.path] = TimedResult(end - start, res)
            }
        }
        return TestReport(this, results.toMap(), description)
    }

    override fun toString(): String = modules.joinToString(System.lineSeparator())
}