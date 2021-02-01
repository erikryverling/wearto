package se.yverling.wearto.core

import android.content.DialogInterface
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.yverling.wearto.R
import se.yverling.wearto.login.LoginActivity
import se.yverling.wearto.ui.errorTryAgainDialog
import se.yverling.wearto.ui.warningMessageDialog

@RunWith(AndroidJUnit4::class)
class DialogsUiTest {
    @get:Rule
    var activityRule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun shouldShowErrorDialogSuccessfully() {
        activityRule.runOnUiThread {
            errorTryAgainDialog(
                    activityRule.activity,
                    R.string.login_failed_title,
                    R.string.login_failed_due_to_general_error_message
            ) { _, _ -> }.show()
        }

        onView(withText((R.string.login_failed_title)))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.message))
                .inRoot(isDialog())
                .check(matches(withText(R.string.login_failed_due_to_general_error_message)))
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.button1))
                .check(matches(withText(R.string.try_again_button)))
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.button2))
                .check(matches(withText(R.string.cancel_button)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun shouldShowWarningDialogSuccessfully() {
        activityRule.runOnUiThread {
            warningMessageDialog(
                    activityRule.activity,
                    R.string.logout_warning_title,
                    R.string.logout_warning_message,
                    DialogInterface.OnClickListener { _, _ -> }
            ).show()
        }

        onView(withText((R.string.logout_warning_title)))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.message))
                .inRoot(isDialog())
                .check(matches(withText(R.string.logout_warning_message)))
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.button1))
                .check(matches(withText(R.string.ok_button)))
                .check(matches(isDisplayed()))

        onView(withId(android.R.id.button2))
                .check(matches(withText(R.string.cancel_button)))
                .check(matches(isDisplayed()))
    }
}
