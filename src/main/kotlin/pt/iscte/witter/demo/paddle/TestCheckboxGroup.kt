package pt.iscte.witter.demo.paddle

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import pt.iscte.strudel.model.VOID
import pt.iscte.witter.tsl.CheckSideEffects
import pt.iscte.witter.tsl.ProcedureTestSpecification
import pt.iscte.witter.tsl.TrackParameterStates
import java.util.*

class TestCheckboxGroup(
    parent: Composite,
    private val test: ProcedureTestSpecification,
    private val args: String
): Composite(parent, SWT.NONE) {

    private val red: Color = display.getSystemColor(SWT.COLOR_RED)
    private val green: Color = display.getSystemColor(SWT.COLOR_DARK_GREEN)

    private val checkboxes = mutableMapOf<String, Button>()

    private fun String.capitalise(): String = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    init {
        layout = GridLayout(1, false)

        Label(this, SWT.BOLD).apply {
            text = test.procedure.id + "($args)"
        }

        val sub = Composite(this, SWT.NONE).apply {
            layout = GridLayout(1, false)
            layoutData = GridData(GridData.FILL_BOTH).apply {
                horizontalIndent = 20
            }
        }

        if (test.procedure.returnType != VOID) {
            val resultButton = Button(sub, SWT.CHECK).apply {
                text = "Result"
                enabled = false
            }
            checkboxes["result"] = resultButton
        }

        test.metrics.forEach { metric ->
            when (metric) {
                is CheckSideEffects, is TrackParameterStates -> {
                    test.procedure.parameters.forEach { param ->
                        val description = metric.description() + " of ${param.id}"
                        val metricButton = Button(sub, SWT.CHECK).apply {
                            text = description.capitalise()
                            enabled = false
                        }
                        checkboxes[description] = metricButton
                    }
                }
                else -> {
                    val metricButton = Button(sub, SWT.CHECK).apply {
                        text = metric.description().capitalise()
                        enabled = false
                    }
                    checkboxes[metric.description()] = metricButton
                }
            }
        }
    }

    fun set(metric: String, passed: Boolean, expected: Any?, actual: Any?) {
        val checkbox = checkboxes[metric] ?: checkboxes[checkboxes.keys.find { metric.lowercase().startsWith(it.lowercase()) }]
        checkbox?.apply {
            selection = passed
            text = "${metric.capitalise()} (expected = $expected; found = $actual)"

            if (getListeners(SWT.Paint).isEmpty())
                addPaintListener { e ->
                    e?.gc?.foreground = if (passed) green else red
                    e?.gc?.drawText(text, 16, 0)
                }

            redraw()
        }
    }
}