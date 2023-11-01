package pt.iscte.witter.demo.paddle

interface IToolTip {
    enum class Position {
        ONSPOT, OVER, BELOW, LEFT
    }

    fun delete()
}