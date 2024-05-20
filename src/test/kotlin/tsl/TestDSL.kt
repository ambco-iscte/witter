package tsl

import org.junit.jupiter.api.Test
import pt.iscte.witter.dsl.*
import pt.iscte.witter.tsl.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestDSL {

    private val src = "src/test/java/reference/BinarySearch.java"

    @Test
    fun testSuiteCreation() {
        val suite = TestSuite(referencePath = "") { }
        assertTrue(suite.referencePath == "")
        assertTrue(suite.description == "")
        assertTrue(suite.cases().isEmpty())
    }

    @Test
    fun testModules() {
        val suite = TestSuite(referencePath = src) {
            Case(CheckSideEffects + CheckArrayAllocations) {

            }

            Case(CountRecursiveCalls()) {

            }
        }
        val modules = suite.cases()

        assertEquals(2, modules.size)

        assertEquals(modules[0].metrics, CheckSideEffects + CheckArrayAllocations)
        assertEquals(modules[1].metrics, setOf(CountRecursiveCalls()))
    }

    @Test
    fun testModulesWithInstructions() {
        val suite = TestSuite(referencePath = src) {
            Case(CheckSideEffects + CheckArrayAllocations) {
                val x = ref("x") { call("search", listOf(1, 2, 3, 4, 5, 6, 7), 1) }
                call("search", listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), x)
            }
        }

        val statements = suite.cases().first().statements()
        println(statements.joinToString())

        assertTrue(statements[0] is VariableAssignment)
        assertTrue((statements[0] as VariableAssignment).initializer() is ProcedureCall)
        assertTrue(statements[1] is ProcedureCall)
        assertEquals((statements[1] as ProcedureCall).arguments, listOf(
            listOf(0, 1, 3, 7, 9, 11, 13, 17, 19), VariableReference(suite.cases()[0], "x")
        ))

    }
}