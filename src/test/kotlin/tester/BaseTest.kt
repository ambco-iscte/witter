package tester

import pt.iscte.witter.testing.Test

open class BaseTest(protected val reference: String, protected val subject: String) {

    val tester = Test(reference)
}