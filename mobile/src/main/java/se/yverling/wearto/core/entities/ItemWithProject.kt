package se.yverling.wearto.core.entities

import android.arch.persistence.room.Embedded

class ItemWithProject {
    @Embedded internal lateinit var item: Item
    @Embedded internal lateinit var project: Project

    override fun toString(): String = item.toString() + project.toString()
}