package se.yverling.wearto.test

import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnitRunner
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import se.yverling.wearto.core.API_BASE_URL
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.di.SHARED_PREFERENCES_FILE_NAME
import se.yverling.wearto.core.di.WEARTO_DATABASE_NAME
import se.yverling.wearto.items.ADD_ITEM_TAP_TARGET_PREFERENCES_KEY
import se.yverling.wearto.items.SYNC_TAP_TARGET_PREFERENCES_KEY
import se.yverling.wearto.items.edit.LATEST_SELECTED_PROJECT_PREFERENCES_KEY
import se.yverling.wearto.sync.network.dtos.Project

internal const val FIRST_PROJECT_NAME = "Project1"
internal const val FIRST_PROJECT_COLOR = 1
internal const val SECOND_PROJECT_NAME = "Project2"
internal const val SECOND_PROJECT_COLOR = 2

class WearToTestRunner : AndroidJUnitRunner() {
    internal val server = MockWebServer()

    private lateinit var dbClient: DatabaseClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)

        val context = InstrumentationRegistry.getTargetContext()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()
        dbClient = DatabaseClient(context, db)

        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

        removeAllItems(dbClient)
        replaceWithTestProjects(dbClient)

        setTapTargetsAsDone()
        resetLastRememberedProject()

        server.setDispatcher(WearToDispatcher())
        Completable.fromRunnable {
            server.start()
            API_BASE_URL = server.url("/").toString()
        }
                .subscribeOn(Schedulers.io())
                .blockingAwait()
    }

    private fun resetLastRememberedProject() {
        val editor = sharedPreferences.edit()
        editor.remove(LATEST_SELECTED_PROJECT_PREFERENCES_KEY)
        editor.apply()
    }

    private fun setTapTargetsAsDone() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ADD_ITEM_TAP_TARGET_PREFERENCES_KEY, true)
        editor.putBoolean(SYNC_TAP_TARGET_PREFERENCES_KEY, true)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        server.shutdown()
        dbClient.close()
    }

    private fun removeAllItems(dbClient: DatabaseClient) {
        dbClient.deleteAllItems()
                .subscribeOn(Schedulers.io())
                .blockingAwait()
    }

    private fun replaceWithTestProjects(dbClient: DatabaseClient) {
        dbClient.replaceAllProjects(
                listOf(
                        Project(0, "Inbox", 0),
                        Project(1, FIRST_PROJECT_NAME, FIRST_PROJECT_COLOR),
                        Project(2, SECOND_PROJECT_NAME, SECOND_PROJECT_COLOR)
                )
        )
                .subscribeOn(Schedulers.io())
                .blockingAwait()
    }

    class WearToDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                "/sync/" -> MockResponse().setResponseCode(200).setBody("{\"projects\":[]}")
                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}