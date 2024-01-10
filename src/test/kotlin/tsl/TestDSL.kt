package tsl

import org.junit.jupiter.api.Test
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.*
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDSL {

    private val src = "src/test/java/reference/BinarySearch.java"

    @Test
    fun testSuiteCreation() {
        val suite = Suite(referencePath = "") { }
        assertTrue(suite.referencePath == "")
        assertTrue(suite.description == "")
        assertTrue(suite.modules().isEmpty())
    }

    @Test
    fun testModules() {
        val suite = Suite(referencePath = src) {
            Stateless(CheckSideEffects + CheckArrayAllocations) {

            }

            Stateful(CountRecursiveCalls()) {

            }
        }
        val modules = suite.modules()

        assertEquals(2, modules.size)

        assertFalse(modules[0].stateful)
        assertTrue(modules[1].stateful)

        assertEquals(modules[0].metrics, CheckSideEffects + CheckArrayAllocations)
        assertEquals(modules[1].metrics, setOf(CountRecursiveCalls()))
    }

    @Test
    fun testModulesWithInstructions() {
        val suite = Suite(referencePath = src) {
            Stateless(CheckSideEffects + CheckArrayAllocations) {
                val x = Var("x") { Call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1) }
                Call("search", listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), x)
            }
        }

        val statements = suite.modules().first().statements()
        println(statements.joinToString())

        assertTrue(statements[0] is VariableAssignment)
        assertTrue((statements[0] as VariableAssignment).initializer() is ProcedureCall)
        assertTrue(statements[1] is ProcedureCall)
        assertEquals((statements[1] as ProcedureCall).arguments, listOf(
            listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), VariableReference("x")
        ))

    }
}