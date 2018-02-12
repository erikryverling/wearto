package se.yverling.wearto.sync.network.dtos

import android.support.annotation.Keep
import java.util.*

@Keep
data class AddItemCommand(
        val args: Args,
        val type: String = "item_add",
        val temp_id: String = UUID.randomUUID().toString(),
        val uuid: String = UUID.randomUUID().toString())

@Keep
data class Args(val project_id: String, val content: String, val item_order: Int = 1)