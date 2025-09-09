package se.yverling.wearto.mobile.data.item

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import se.yverling.wearto.mobile.data.item.network.TasksEndpoint
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import javax.inject.Inject

internal class ItemRepositoryImpl @Inject constructor(
    private val tasksEndpoint: TasksEndpoint,
    private val settingsRepository: SettingsRepository,
) : ItemRepository {
    override suspend fun addItem(itemName: String) {
        val project = settingsRepository.getProject().first()

        if (project == null) throw IllegalStateException("No project selected")

        val response = tasksEndpoint.addTask(
            projectId = project.id,
            itemName = itemName
        )

        if (response.status.value != HttpStatusCode.OK.value) {
            throw IllegalStateException("Add tasks request failed")
        }
    }
}
