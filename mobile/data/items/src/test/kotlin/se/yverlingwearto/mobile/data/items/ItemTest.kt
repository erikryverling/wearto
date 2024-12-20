package se.yverlingwearto.mobile.data.items

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.items.db.toModels
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.db.Item as DbItem
import se.yverling.wearto.mobile.data.items.model.toEntity

private class ItemTest {
    val model = Item(uid = 1, name = "name")
    val entity = DbItem(uid = 1, name = "name")

    @Test
    fun `toEntity should map successfully`() {
        model.toEntity().shouldBe(entity)
    }

    @Test
    fun `toModels should map successfully`() {
        listOf(entity).toModels().shouldBe(listOf(model))
    }
}
