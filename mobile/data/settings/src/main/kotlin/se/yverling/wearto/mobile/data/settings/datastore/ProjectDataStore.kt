package se.yverling.wearto.mobile.data.settings.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.mobile.data.settings.model.Project
import javax.inject.Inject

internal class ProjectDataStore @Inject constructor(@param:ApplicationContext private val context: Context) {
    suspend fun persistProject(project: Project) {
        context.dataStore.updateData {
            it.toBuilder()
                .setId(project.id)
                .setName(project.name)
                .build()
        }
    }

    fun getProject(): Flow<Project?> = context.dataStore.data.map {
        if (it.id.isNullOrBlank() || it.name.isNullOrBlank()) null
        else Project(id = it.id, name = it.name)
    }

    suspend fun clearProject() {
        context.dataStore.updateData {
            it.toBuilder().clear().build()
        }
    }
}

internal val Context.dataStore: DataStore<se.yverling.wearto.Project> by dataStore(
    fileName = DATASTORE_FILE_NAME,
    serializer = ProjectSerializer
)
