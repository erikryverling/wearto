package se.yverling.wearto.mobile.data.settings

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDto
import se.yverling.wearto.mobile.data.settings.network.dto.toSortedProjects
import javax.inject.Inject

internal class SettingsRepositoryImpl @Inject constructor(
    private val projectsEndpoint: ProjectsEndpoint,
    private val projectDataStore: ProjectDataStore,
) : SettingsRepository {
    override fun getProjects(): Flow<List<Project>> = flow {
        val response = projectsEndpoint.getProjects()

        when (response.status.value) {
            HttpStatusCode.OK.value -> emit(response.body<List<ProjectDto>>().toSortedProjects())
            else -> throw IllegalStateException("Get projects request failed")
        }
    }

    override fun getProject(): Flow<Project?> = projectDataStore.getProject()

    override suspend fun setProject(project: Project) {
        projectDataStore.persistProject(project)
    }

    override suspend fun clearProject() {
        projectDataStore.clearProject()
    }
}
