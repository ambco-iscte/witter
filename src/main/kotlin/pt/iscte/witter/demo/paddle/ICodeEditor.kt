package pt.iscte.witter.demo.paddle

import pt.iscte.strudel.javaparser.SourceLocation
import pt.iscte.strudel.model.IProcedure

interface ICodeEditor {

    fun compile(): Boolean

    fun addTooltip(offset: Int, message: String,  position: IToolTip.Position) : IToolTip

    fun addTooltip(location: SourceLocation, message: String,  position: IToolTip.Position) : IToolTip

    fun getProcedureOnCursor(): IProcedure?

    fun clearTooltips()

    val code: String
}