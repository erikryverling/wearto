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

    @RelaxedMockK
    lateinit var settingsRepositoryMock: SettingsRepository

    lateinit var repository: ItemRepositoryImpl

    @Test
    fun `addItem should call SettingsRepository and TasksEndpoint`() = runTest {
        every { settingsRepositoryMock.getProject() } returns flowOf(Project(id = "Id", name = "Project"))

        mockResponse(HttpStatusCode.OK)

        repository = createRepository()

        repository.addItem(itemName = "Item")

        coVerify { settingsRepositoryMock.getProject() }
        coVerify { tasksEndpointMock.addTask(any(), "Item") }
    }

    @Test
    fun `addItem should throw if project is null`() = runTest {
        every { settingsRepositoryMock.getProject() } returns flowOf(null)

        repository = createRepository()

        shouldThrow<IllegalStateException> {
            repository.addItem("Item")
        }
    }

    @Test
    fun `addItem should throw if TasksEndpoint response is not successful`() = runTest {
        every { settingsRepositoryMock.getProject() } returns flowOf(Project(id = "Id", name = "Project"))

        mockResponse(HttpStatusCode.InternalServerError)

        repository = createRepository()

        shouldThrow<IllegalStateException> {
            repository.addItem(itemName = "Item")
        }
    }

    private fun createRepository(): ItemRepositoryImpl = ItemRepositoryImpl(tasksEndpointMock, settingsRepositoryMock)

    private fun mockResponse(httpStatusCode: HttpStatusCode) {
        val responseMock = mockk<HttpResponse>()
        every { responseMock.status } returns httpStatusCode
        coEvery { tasksEndpointMock.addTask(any(), any()) } returns responseMock
    }
}
