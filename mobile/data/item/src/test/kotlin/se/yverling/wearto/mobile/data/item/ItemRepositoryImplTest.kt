package se.yverling.wearto.mobile.data.item

import io.kotest.assertions.throwables.shouldThrow
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.item.network.TasksEndpoint
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class ItemRepositoryImplTest {
    @RelaxedMockK
    lateinit var tasksEndpointMock: TasksEndpoint

    @Test
    fun `addItem should call SettingsRepository and TasksEndpoint`() = runTest {
        val settingsRepository = FakeSettingsRepository(project = Project(id = "Id", name = "Project"))
        mockResponse(HttpStatusCode.OK)

        val repository = createRepository(settingsRepository)
        repository.addItem(itemName = "Item")

        coVerify { tasksEndpointMock.addTask(projectId = "Id", itemName = "Item") }
    }

    @Test
    fun `addItem should throw if project is null`() = runTest {
        val settingsRepository = FakeSettingsRepository(project = null)

        val repository = createRepository(settingsRepository)

        shouldThrow<IllegalStateException> {
            repository.addItem("Item")
        }
    }

    @Test
    fun `addItem should throw if TasksEndpoint response is not successful`() = runTest {
        val settingsRepository = FakeSettingsRepository(project = Project(id = "Id", name = "Project"))
        mockResponse(HttpStatusCode.InternalServerError)

        val repository = createRepository(settingsRepository)

        shouldThrow<IllegalStateException> {
            repository.addItem(itemName = "Item")
        }
    }

    private fun createRepository(settingsRepository: SettingsRepository): ItemRepositoryImpl =
        ItemRepositoryImpl(tasksEndpointMock, settingsRepository)

    private fun mockResponse(httpStatusCode: HttpStatusCode) {
        val responseMock = mockk<HttpResponse>()
        every { responseMock.status } returns httpStatusCode
        coEvery { tasksEndpointMock.addTask(any(), any()) } returns responseMock
    }
}

private class FakeSettingsRepository(
    var project: Project? = null,
) : SettingsRepository {
    override fun getProject(): Flow<Project?> = flowOf(project)
    override fun getProjects(): Flow<List<Project>> = flowOf(emptyList())
    override suspend fun setProject(project: Project) { this.project = project }
    override suspend fun clearProject() { this.project = null }
}
