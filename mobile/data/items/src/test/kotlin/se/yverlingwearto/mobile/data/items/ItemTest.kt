package se.yverlingwearto.mobile.data.items

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.model.toEntities
import se.yverling.wearto.mobile.data.items.db.Item as DbItem
import se.yverling.wearto.mobile.data.items.model.toEntity

class ItemTest {
    @Test
    fun `toEntity should map successfully`() {
        val model = Item(uid = 1, name = "name")
        val entity = DbItem(uid = 1, name = "name")

        model.toEntity().shouldBe(entity)
    }

    @Test
    fun `toEntities should map successfully`() {
        val model = Item(uid = 1, name = "name")
        val entity = DbItem(uid = 1, name = "name")

        listOf(model).toEntities().shouldBe(listOf(entity))
    }
}