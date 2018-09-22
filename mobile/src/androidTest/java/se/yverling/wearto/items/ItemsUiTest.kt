package se.yverling.wearto.items

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.Intents.times
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import android.view.View
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.yverling.wearto.R
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.di.SHARED_PREFERENCES_FILE_NAME
import se.yverling.wearto.core.di.WEARTO_DATABASE_NAME
import se.yverling.wearto.core.projectColorToColorHex
import se.yverling.wearto.login.LoginActivity
import se.yverling.wearto.test.AuthenticationTestRule
import se.yverling.wearto.test.FIRST_PROJECT_COLOR
import se.yverling.wearto.test.FIRST_PROJECT_NAME
import se.yverling.wearto.test.SECOND_PROJECT_COLOR
import se.yverling.wearto.test.SECOND_PROJECT_NAME

private const val FIRST_ITEM_NAME = "Item1"

@RunWith(AndroidJUnit4::class)
class ItemsUiTest {
    @get:Rule
    var activityRule: AuthenticationTestRule<ItemsActivity> = AuthenticationTestRule(ItemsActivity::class.java, true)

    private lateinit var context: Context
    private lateinit var dbClient: DatabaseClient

    @Before
    fun setup() {
        context = InstrumentationRegistry.getTargetContext()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()
        dbClient = DatabaseClient(context, db)
        dbClient.deleteAllItems().subscribeOn(Schedulers.io()).blockingAwait()
    }

    @After
    fun teardown() {
        dbClient.close()
    }

    // TODO Fix. Race?
    @Test
    fun shouldShowTapTargetSuccessfully() {
        setTapTargetsAsUndone()

        launchActivity()

        onView(withId(R.id.material_target_prompt_view)).check(matches(isDisplayed()))

        onView(withId(R.id.add_item_button)).perform(click())

        onView(withId(R.id.edit_text)).perform(typeText(FIRST_ITEM_NAME))

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        onView(withId(R.id.material_target_prompt_view)).check(matches(isDisplayed()))

        onView(withId(R.id.items_layout)).perform(click())

        onView(withId(R.id.material_target_prompt_view)).check(doesNotExist())
    }

