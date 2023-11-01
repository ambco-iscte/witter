package pt.iscte.witter.demo.paddle

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.*
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.FontData
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.javaparser.SourceLocation
import pt.iscte.strudel.javaparser.StrudelUnsupported
import pt.iscte.strudel.model.IBlock
import pt.iscte.strudel.model.IBlockElement
import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.IProcedure
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import javax.tools.Diagnostic
import kotlin.math.max
import pt.iscte.witter.demo.JavaLineStyler

class CodeEditor(parent: Composite, val file: File) : ICodeEditor {

    private val font =
        Font(Display.getDefault(), FontData("Courier", 14, SWT.NONE))
    private val textCode = StyledText(
        parent, SWT.BORDER or SWT.MULTI or SWT.V_SCROLL
                or SWT.H_SCROLL
    )

    override val code: String get() = textCode.text

    val diagnosticStyles: MutableList<StyleRange> = mutableListOf()

    private val tooltips = mutableListOf<ToolTip>()

    init {
        textCode.font = font
        val spec = GridData()
        spec.horizontalAlignment = GridData.FILL
        spec.grabExcessHorizontalSpace = true
        spec.verticalAlignment = GridData.FILL
        spec.grabExcessVerticalSpace = true

        textCode.layoutData = spec
        textCode.addLineStyleListener(JavaLineStyler(diagnosticStyles))
        textCode.editable = true

        textCode.background = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
        if (Display.isSystemDarkTheme()) {
            textCode.foreground =
                Display.getDefault().getSystemColor(SWT.COLOR_BLACK)
        } else {
            textCode.foreground =
                Display.getDefault().getSystemColor(SWT.COLOR_WHITE)
        }

        textCode.text = file.readText(Charset.defaultCharset())

        textCode.addMouseMoveListener(object : MouseMoveListener {
            var tip: IToolTip? = null

            override fun mouseMove(e: MouseEvent) {
                tip?.delete()
                val offset = textCode.getOffsetAtPoint(Point(e.x, e.y))
                diagnosticStyles.find { r ->
                    offset in r.start..r.start + r.length
                }?.let { r ->
                    tip = addTooltip(
                        offset,
                        r.data.toString(),
                        IToolTip.Position.BELOW
                    )
                }
            }
        })

        textCode.addModifyListener {
            clearTooltips()
        }

    }

    private class ToolTip(
        val offset: Int,
        val message: String,
        val position: IToolTip.Position = IToolTip.Position.ONSPOT,
        val dispose: () -> Unit
    ) : IToolTip {

        override fun delete() {
            dispose()
        }
    }

    override fun addTooltip(
        location: SourceLocation,
        message: String,
        position: IToolTip.Position
    ): IToolTip {
        val shell = Shell(textCode.shell, SWT.ON_TOP)
        shell.text = "Tooltip"
        shell.layout = FillLayout()

        val offset =
            textCode.getOffsetAtLine(location.line - 1) + location.start + location.end - location.start

        return addTooltip(offset, message, position)
    }

    override fun addTooltip(
        offset: Int,
        message: String,
        position: IToolTip.Position
    ): IToolTip {
        val shell = Shell(textCode.shell, SWT.NONE)
        shell.text = "Tooltip"
        shell.layout = FillLayout()
        val tooltipLabel = Label(shell, SWT.NONE)
        tooltipLabel.text = message
        setLocation(offset, position, tooltipLabel, shell)
        shell.pack()
        shell.open()

        val listener =
            PaintListener { setLocation(offset, position, tooltipLabel, shell) }
        textCode.addPaintListener(listener)
        val tip = ToolTip(offset, message, position) {
            textCode.removePaintListener(listener)
            shell.dispose()
        }
        tooltips.add(tip)
        return tip
    }

