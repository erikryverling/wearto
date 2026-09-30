package se.yverling.wearto.mobile.data.settings.datastore

import kotlinx.coroutines.flow.Flow
import se.yverling.wearto.mobile.data.settings.model.Project

internal interface ProjectDataStore {
    suspend fun persistProject(project: Project)
    fun getProject(): Flow<Project?>
    suspend fun clearProject()
}
