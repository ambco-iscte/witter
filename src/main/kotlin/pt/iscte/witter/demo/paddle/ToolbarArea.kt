package pt.iscte.witter.demo.paddle

import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.ToolBar
import org.eclipse.swt.widgets.ToolItem
import pt.iscte.witter.demo.paddle.Paddle4Witter

class ToolbarArea(parent: Composite, paddle: Paddle4Witter) {
    private val toolBar = ToolBar(parent, SWT.FLAT or SWT.WRAP or SWT.RIGHT)

    private var itemChooseProblem: Combo? = null

    val selectedAssignment: String
        get() = itemChooseProblem?.text ?: ""

    init {
        /*
        val itemCompile = ToolItem(toolBar, SWT.PUSH)
        itemCompile.text = "Compile"
        itemCompile.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent?) {
                paddle.codeEditor.compile()
            }
        })

        ToolItem(toolBar, SWT.SEPARATOR)

         */

        val itemRun = ToolItem(toolBar, SWT.PUSH)
        itemRun.text = "Run with Witter"
        itemRun.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent?) {
                /*
                if (paddle.codeEditor.compile()) {
                    paddle.codeEditor.clearTooltips()
                    paddle.testArea.runTests()
                }
                 */
                if (paddle.selectedAssignment == null) {

                } else paddle.testArea.runTests()
            }
        })

        ToolItem(toolBar, SWT.SEPARATOR)

        ToolItem(toolBar, SWT.PUSH).apply {
            text = "Choose assignment:"
            enabled = false
        }

        val sep = ToolItem(toolBar, SWT.SEPARATOR)
        itemChooseProblem = Combo(toolBar, SWT.READ_ONLY).apply {
            layout = FillLayout()
        }
        itemChooseProblem!!.setItems(*paddle.assignments.map { it.nameWithoutExtension }.toTypedArray())
        itemChooseProblem!!.addModifyListener { e ->
            val chosen = (e!!.widget as Combo).text
            paddle.testArea.show(chosen)
        }
        sep.control = itemChooseProblem
    }
}