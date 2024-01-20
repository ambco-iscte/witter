import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite
import tester.TestBinarySearch
import tester.TestInsertionSort
import tester.TestRecursiveFactorial
import tester.TestStack
import tsl.TestDSL
import tsl.TestJava2Kotlin
import tsl.TestTSLParser

@Suite
@SelectClasses(
    TestJava2Kotlin::class,
    TestTSLParser::class,
    TestDSL::class,
    TestBinarySearch::class,
    TestInsertionSort::class,
    TestRecursiveFactorial::class,
    TestStack::class
)
class RunAllTests