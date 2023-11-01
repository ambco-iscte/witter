package pt.iscte.witter.demo.paddle

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URI
import javax.tools.*


data class CompilationItem(val file: File, val src: String) {
    constructor(file: File) : this(file, file.readText())
}

fun compileNoOutput(files: List<CompilationItem>) : List<Diagnostic<*>> {
    val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = DiagnosticCollector<JavaFileObject>()
    val standardFileManager = compiler.getStandardFileManager(null, null, null)
    val fileManager = MemoryFileManager(standardFileManager)
    val task: JavaCompiler.CompilationTask =
        compiler.getTask(
            null,
            fileManager,
            diagnostics,
            null,
            null,
            files.map { JavaSourceFromString(it.file.nameWithoutExtension, it.src) }
        )
    task.call()
    return diagnostics.diagnostics
}

internal class MemoryFileManager(fileManager: JavaFileManager?) :
    ForwardingJavaFileManager<JavaFileManager?>(fileManager) {

    private val classBytes: MutableMap<String, ByteArrayOutputStream> =
        HashMap()

    @Throws(IOException::class)
    override fun getJavaFileForOutput(
        location: JavaFileManager.Location,
        className: String,
        kind: JavaFileObject.Kind,
        sibling: FileObject
    ): JavaFileObject {
        return MemoryJavaFileObject(className, kind)
    }

    val classes: Map<String, ByteArray>
        get() {
            val classMap: MutableMap<String, ByteArray> = HashMap()
            for (className in classBytes.keys) {
                classMap[className] = classBytes[className]!!.toByteArray()
            }
            return classMap
        }

    private inner class MemoryJavaFileObject(
        private val name: String, kind: JavaFileObject.Kind
    ) :
        SimpleJavaFileObject(
            URI.create(
                "string:///" +
                        name.replace(
                            "\\.".toRegex(),
                            "/"
                        ) + kind.extension
            ), kind
        ) {
        private val byteCode: ByteArrayOutputStream = ByteArrayOutputStream()

        @Throws(IOException::class)
        override fun openOutputStream(): ByteArrayOutputStream {
            classBytes[name] = byteCode
            return byteCode
        }

        override fun delete(): Boolean {
            classBytes.remove(name)
            return true
        }

        val bytes: ByteArray
            get() = byteCode.toByteArray()
    }
}

internal class JavaSourceFromString(val filename: String, val code: String) :
    SimpleJavaFileObject(
        URI.create(
            "string:///" + filename.replace(
                '.',
                '/'
            ) + JavaFileObject.Kind.SOURCE.extension
        ),
        JavaFileObject.Kind.SOURCE
    ) {
    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return code
    }
}