package se.yverling.wearto.mobile.data.settings

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.settings.network.dto.ProjectDto
import se.yverling.wearto.mobile.data.settings.network.dto.toSortedProjects

private class ProjectTest {
    val projectDto1 = ProjectDto(id = "1", name = "B")
    val projectDto2 = ProjectDto(id = "2", name = "A")

    val projectModel1 = Project(id = "2", name = "A")
    val projectModel2 = Project(id = "1", name = "B")

    @Test
    fun `toSortedProjects should map and sort correctly`() {
        listOf(projectDto1, projectDto2).toSortedProjects()
            .shouldBeEqual(listOf(projectModel1, projectModel2))
    }
}
