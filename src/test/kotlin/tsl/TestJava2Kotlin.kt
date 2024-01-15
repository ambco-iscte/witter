package tsl

import org.junit.jupiter.api.Test
import pt.iscte.strudel.model.DOUBLE
import pt.iscte.strudel.model.INT
import pt.iscte.strudel.vm.NULL
import pt.iscte.witter.tsl.Java2Kotlin
import reference.Stack
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestJava2Kotlin {

    private val parser = Java2Kotlin()

    @Test
    fun testIntegerLiteralExpr() {
        assertEquals(1, parser.translate("1"))
        assertEquals(0, parser.translate("0"))
        assertEquals(-1, parser.translate("-1"))
    }

    @Test
    fun testDoubleLiteralExpr() {
        assertEquals(1.41, parser.translate("1.41"))
        assertEquals(2.78, parser.translate("2.78"))
        assertEquals(-3.14, parser.translate("-3.14"))
        assertEquals(0.0, parser.translate("0.0"))
    }

    @Test
    fun testCharLiteralExpr() {
        assertEquals('a', parser.translate("'a'"))
        assertEquals('b', parser.translate("'b'"))
        assertEquals('c', parser.translate("'c'"))
        assertEquals('d', parser.translate("'d'"))
    }

    @Test
    fun testStringLiteralExpr() {
        assertEquals("a", parser.translate("\"a\""))
        assertEquals("b", parser.translate("\"b\""))
        assertEquals("c", parser.translate("\"c\""))
        assertEquals("d", parser.translate("\"d\""))
    }

    @Test
    fun testBooleanLiteralExpr() {
        assertEquals(true, parser.translate("true"))
        assertEquals(false, parser.translate("false"))
    }

    @Test
    fun testNullLiteralExpr() {
        assertEquals(null, parser.translate("null"))
    }

    @Test
    fun testArrayCreationExpr() {
        assertEquals(
            listOf<Int>(1, 2, 3, 4, 5),
            parser.translate("new int[] {1, 2, 3, 4, 5}")
        )

        assertEquals(
            listOf<Int>(1, -2, 3, -4, 5),
            parser.translate("new int[] {1, -2, 3, -4, 5}")
        )

        assertEquals(
            listOf<Int>(),
            parser.translate("new int[] {}")
        )

        assertEquals(
            listOf<Double>(1.41, 2.78, 3.14),
            parser.translate("new double[] {1.41, 2.78, 3.14}")
        )

        assertEquals(
            listOf<Double>(1.41, -2.78, -3.14),
            parser.translate("new double[] {1.41, -2.78, -3.14}")
        )
    }

    @Test
    fun testObjectCreationExpr() {
        assertTrue(parser.translate("new reference.Stack(5)") is Stack)
    }
}