package tester

import org.junit.jupiter.api.Test
import pt.iscte.strudel.javaparser.Java2Strudel
import java.io.File

class TestHeapSorting {

    @Test
    fun test() {
        val model = Java2Strudel().load(File("src/test/java/reference/HeapSorting.java"))
        println(model)
    }
}