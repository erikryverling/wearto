package se.yverling.wearto.core.entities

import androidx.room.Embedded

class ItemWithProject {
    @Embedded internal lateinit var item: Item
    @Embedded internal lateinit var project: Project

    override fun toString(): String = item.toString() + project.toString()
}