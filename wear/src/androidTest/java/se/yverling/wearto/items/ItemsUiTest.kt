package se.yverling.wearto.items

import android.arch.persistence.room.Room
import android.content.Intent
import android.content.res.ColorStateList
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.TextView
import org.hamcrest.CoreMatchers.allOf
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
import se.yverling.wearto.core.di.WEARTO_DATABASE_NAME
import se.yverling.wearto.core.entities.Item
import java.util.*

@RunWith(AndroidJUnit4::class)
class ItemsUiTest {
    @get:Rule
    var activityRule: ActivityTestRule<ItemsActivity> = ActivityTestRule(ItemsActivity::class.java, true)

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.databaseBuilder(context, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()
        db.itemDao().deleteAll()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun shouldShowEmptyScreenSuccessfully() {
        launchActivity()

        onView(withId(R.id.empty_screen_text)).check(matches(isDisplayed()))
    }

    @Test
    fun showShowItemsSuccessfully() {
        db.itemDao().saveAll(listOf(
                Item(UUID.randomUUID().toString(), "Item1", "Inbox", 0),
                Item(UUID.randomUUID().toString(), "Item2", "Project1", 1),
                Item(UUID.randomUUID().toString(), "Item3", "Project2", 2)
        ))

        launchActivity()

        onView(allOf(withId(R.id.item), withText("Item1"))).check(matches(hasDrawableColor(0)))
        onView(allOf(withId(R.id.item), withText("Item2"))).check(matches(hasDrawableColor(1)))
        onView(allOf(withId(R.id.item), withText("Item3"))).check(matches(hasDrawableColor(2)))
    }

    /*
    TODO Add tests for receiving items from mobile and sending a selected item back.
         This is currently hard since we need to mock DataLayerClient and
         there seems to be issues with Kotlin + Mockito + Android
    */

    private fun launchActivity() {
        activityRule.launchActivity(Intent())
    }

    private fun hasDrawableColor(expectedColor: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("background color should be ").appendValue(expectedColor)
            }

            override fun matchesSafely(view: View): Boolean {
                val textView = view as TextView
                return textView.compoundDrawableTintList == ColorStateList.valueOf(expectedColor)
            }
        }
    }
}