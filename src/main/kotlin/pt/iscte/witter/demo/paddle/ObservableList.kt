package pt.iscte.witter.demo.paddle

class ObservableList<T>(val list: MutableList<T>) : MutableList<T> by list {
    val observers = mutableListOf<() -> Unit>()

    override fun add(element: T): Boolean {
        val r = list.add(element)
        observers.forEach { it() }
        return r
    }

    override fun clear() {
        list.clear()
        observers.forEach { it() }
    }
}