    private fun setLocation(
        offset: Int,
        position: IToolTip.Position,
        tooltipLabel: Label,
        shell: Shell
    ) {
        val caretLocation = textCode.getLocationAtOffset(offset)
        shell.visible = caretLocation.y in 0..textCode.bounds.height &&
                caretLocation.x in 0..textCode.bounds.width
        val displayLocation = textCode.toDisplay(caretLocation)
        val (x, y) = when (position) {
            IToolTip.Position.ONSPOT -> Pair(
                displayLocation.x,
                displayLocation.y
            )
            IToolTip.Position.BELOW -> Pair(
                displayLocation.x,
                displayLocation.y + textCode.lineHeight
            )
            IToolTip.Position.OVER -> Pair(
                displayLocation.x,
                displayLocation.y - textCode.lineHeight
            )
            IToolTip.Position.LEFT -> Pair(
                displayLocation.x - tooltipLabel.computeSize(
                    SWT.DEFAULT,
                    SWT.DEFAULT
                ).x, displayLocation.y
            )
        }
        shell.setLocation(x, y)
    }

    override fun getProcedureOnCursor(): IProcedure? {
        val module: IModule = try {
            Java2Strudel().load(textCode.text)
        } catch (e: AssertionError) {
            null
        } ?: return null
        val line = textCode.getLineAtOffset(textCode.caretOffset) + 1
        var proc =
            module.getProcedure { it.getProperty(SourceLocation::class.java)?.line == line }
        if (proc == null) {
            module.procedures.forEach { p ->
                p.accept(object : IBlock.IVisitor {
                    override fun visitAny(element: IBlockElement) {
                        if (element.getProperty(SourceLocation::class.java)?.line == line)
                            proc = element.ownerProcedure
                    }
                })
            }
        }
        return proc
    }

    override fun clearTooltips() {
        textCode.display.syncExec {
            tooltips.forEach {
                it.dispose()
            }
        }
    }

    private fun errorRange(msg: String) = StyleRange().apply {
        underline = true
        underlineStyle = SWT.UNDERLINE_SQUIGGLE
        underlineColor = Display.getDefault().getSystemColor(SWT.COLOR_RED)
        data = msg
    }

    override fun compile(): Boolean {
        val writer = PrintWriter(file)
        writer.println(textCode.text)
        writer.close()
        val diagnostics = compileNoOutput(listOf(CompilationItem(file)))
        diagnosticStyles.clear()
        clearTooltips()
        diagnostics.filter { it.kind == Diagnostic.Kind.ERROR }.forEach {
            val range = errorRange(it.getMessage(null)).apply {
                val diff = (it.endPosition - it.startPosition).toInt()
                val len = if (diff == 0) 1 else diff
                start = max(
                    if (diff == 0) (it.startPosition - 1).toInt() else it.startPosition.toInt(),
                    0
                )
                length = len
            }
            diagnosticStyles.add(range)
        }

        val javaCompileOK =
            diagnostics.none { it.kind == Diagnostic.Kind.ERROR }

        fun strudelCompile(): Boolean {
            val module: IModule? = try {
                Java2Strudel().load(code)
            } catch (e: StrudelUnsupported) {
                if (e.locations.isEmpty()) {
                    val range =
                        errorRange("Unsupported: ${e.message}").apply {
                            start = 0
                            length = 1
                        }
                    diagnosticStyles.add(range)
                } else {
                    val range =
                        errorRange("Unsupported: ${e.message}").apply {
                            start = textCode.getOffsetAtLine(e.locations[0].line - 1) + e.locations[0].start - 1
                            length = e.locations[0].end - e.locations[0].start
                        }
                    diagnosticStyles.add(range)
                }
                null
            }
            return module != null
        }

        val strudelCompileOK = javaCompileOK && strudelCompile()

        // to force relayout of style (maybe there is a better way)
        val caretPos = textCode.caretOffset
        textCode.text = textCode.text
        textCode.caretOffset = caretPos

        return javaCompileOK && strudelCompileOK
    }
}