package pt.iscte.witter.testing

data class TestReport(val data: Map<String, List<ITestResult>>) {

    // TODO: method in Test that returns this

    fun size(): Int = data.size

    fun getAll(predicate: (ITestResult) -> Boolean = { true }): List<ITestResult> {
        val all = mutableListOf<ITestResult>()
        data.forEach { (_, results) ->
            results.forEach { if (predicate(it)) all.add(it) }
        }
        return all.toList()
    }

    fun save(path: String) {
        if (!path.endsWith(".html"))
            throw IllegalArgumentException("Report must be saved in an .html file! Did your path end in a file with extension \".html\"?")

        // TODO: save in pretty HTML
    }
}