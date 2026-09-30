package se.yverling.wearto.wear.data.items

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.yverling.wearto.wear.data.items.db.ItemsDao
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.db.Item as DbItem

private class ItemsRepositoryImplTest {
    private lateinit var dao: FakeItemsDao
    private lateinit var repository: ItemsRepositoryImpl

    private val item = Item(name = "name")

    @BeforeEach
    fun setUp() {
        dao = FakeItemsDao()
        repository = ItemsRepositoryImpl(dao)
    }

    @Test
    fun `getItems should call dao correctly`() = runTest {
        repository.replaceItems(listOf(item))

        repository.getItems().first() shouldBe listOf(item)
    }

    @Test
    fun `replaceItems should call dao correctly`() = runTest {
        val oldItem = Item(name = "old")
        repository.replaceItems(listOf(oldItem))
        repository.replaceItems(listOf(item))

        repository.getItems().first() shouldBe listOf(item)
    }
}

private class FakeItemsDao : ItemsDao {
    private val items = MutableStateFlow<List<DbItem>>(emptyList())

    override fun getItems(): Flow<List<DbItem>> = items

    override suspend fun setItems(items: List<DbItem>) {
        this.items.value = items
    }

    override suspend fun deleteAllItems() {
        items.value = emptyList()
    }
}
