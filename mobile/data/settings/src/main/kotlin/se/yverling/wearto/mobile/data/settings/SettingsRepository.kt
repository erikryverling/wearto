package se.yverling.wearto.mobile.data.settings

import kotlinx.coroutines.flow.Flow
import se.yverling.wearto.mobile.data.settings.model.Project

interface SettingsRepository {
    fun getProjects(): Flow<List<Project>>
    fun getProject(): Flow<String?>
    suspend fun setProject(project: String)
    suspend fun clearProject()
}
