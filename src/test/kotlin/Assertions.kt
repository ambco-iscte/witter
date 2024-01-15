import pt.iscte.strudel.vm.IValue
import pt.iscte.witter.testing.equivalent
import pt.iscte.witter.testing.sameAs
import kotlin.test.assertTrue

fun assertEquivalent(expected: IValue, actual: IValue) = assertTrue(
    expected.sameAs(actual),
    "Expected <$expected> but was <$actual>"
)

fun assertEquivalent(expected: Collection<IValue>, actual: Collection<IValue>) = assertTrue(
    expected.sameAs(actual),
    "Expected <$expected> but was <$actual>"
)

fun assertEquivalent(expected: Any?, actual: Any?) =
    assertTrue(equivalent(expected, actual), "Expected <$expected> but was <$actual>")