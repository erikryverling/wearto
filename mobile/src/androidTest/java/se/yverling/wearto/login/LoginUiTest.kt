package se.yverling.wearto.login

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.yverling.wearto.test.AuthenticationTestRule
import se.yverling.wearto.R
import se.yverling.wearto.test.TEST_TOKEN
import se.yverling.wearto.test.WearToTestRunner
import se.yverling.wearto.items.ItemsActivity

@RunWith(AndroidJUnit4::class)
class LoginUiTest {
    @get:Rule
    var activityRule: AuthenticationTestRule<LoginActivity> = AuthenticationTestRule(LoginActivity::class.java, false)

    @Test
    fun shouldLoginSuccessfully() {
        launchActivity()

        onView(withId(R.id.login_button))
                .check(matches(withText(activityRule.activity.getString(R.string.login_button))))
                .check(matches(not(isEnabled())))

        onView(withId(R.id.access_token_edit)).perform(typeText(TEST_TOKEN))

        onView(withId(R.id.login_button)).check(matches(isEnabled()))

        closeSoftKeyboard()

        onView(withId(R.id.login_button)).perform(click())

        intended(hasComponent(ItemsActivity::class.java.name))
    }

    @Test
    fun shouldShowErrorDialogWhenApiRespondsWithError() {
        returnHttpBadRequest()

        launchActivity()

        onView(withId(R.id.login_button))
                .check(matches(withText(activityRule.activity.getString(R.string.login_button))))
                .check(matches(not(isEnabled())))

        onView(withId(R.id.access_token_edit)).perform(typeText(TEST_TOKEN))

        onView(withId(R.id.login_button)).check(matches(isEnabled()))

        closeSoftKeyboard()

        onView(withId(R.id.login_button)).perform(click())

        assertLoginFailedTryAgainDialogIsShowing()
    }

    private fun returnHttpBadRequest() {
        val server = (InstrumentationRegistry.getInstrumentation() as WearToTestRunner).server
        server.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/sync/" -> MockResponse().setResponseCode(400).setBody("{\"projects\":[]}")
                    else -> MockResponse().setResponseCode(404)
                }
            }
        })
    }

    private fun launchActivity() {
        activityRule.launchActivity(Intent())
    }

    private fun assertLoginFailedTryAgainDialogIsShowing() {
        onView(withText((R.string.login_failed_title)))
                .inRoot(RootMatchers.isDialog())
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.message))
                .inRoot(RootMatchers.isDialog())
                .check(matches(withText(R.string.login_failed_due_to_general_error_message)))
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.button1))
                .check(matches(withText(R.string.try_again_button)))
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.button2))
                .check(matches(withText(R.string.cancel_button)))
                .check(matches(ViewMatchers.isDisplayed()))
    }
}