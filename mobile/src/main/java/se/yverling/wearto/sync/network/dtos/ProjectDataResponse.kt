package se.yverling.wearto.sync.network.dtos

import android.support.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ProjectDataResponse(val items: List<Item>)

@Keep
data class Item(val id: Long, @Json(name = "project_id") val projectId: Long, val content: String)