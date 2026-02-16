package se.yverling.wearto.mobile.data.settings

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.call.body
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDto
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectsDto

@ExtendWith(MockKExtension::class)
private class SettingsRepositoryImplTest {
    @RelaxedMockK
    lateinit var projectsEndpointMock: ProjectsEndpoint

    @RelaxedMockK
    lateinit var projectDataStoreMock: ProjectDataStore

    lateinit var repository: SettingsRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = SettingsRepositoryImpl(
            projectsEndpointMock,
            projectDataStoreMock
        )
    }

    @Test
    fun `getProjects should emit successfully`() = runTest {
        val responseMock = mockk<HttpResponse>()

        val projectsDtoWithUnsortedListOfProjects =
            ProjectsDto(
                results =
                    listOf(
                        ProjectDto(id = "1", name = "B"),
                        ProjectDto(id = "2", name = "A")
                    )
            )

        val sortedListOfProjectModels = listOf(
            Project(id = "2", name = "A"),
            Project(id = "1", name = "B")
        )

        every { responseMock.status } returns HttpStatusCode.OK
        coEvery { responseMock.body<ProjectsDto>() } returns projectsDtoWithUnsortedListOfProjects

        coEvery { projectsEndpointMock.getProjects() } returns responseMock

        repository.getProjects().collect {
            it.shouldBeEqual(sortedListOfProjectModels)
        }
    }

    @Test
    fun `getProjects should throw IllegalStateException when status code is not OK`() = runTest {
        val responseMock = mockk<HttpResponse>()

        every { responseMock.status } returns HttpStatusCode.InternalServerError
        coEvery { projectsEndpointMock.getProjects() } returns responseMock

        shouldThrow<IllegalStateException> {
            repository.getProjects().collect {}
        }
    }

    @Test
    fun `getProject should emit successfully`() = runTest {
        val project = Project(id = "1", name = "A")

        coEvery { projectDataStoreMock.getProject() } returns flowOf(project)

        repository.getProject().collect {
            it.shouldNotBeNull()
            it.shouldBeEqual(project)
        }
    }

    @Test
    fun `setProject should call ProjectDataStore`() = runTest {
        val project = Project(id = "1", name = "A")

        repository.setProject(project)

        coVerify { projectDataStoreMock.persistProject(project) }
    }

    @Test
    fun `clearProject should call ProjectDataStore`() = runTest {
        repository.clearProject()

        coVerify { projectDataStoreMock.clearProject() }
    }
}