    @Test
    fun shouldShowEmptyScreenSuccessfully() {
        launchActivity()

        onView(withId(R.id.empty_screen_text)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldCreateItemSuccessfully() {
        launchActivity()

        onView(withId(R.id.add_item_button)).perform(click())

        onView(withId(R.id.edit_text)).perform(typeText(FIRST_ITEM_NAME))

        onView(withId(R.id.spinner)).perform(click())

        onView(withText(FIRST_PROJECT_NAME)).check(matches(isDisplayed()))
        onView(withText(SECOND_PROJECT_NAME)).perform(click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        onView(withId(R.id.item_name)).check(matches(withText(FIRST_ITEM_NAME)))
        onView(withId(R.id.project_name))
                .check(matches(withText(SECOND_PROJECT_NAME)))
                .check(matches(hasBackgroundColor(projectColorToColorHex(context, SECOND_PROJECT_COLOR))))
    }

    @Test
    fun shouldEditItemSuccessfully() {
        addTestItem()

        launchActivity()

        onView(withId(R.id.items_list_item)).perform(click())

        onView(withId(R.id.edit_text)).perform(typeText("-edit"))
        onView(withId(R.id.spinner)).perform(click())

        onView(withText(FIRST_PROJECT_NAME)).check(matches(isDisplayed()))
        onView(withText(SECOND_PROJECT_NAME)).perform(click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        onView(withId(R.id.item_name)).check(matches(withText("$FIRST_ITEM_NAME-edit")))

        onView(withId(R.id.project_name))
                .check(matches(withText(SECOND_PROJECT_NAME)))
                .check(matches(hasBackgroundColor(projectColorToColorHex(context, SECOND_PROJECT_COLOR))))
    }

    @Test
    fun shouldDeleteItemSuccessfully() {
        addTestItem()

        launchActivity()

        onView(withId(R.id.items_list_item)).perform(click())

        onView(withId(R.id.delete_item_action)).perform(click())

        onView(withId(R.id.items)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_screen_text)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldNotBeAbleToSaveItemWithEmptyName() {
        launchActivity()

        onView(withId(R.id.add_item_button)).perform(click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        onView(withText(R.string.item_name_is_required_label)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldNotSaveItemWhenBackingOut() {
        addTestItem()

        launchActivity()

        onView(withId(R.id.items_list_item)).perform(click())

        onView(withId(R.id.edit_text)).perform(typeText("-edit"))

        pressBack()
        pressBack()

        onView(withId(R.id.item_name)).check(matches(withText(FIRST_ITEM_NAME)))
        onView(withId(R.id.project_name))
                .check(matches(withText(FIRST_PROJECT_NAME)))
                .check(matches(hasBackgroundColor(projectColorToColorHex(context, FIRST_PROJECT_COLOR))))
    }

    @Test
    fun shouldRememberLastProject() {
        launchActivity()

        onView(withId(R.id.add_item_button)).perform(click())

        onView(withId(R.id.edit_text)).perform(typeText(FIRST_ITEM_NAME))
        onView(withId(R.id.spinner)).perform(click())

        onView(withText(SECOND_PROJECT_NAME)).perform(click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        onView(withId(R.id.add_item_button)).perform(click())

        onView(withId(R.id.project_name)).check(matches(withText(SECOND_PROJECT_NAME)))
    }

    @Test
    fun shouldLogOutSuccessfully() {
        addTestItem()

        launchActivity()

        openActionBarOverflowOrOptionsMenu(context)

        onView(withText(R.string.logout_item)).perform(click())

        assertLogoutWarningDialogIsShowing()

        onView(withId(android.R.id.button1)).perform(click())

        intended(IntentMatchers.hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun shouldCancelLogoutWhenCancelingDialog() {
        addTestItem()

        launchActivity()

        openActionBarOverflowOrOptionsMenu(context)

        onView(withText(R.string.logout_item)).perform(click())

        assertLogoutWarningDialogIsShowing()

        onView(withId(android.R.id.button2)).perform(click())

        intended(IntentMatchers.hasComponent(LoginActivity::class.java.name), times(0))

        onView(withId(R.id.item_name)).check(matches(withText(FIRST_ITEM_NAME)))

        onView(withId(R.id.project_name))
                .check(matches(withText(FIRST_PROJECT_NAME)))
                .check(matches(hasBackgroundColor(projectColorToColorHex(context, FIRST_PROJECT_COLOR))))
    }

    /*
    TODO Add tests for syncing and receiving a selectable item from wear.
         This is currently hard since we need to mock DataLayerClient and
         there seems to be issues with Kotlin + Mockito + Android
    */

    private fun setTapTargetsAsUndone() {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(ADD_ITEM_TAP_TARGET_PREFERENCES_KEY, false)
        editor.putBoolean(SYNC_TAP_TARGET_PREFERENCES_KEY, false)
        editor.apply()
    }

    private fun addTestItem() {
        dbClient.saveItem(FIRST_ITEM_NAME, FIRST_PROJECT_NAME)
                .subscribeOn(Schedulers.io())
                .blockingAwait()
    }

    private fun launchActivity() {
        activityRule.launchActivity(Intent())
    }

    private fun assertLogoutWarningDialogIsShowing() {
        onView(withText((R.string.logout_warning_title)))
                .inRoot(RootMatchers.isDialog())
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.message))
                .inRoot(RootMatchers.isDialog())
                .check(matches(withText(R.string.logout_warning_message)))
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.button1))
                .check(matches(withText(R.string.ok_button)))
                .check(matches(ViewMatchers.isDisplayed()))

        onView(withId(android.R.id.button2))
                .check(matches(withText(R.string.cancel_button)))
                .check(matches(ViewMatchers.isDisplayed()))
    }


    private fun hasBackgroundColor(expectedColor: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("background color should be ").appendValue(expectedColor)
            }

            override fun matchesSafely(view: View): Boolean {
                val stateList: ColorStateList = view.backgroundTintList
                return stateList == ColorStateList.valueOf(expectedColor)
            }
        }
    }
}
