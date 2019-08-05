package se.yverling.wearto.sync.network.dtos

import androidx.annotation.Keep

@Keep
data class SyncResponse(val projects: List<Project>)

@Keep
data class Project(val id: Long, val name: String, val color: Int)