package se.yverling.wearto.mobile.data.settings.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.yverling.wearto.mobile.data.settings.model.Project

@Serializable
internal data class ProjectDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)

internal fun List<ProjectDto>.toSortedProjects(): List<Project> =
    this.map { Project(id = it.id, name = it.name) }.sortedBy { it.name }
