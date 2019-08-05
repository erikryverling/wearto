package se.yverling.wearto.test

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import assertk.Assert
import assertk.assert
import assertk.assertions.isEqualTo
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

fun <T> whenever(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)!!

fun Assert<ObservableField<String>>.valueIsEqualTo(expected: String) {
    assert(actual.get()).isEqualTo(expected)
}

fun Assert<ObservableBoolean>.valueIsEqualTo(expected: Boolean) {
    assert(actual.get()).isEqualTo(expected)
}

class RxRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        return object: Statement() {
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
                RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
                base?.evaluate()
            }
        }
    }
}