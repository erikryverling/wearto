package se.yverling.wearto.mobile.data.settings.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.yverling.wearto.mobile.data.settings.model.Project

@Serializable
internal data class ProjectsDto(
    @SerialName("results")
    val results: List<ProjectDto>
)

internal fun ProjectsDto.toSortedProjects(): List<Project> =
    results.map { Project(id = it.id, name = it.name) }.sortedBy { it.name }
