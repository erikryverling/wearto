package se.yverling.wearto.items

import android.content.Intent
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.yverling.wearto.R
import se.yverling.wearto.chars.CharsActivity
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.di.WEARTO_DATABASE_NAME
import se.yverling.wearto.core.entities.Item
import java.util.*

@RunWith(AndroidJUnit4::class)
class CharsUiTest {
    @get:Rule
    var activityRule: ActivityTestRule<CharsActivity> = ActivityTestRule(CharsActivity::class.java, true)

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(context, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()
        db.itemDao().deleteAll()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun showShowCharsSuccessfully() {
        db.itemDao().saveAll(listOf(
                Item(UUID.randomUUID().toString(), "A", "Inbox", 0),
                Item(UUID.randomUUID().toString(), "A", "Project1", 1),
                Item(UUID.randomUUID().toString(), "A", "Project1", 1),
                Item(UUID.randomUUID().toString(), "B", "Project1", 1),
                Item(UUID.randomUUID().toString(), "B", "Project1", 1),
                Item(UUID.randomUUID().toString(), "B", "Project2", 2),
                Item(UUID.randomUUID().toString(), "C", "Project2", 2),
                Item(UUID.randomUUID().toString(), "C", "Project2", 2),
                Item(UUID.randomUUID().toString(), "C", "Project2", 2),
                Item(UUID.randomUUID().toString(), "C", "Project2", 2)
        ))

        launchActivity()

        onView(allOf(withId(R.id.item), withText("A")))
        onView(allOf(withId(R.id.item), withText("B")))
        onView(allOf(withId(R.id.item), withText("C")))
    }

    private fun launchActivity() {
        activityRule.launchActivity(Intent())
    }
}