package pt.iscte.witter.demo.paddle

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import java.io.File

fun main(args: Array<String>) {
    val display = Display()
    val shell = Shell(display)
    shell.text = "Witter • Paddle"
    shell.size = Point(1200, 800)
    shell.layout = GridLayout(1, false)

    val file = if (args.isEmpty()) File("Test.java") else File(args[0])
    if (!file.exists())
        file.createNewFile()

    val assignmentsFolder = if (args.size >= 2) args[1] else "src/main/java/pt/iscte/witter/demo/assignments"
    val assignments = File(assignmentsFolder).listFiles { f -> f.extension == "java" }?.toList() ?: listOf()
    Paddle4Witter(shell, file, assignments)

    //shell.pack()
    shell.open()

    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
    display.dispose()
}

class Paddle4Witter(private val shell: Shell, file: File, val assignments: List<File>) {
    private val composite = Composite(shell, SWT.NONE).apply {
        layout = GridLayout(1, false)
        layoutData = GridData(GridData.FILL_BOTH)
    }

    val toolBar = ToolbarArea(composite, this)

    val sash = SashForm(composite, SWT.HORIZONTAL).apply {
        layoutData = GridData(GridData.FILL_BOTH)
    }

    val codeEditor: ICodeEditor = CodeEditor(sash, file)

    val testArea = TestArea(sash, this)

    val selectedAssignment: File?
        get() = assignments.firstOrNull { it.nameWithoutExtension == toolBar.selectedAssignment }

    fun showMessage(title: String, message: String) {
        val box = MessageBox(shell, SWT.OK)
        box.text = title
        box.message = message
        box.open()
    }
}
