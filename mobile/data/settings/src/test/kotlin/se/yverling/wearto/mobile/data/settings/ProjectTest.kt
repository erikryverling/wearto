package se.yverling.wearto.mobile.data.settings

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDto
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectsDto
import se.yverling.wearto.mobile.data.settings.network.dto.toSortedProjects

private class ProjectTest {
    val projectsDto = ProjectsDto(
        results = listOf(
            ProjectDto(id = "1", name = "B"),
            ProjectDto(id = "2", name = "A")
        )
    )

    val projectModel1 = Project(id = "2", name = "A")
    val projectModel2 = Project(id = "1", name = "B")

    @Test
    fun `toSortedProjects should map and sort correctly`() {
        projectsDto.toSortedProjects().shouldBeEqual(listOf(projectModel1, projectModel2))
    }
}
