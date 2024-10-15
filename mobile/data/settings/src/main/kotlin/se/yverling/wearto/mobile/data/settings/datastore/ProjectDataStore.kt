package se.yverling.wearto.mobile.data.settings.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun persistProject(project: String) {
        context.dataStore.edit { projects ->
            projects[projectKey] = project
        }
    }

    fun getProject(): Flow<String?> {
        return context.dataStore.data.map { projects ->
            projects[projectKey]
        }
    }

    suspend fun clearProject() {
        context.dataStore.edit { projects ->
            projects.remove(projectKey)
        }
    }

    companion object {
        private const val PROJECT_KEY = "project"
        private val projectKey = stringPreferencesKey(PROJECT_KEY)
    }
}

val Context.dataStore by preferencesDataStore(name = "project")
