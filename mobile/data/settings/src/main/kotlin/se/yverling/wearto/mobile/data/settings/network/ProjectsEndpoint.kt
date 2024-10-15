package se.yverling.wearto.mobile.data.settings.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject
import javax.inject.Named

class ProjectsEndpoint @Inject constructor(@Named("todoist") private val client: HttpClient) {
    suspend fun getProjects(): HttpResponse = client.get("projects")
}
