package se.yverling.wearto.test

import android.app.Activity
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.intent.rule.IntentsTestRule
import io.reactivex.schedulers.Schedulers
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.di.SHARED_PREFERENCES_FILE_NAME

internal const val TEST_TOKEN = "test-access-token"

class AuthenticationTestRule<T : Activity>(activityClass: Class<T>, private var authenticated: Boolean) :
        IntentsTestRule<T>(activityClass, false, false) {

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

        val sharedPreferences = InstrumentationRegistry
                .getInstrumentation().targetContext
                .getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

        if (authenticated) {
            TokenManager(sharedPreferences).persistToken(TEST_TOKEN).subscribeOn(Schedulers.io()).blockingAwait()
        } else {
            TokenManager(sharedPreferences).removeAccessToken().subscribeOn(Schedulers.io()).blockingAwait()
        }
    }
}