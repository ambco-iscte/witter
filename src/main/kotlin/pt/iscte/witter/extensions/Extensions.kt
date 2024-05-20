package pt.iscte.witter.extensions

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

internal fun getValidSubmissions(directory: File, name: String): Sequence<File> = directory.walkTopDown().filter {
    it.name == name && compile(it)
}

internal fun compile(
    file: File,
): Boolean {
    val javac = ToolProvider.getSystemJavaCompiler()
    val error = ByteArrayOutputStream()
    val diagnostics = DiagnosticCollector<JavaFileObject>()

    val fileManager = javac.getStandardFileManager(diagnostics, null, null)
    val units = fileManager.getJavaFileObjectsFromFiles(listOf(file))
    val task = javac.getTask(
        PrintWriter(error),
        fileManager,
        diagnostics,
        listOf("-classpath", file.parentFile.path),
        null,
        units
    )

    val success = task.call()
    fileManager.close()

    return success
}