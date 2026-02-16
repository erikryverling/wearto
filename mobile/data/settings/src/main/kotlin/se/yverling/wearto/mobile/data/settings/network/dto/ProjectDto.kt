package se.yverling.wearto.mobile.data.settings.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProjectDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)
