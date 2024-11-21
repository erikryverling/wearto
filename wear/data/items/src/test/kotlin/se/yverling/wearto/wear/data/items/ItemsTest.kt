package se.yverling.wearto.wear.data.items

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test
import se.yverling.wearto.wear.data.items.db.toModelItems
import se.yverling.wearto.wear.data.items.model.toEntities
import se.yverling.wearto.wear.data.items.db.Item as ItemEntity
import se.yverling.wearto.wear.data.items.model.Item as ItemModel

private class ItemsTest {
    val itemEntity = ItemEntity(name = "Name")
    val itemModel = ItemModel(name = "Name")

    @Test
    fun `toModelItems should map successfully`() {
        listOf(itemEntity).toModelItems().shouldBeEqual(listOf(itemModel))
    }

    @Test
    fun `toEntities should map successfully`() {
        listOf(itemModel).toEntities().shouldBeEqual(listOf(itemEntity))
    }
}
