package se.yverling.wearto.mobile.data.settings

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.common.network.exception.NetworkException
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDto
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectsDto

@ExtendWith(MockKExtension::class)
private class SettingsRepositoryImplTest {
    @RelaxedMockK
    lateinit var projectsEndpointMock: ProjectsEndpoint

    private lateinit var projectDataStore: FakeProjectDataStore
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeEach
    fun setup() {
        projectDataStore = FakeProjectDataStore()
        repository = SettingsRepositoryImpl(
            projectsEndpoint = projectsEndpointMock,
            projectDataStore = projectDataStore
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

        repository.getProjects().first() shouldBe sortedListOfProjectModels
    }

    @Test
    fun `getProjects should throw NetworkException when status code is not OK`() = runTest {
        val responseMock = mockk<HttpResponse>()

        every { responseMock.status } returns HttpStatusCode.InternalServerError
        coEvery { projectsEndpointMock.getProjects() } returns responseMock

        shouldThrow<NetworkException> {
            repository.getProjects().collect {}
        }
    }

    @Test
    fun `getProjects should throw NetworkException when endpoint throws transport exception`() = runTest {
        coEvery { projectsEndpointMock.getProjects() } throws java.io.IOException("Connection reset")

        shouldThrow<NetworkException> {
            repository.getProjects().collect {}
        }
    }

    @Test
    fun `getProject should emit successfully`() = runTest {
        val project = Project(id = "1", name = "A")
        projectDataStore.persistProject(project)

        repository.getProject().first() shouldBe project
    }

    @Test
    fun `setProject should call ProjectDataStore`() = runTest {
        val project = Project(id = "1", name = "A")

        repository.setProject(project)

        projectDataStore.getProject().first() shouldBe project
    }

    @Test
    fun `clearProject should call ProjectDataStore`() = runTest {
        val project = Project(id = "1", name = "A")
        projectDataStore.persistProject(project)

        repository.clearProject()

        projectDataStore.getProject().first().shouldBeNull()
    }
}

private class FakeProjectDataStore(
    initialProject: Project? = null,
) : ProjectDataStore {
    private val projectFlow = MutableStateFlow(initialProject)

    override fun getProject(): Flow<Project?> = projectFlow

    override suspend fun persistProject(project: Project) {
        projectFlow.value = project
    }

    override suspend fun clearProject() {
        projectFlow.value = null
    }
}
