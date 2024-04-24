package pt.iscte.witter.testing.report

import com.github.javaparser.StaticJavaParser
import kotlinx.html.*
import kotlinx.html.dom.*
import pt.iscte.strudel.javaparser.StrudelUnsupportedException
import pt.iscte.witter.testing.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory



data class TestReport(val suite: TestSuite, val data: Map<String, List<ITestResult>>, val title: String = "") {

    fun fileCount(): Int = data.size

    fun getAll(predicate: (ITestResult) -> Boolean = { true }): List<ITestResult> {
        val all = mutableListOf<ITestResult>()
        data.forEach { (_, results) ->
            results.forEach { if (predicate(it)) all.add(it) }
        }
        return all.toList()
    }

    inline fun <reified T: ITestResult> count(): Int = data.values.sumOf { it.count { res -> res is T } }

    fun countPassed(path: String): Int = data[path]?.count { it is TestResult && it.passed } ?: 0

    fun toHTML(path: String) {
        if (!path.endsWith(".html") && !path.endsWith(".htm"))
            throw IllegalArgumentException("Report must be saved in an .html file!")

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().create.html {
            head {
                title("Witter Report - ${this@TestReport.title}")
                link(href = "https://fonts.googleapis.com/css?family=Poppins:100,300,400,700,900", rel = "stylesheet")
                link(href = "https://fonts.googleapis.com/css?family=JetBrains Mono", rel = "stylesheet")
                script(src = "https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/run_prettify.js") {  }
            }
            body {
                style="color:#41494f; margin:auto"

                div {
                    style = "text-align: center;"

                    div {
                        style="padding-top:1em; padding-bottom: 8em;background: linear-gradient(150deg, rgba(2,0,36,1) 0%, rgba(9,9,121,1) 35%, rgba(0,71,255,1) 100%); padding-left: 5em; padding-right: 5em; color:white"
                        h1 {
                            style="font-family:Poppins, sans-serif;font-size:4em;font-weight:600;"
                            +"Witter Report"
                        }
                        h1 {
                            style="font-family:Poppins, sans-serif;font-size:2.5em;font-weight:400;"
                            +this@TestReport.title
                        }
                    }

                    div {
                        style="position:relative;margin-top:-3rem;margin-left:20%;margin-right:20%;background: white;box-shadow: 0 0.5rem 1rem gray;border-radius:0.25rem;"
                        div {
                            style="padding: 0.5em;"
                            p {
                                style = STYLE_FONT_POPPINS
                                +"Found ${this@TestReport.data.size} Java source code files matching reference file ${File(this@TestReport.suite.referencePath).name}."
                            }

                            p {
                                style = STYLE_FONT_POPPINS

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
                    }
                }

                br
                br

                div {
                    style = "margin: 2em;font-size:1.1em;"

                    h3 {
                        style = "font-family:Poppins, sans-serif;font-size:2em"
                        +"Reference Solution"
                    }

                    details {
                        style = STYLE_FONT_POPPINS
                        summary { +"Source Code" }
                        pre {
                            classes = setOf("prettyprint")
                            style = STYLE_FONT_POPPINS
                            +StaticJavaParser.parse(File(suite.referencePath)).toString()
                        }
                    }
                }

                br
                br

                div {
                    style = "margin: 2em;font-size:1.1em;"

                    h3 {
                        style = "font-family:Poppins, sans-serif;font-size:2em"
                        +"Technical Report"
                    }
                    ul {
                        style = STYLE_FONT_POPPINS
                        li {
                            val failed = this@TestReport.getAll { it is FileLoadingError } as List<FileLoadingError>
                            val ratio = 100 * failed.size / this@TestReport.fileCount()
                            details {
                                summary {
                                    +"Failed to load ${failed.size} files (approximately $ratio%)."
                                }
                                ul {
                                    val groupedByExceptionType = failed.groupBy { it.cause::class.simpleName }
                                    groupedByExceptionType.forEach { (name, lst) ->
                                        br
                                        details {
                                            summary {
                                                if (name != null && name.contains("Strudel"))
                                                    b { +"$name: ${lst.size}" }
                                                else
                                                    +"$name: ${lst.size}"
                                            }
                                            ul {
                                                if (name == "StrudelUnsupportedException") {
                                                    val groupedByStrudelUnsupportedNodeType = lst.groupBy {
                                                        (it.cause as StrudelUnsupportedException).getFirstNodeType()
                                                    }
                                                    groupedByStrudelUnsupportedNodeType.forEach { (nodeType, exceptionList) ->
                                                        br
                                                        details {
                                                            summary { +"${nodeType?.simpleName}: ${exceptionList.size}" }
                                                            ul {
                                                                exceptionList.forEach {
                                                                    li {
                                                                        pre {
                                                                            style = STYLE_FONT_JETBRAINS
                                                                            +it.message
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else
                                                    lst.forEach {
                                                        li {
                                                            pre {
                                                                style = STYLE_FONT_JETBRAINS
                                                                +it.message
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        br
                        li {
                            val threw = (this@TestReport.getAll {
                                it is ExceptionTestResult
                            } as List<ExceptionTestResult>).filter { !it.passed && it.actual != null }
                            details {
                                summary {
                                    + "${threw.size} statement(s) threw unexpected exceptions."
                                }
                                ul {
                                    val grouped = threw.groupBy { it.actual!!::class.simpleName }
                                    grouped.forEach { (name, lst) ->
                                        br
                                        details {
                                            summary { +"$name: ${lst.size}" }
                                            ul {
                                                lst.forEach {
                                                    li {
                                                        pre {
                                                            style = STYLE_FONT_JETBRAINS
                                                            +it.message
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                br
                br

                div {
                    style = "margin: 2em;font-size:1.1em;"

                    h3 {
                        style = "font-family:Poppins, sans-serif;font-size:2em"
                        +"Test Results"
                    }

                    ul {
                        data.forEach { (path, results) ->
                            li {
                                details {
                                    summary {
                                        style = STYLE_FONT_POPPINS
                                        +path
                                    }
                                    p {
                                        style = STYLE_FONT_POPPINS
                                        +"Passed ${countPassed(path)} tests."
                                    }
                                    results.forEach {
                                        pre {
                                            style = STYLE_FONT_JETBRAINS
                                            +("➤ $it")
                                        }
                                    }
                                }
                                br
                                br
                            }
                        }
                    }
                }
            }
        }

        File(path).writeText(document.serialize(), charset = charset("UTF-8"))

        println("Saved report of $title to file: $path")
    }
}