package se.yverling.wearto.mobile.data.settings

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.common.network.exception.NetworkException
import se.yverling.wearto.mobile.common.network.exception.NoTokenException
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectsDto
import se.yverling.wearto.mobile.data.settings.network.dto.toSortedProjects
import javax.inject.Inject

internal class SettingsRepositoryImpl @Inject constructor(
    private val projectsEndpoint: ProjectsEndpoint,
    private val projectDataStore: ProjectDataStore,
) : SettingsRepository {
    override fun getProjects(): Flow<List<Project>> = flow {
        val response = try {
            projectsEndpoint.getProjects()
        } catch (e: CancellationException) {
            throw e
        } catch (e: InvalidTokenException) {
            throw e
        } catch (e: NoTokenException) {
            throw e
        } catch (e: Exception) {
            throw NetworkException("Failed to fetch projects", e)
        }

        when (response.status.value) {
            HttpStatusCode.OK.value -> emit(response.body<ProjectsDto>().toSortedProjects())
            else -> throw NetworkException("Get projects request failed with status: ${response.status}")
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
