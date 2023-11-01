package pt.iscte.witter.demo.paddle

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.IProcedure
import pt.iscte.witter.demo.paddle.Paddle4Witter
import pt.iscte.witter.testing.Test
import pt.iscte.witter.testing.definedWitterTests
import pt.iscte.witter.testing.definedWitterTests

private fun IProcedure.descriptor(): String = "${returnType.id} $id(${parameters.joinToString(", ") { "${it.type} ${it.id}" }})"

class TestArea(parent: Composite, private val paddle: Paddle4Witter) {
    private val composite = Composite(parent, SWT.NONE).apply {
        layout = GridLayout(1, false).apply {
            horizontalSpacing = 20
        }
        layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
    }

    private val testCases = mutableMapOf<String, MutableList<TestCheckboxGroup>>()

    fun show(assignment: String) {
        composite.children.forEach { it.dispose() }
        testCases.clear()

        val tests = Java2Strudel().load(paddle.assignments.first { it.nameWithoutExtension == assignment }).definedWitterTests

        Label(composite, SWT.NONE).apply {
            text = "For this assignment, you must define the following procedures:"
        }

        tests.forEach {
            Label(composite, SWT.NONE).apply {
                text = "• ${it.procedure.descriptor()}"
            }
        }

        Label(composite, SWT.NONE)

        tests.forEach { test ->
            test.cases.forEach { args ->
                if (!testCases.containsKey(test.procedure.id))
                    testCases[test.procedure.id!!] = mutableListOf()
                testCases[test.procedure.id]!!.add(TestCheckboxGroup(composite, test, args))
            }
        }

        composite.layout()
    }


    private fun getMissingProcedures(): List<IProcedure> {
        val expected = Java2Strudel().load(paddle.selectedAssignment!!).definedWitterTests.map { it.procedure }
        val implemented = Java2Strudel().load(paddle.codeEditor.code).procedures.map { it }

        return expected.filter { it.id !in implemented.map { p -> p.id } }
    }

    fun runTests() {
        try {
            val tester = Test(paddle.selectedAssignment!!)

            val missing = getMissingProcedures()
            if (missing.isNotEmpty()) {
                paddle.showMessage(
                    "Unfinished Implementation",
                    "You must implement all the necessary procedures before testing your implementation!\n\n" +
                            "Looks like you didn't implement these procedures:\n" +
                            missing.joinToString("\n") { "• ${it.descriptor()}" } + "\n\n" +
                            "You can find a list of expected procedures to the right of the code editing area."
                )
                return
            }

            val groupedResults = tester.executeSource(paddle.codeEditor.code).groupBy { Pair(it.procedure, it.args) }
            groupedResults.keys.forEachIndexed { index, case ->
                groupedResults[case]?.forEach { caseResult ->
                    testCases[case.first.id]!![index].set(caseResult.metricName, caseResult.passed, caseResult.expected, caseResult.actual)
                }
            }

            composite.layout()
        } catch (ex: Exception) {
            paddle.showMessage("Execution error!", "Witter has thrown a ${ex::class.simpleName}:" +
                    "\n" + ex.stackTraceToString())
        }
    }
}