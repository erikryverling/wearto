package se.yverling.wearto.mobile.data.item.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RequestDto(
    @SerialName("project_id") val projectId: String,
    @SerialName("content") val content: String,
)
