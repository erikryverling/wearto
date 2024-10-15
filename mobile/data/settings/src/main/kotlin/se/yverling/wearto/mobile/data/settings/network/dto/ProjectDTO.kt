package se.yverling.wearto.mobile.data.settings.network.dto

import kotlinx.serialization.Serializable
import se.yverling.wearto.mobile.data.settings.model.Project

@Serializable
data class ProjectDTO(val id: String, val name: String)

fun List<ProjectDTO>.toSortedProjects(): List<Project> =
    this.map { Project(id = it.id, name = it.name) }.sortedBy { it.name }
