package se.yverling.wearto.mobile.data.item.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import se.yverling.wearto.mobile.data.item.network.dto.RequestDto
import javax.inject.Inject
import javax.inject.Named

class TasksEndpoint @Inject constructor(@param:Named("todoist") private val client: HttpClient) {
    suspend fun addTask(projectId: String, itemName: String): HttpResponse = client.post("tasks") {
        contentType(ContentType.Application.Json)
        setBody(
            RequestDto(
                projectId = projectId,
                content = itemName,
            )
        )
    }
}
