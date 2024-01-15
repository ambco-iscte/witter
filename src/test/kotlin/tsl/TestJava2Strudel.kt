package tsl

import assertEquivalent
import pt.iscte.strudel.javaparser.Java2Strudel
import pt.iscte.strudel.model.DOUBLE
import pt.iscte.strudel.model.IModule
import pt.iscte.strudel.model.INT
import pt.iscte.strudel.vm.IVirtualMachine
import pt.iscte.strudel.vm.NULL
import pt.iscte.witter.testing.EvaluationMetricListener
import pt.iscte.witter.tsl.JavaArgument2Strudel
import pt.iscte.witter.tsl.TestCase
import java.io.File
import kotlin.test.Test

class TestJava2Strudel {

    private val ref: String = "src/test/java/reference/Stack.java"

    private val test = pt.iscte.witter.testing.Test(ref)
    private val vm: IVirtualMachine = IVirtualMachine.create()
    private val module: IModule = Java2Strudel().load(File(ref))
    private val listener = EvaluationMetricListener(
        vm,
        TestCase(module, listOf(), "", setOf())
    )

    private val parser = JavaArgument2Strudel(test, vm, module, listener)

    @Test
    fun testIntegerLiteralExpr() {
        assertEquivalent(vm.getValue(1), parser.translate("1"))
        assertEquivalent(vm.getValue(0), parser.translate("0"))
        assertEquivalent(vm.getValue(-1), parser.translate("-1"))
    }

    @Test
    fun testDoubleLiteralExpr() {
        assertEquivalent(vm.getValue(1.41), parser.translate("1.41"))
        assertEquivalent(vm.getValue(2.78), parser.translate("2.78"))
        assertEquivalent(vm.getValue(-3.14), parser.translate("-3.14"))
        assertEquivalent(vm.getValue(0.0), parser.translate("0.0"))
    }

    @Test
    fun testCharLiteralExpr() {
        assertEquivalent(vm.getValue('a'), parser.translate("'a'"))
        assertEquivalent(vm.getValue('b'), parser.translate("'b'"))
        assertEquivalent(vm.getValue('c'), parser.translate("'c'"))
        assertEquivalent(vm.getValue('d'), parser.translate("'d'"))
    }

    @Test
    fun testStringLiteralExpr() {
        assertEquivalent(vm.getValue("a"), parser.translate("\"a\""))
        assertEquivalent(vm.getValue("b"), parser.translate("\"b\""))
        assertEquivalent(vm.getValue("c"), parser.translate("\"c\""))
        assertEquivalent(vm.getValue("d"), parser.translate("\"d\""))
    }

    @Test
    fun testBooleanLiteralExpr() {
        assertEquivalent(vm.getValue(true), parser.translate("true"))
        assertEquivalent(vm.getValue(false), parser.translate("false"))
    }

    @Test
    fun testNullLiteralExpr() {
        assertEquivalent(vm.getValue(null), parser.translate("null"))
        assertEquivalent(NULL, parser.translate("null"))
    }

    @Test
    fun testArrayCreationExpr() {
        assertEquivalent(
            vm.allocateArrayOf(INT, 1, 2, 3, 4, 5),
            parser.translate("new int[] {1, 2, 3, 4, 5}")
        )

        assertEquivalent(
            vm.allocateArrayOf(INT, 1, -2, 3, -4, 5),
            parser.translate("new int[] {1, -2, 3, -4, 5}")
        )

        assertEquivalent(
            vm.allocateArrayOf(INT),
            parser.translate("new int[] {}")
        )

        assertEquivalent(
            vm.allocateArrayOf(DOUBLE, 1.41, 2.78, 3.14),
            parser.translate("new double[] {1.41, 2.78, 3.14}")
        )

        assertEquivalent(
            vm.allocateArrayOf(DOUBLE, 1.41, -2.78, -3.14),
            parser.translate("new double[] {1.41, -2.78, -3.14}")
        )
    }

    @Test
    fun testObjectCreationExpr() {
        val obj = vm.allocateRecord(module.getRecordType("Stack"))
        val constructor = module.getProcedure("\$init")
        vm.execute(constructor, obj, vm.getValue(5))

        assertEquivalent(
            obj,
            parser.translate("new Stack(5)")
        )
    }
}