package pt.iscte.witter.testing

import kotlinx.html.*
import kotlinx.html.dom.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.truncate
import kotlin.reflect.KClass

data class TestReport(val suite: TestSuite, val data: Map<String, List<ITestResult>>, val title: String = "") {

    fun fileCount(): Int = data.size

    fun resultCount(): Int = data.values.sumOf { it.size }

    fun getAll(predicate: (ITestResult) -> Boolean = { true }): List<ITestResult> {
        val all = mutableListOf<ITestResult>()
        data.forEach { (_, results) ->
            results.forEach { if (predicate(it)) all.add(it) }
        }
        return all.toList()
    }

    inline fun <reified T: ITestResult> count(): Int = data.values.sumOf { it.count { res -> res is T } }

    fun countEach(): Map<KClass<out ITestResult>, Int> {
        val counts = mutableMapOf<KClass<out ITestResult>, Int>()
        data.forEach { (_, results) ->
            results.forEach {
                counts[it::class] = (counts[it::class] ?: 0) + 1
            }
        }
        return counts.toMap()
    }

    fun countPassed(path: String): Int = data[path]?.count { it is TestResult && it.passed } ?: 0

    fun save(path: String) {
        if (!path.endsWith(".html"))
            throw IllegalArgumentException("Report must be saved in an .html file!")

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().create.html {
            head {
                title("Witter Report - ${this@TestReport.title}")
                link(href = "https://fonts.googleapis.com/css?family=Montserrat", rel = "stylesheet")
            }
            body {
                div {
                    style = "margin-top:2em;"
                    attributes["align"] = "center"
                    h1 {
                        style = "font-family:Montserrat;font-size:4em"
                        +"Witter Report"
                    }
                    h2 {
                        style = "font-family:Montserrat;font-size:2em"
                        +this@TestReport.title
                    }
                }

                br
                br

                h3 {
                    style = "font-family:Montserrat;font-size:1.5em"
                    +"Technical Report"
                }
                ul {
                    style = "font-family:Montserrat;"
                    li {
                        +"Found ${this@TestReport.data.size} Java source code files matching reference file ${File(this@TestReport.suite.referencePath).name}."
                    }
                    br
                    li {
                        val failed = this@TestReport.getAll { it is FileLoadingError } as List<FileLoadingError>
                        val ratio = 100 * failed.size / this@TestReport.fileCount()
                        details {
                            summary {
                                +"Failed to load ${failed.size} files (approximately $ratio%)."
                            }
                            ul {
                                val grouped = failed.groupBy { it.cause::class.simpleName }
                                grouped.forEach { (name, lst) ->
                                    br
                                    details {
                                        summary { +"$name: ${lst.size}" }
                                        ul {
                                            lst.forEach {
                                                br
                                                li { +it.message }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    br
                    li {
                        val threw = this@TestReport.getAll { it is ExceptionThrown } as List<ExceptionThrown>
                        details {
                            summary {
                                + "${threw.size} statement(s) threw exceptions."
                            }
                            ul {
                                val grouped = threw.groupBy { it.exception::class.simpleName }
                                grouped.forEach { (name, lst) ->
                                    br
                                    details {
                                        summary { +"$name: ${lst.size}" }
                                        ul {
                                            lst.forEach {
                                                br
                                                li { +it.message }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    br
                    li {
                        val res = this@TestReport.getAll { it is TestResult }
                        val passes = res.count { it is TestResult && it.passed }

                        if (res.isEmpty()) {
                            +"Could not successfully execute any tests."
                        } else {
                            val ratio = 100 * passes / res.size
                            +"Successfully executed ${res.size} tests, of which $passes (approximately $ratio%) were passed."
                        }
                    }
                }

                br
                br

                h3 {
                    style = "font-family:Montserrat;font-size:1.5em"
                    +"Test Results"
                }

                br

                data.forEach { (path, results) ->
                    details {
                        summary {
                            style = "font-family:Montserrat;"
                            +path
                        }
                        p {
                            style = "font-family:Montserrat;"
                            +"Passed ${countPassed(path)} tests."
                        }
                        results.forEach {
                            pre {
                                style = "font-family:Montserrat;"
                                +it.toString()
                            }
                        }
                    }
                    br
                    br
                }
            }
        }

        File(path).writeText(document.serialize(prettyPrint = true), charset = charset("UTF-8"))

        println("Saved report of $title to file: $path")
    }
}