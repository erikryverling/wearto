package se.yverling.wearto.mobile.data.settings

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDTO
import se.yverling.wearto.mobile.data.settings.network.dto.toSortedProjects
import javax.inject.Inject

internal class SettingsRepositoryImpl @Inject constructor(
    private val projectsEndpoint: ProjectsEndpoint,
    private val projectDataStore: ProjectDataStore,
): SettingsRepository {
    override fun getProjects(): Flow<List<Project>> = flow {
        val response = projectsEndpoint.getProjects()

        when (response.status.value) {
            HttpStatusCode.OK.value -> emit(response.body<List<ProjectDTO>>().toSortedProjects())
            else -> throw IllegalStateException("Could not parse projects response with Ktor")
        }
    }

    override fun getProject(): Flow<String?> = projectDataStore.getProject()

    override suspend fun setProject(project: String) {
        projectDataStore.persistProject(project)
    }

    override suspend fun clearProject() {
        projectDataStore.clearProject()
    }
}
