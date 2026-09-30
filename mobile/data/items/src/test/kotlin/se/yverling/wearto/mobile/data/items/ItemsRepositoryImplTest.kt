package se.yverling.wearto.mobile.data.items

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.items.db.ItemsDao
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.model.toEntity
import se.yverling.wearto.mobile.data.items.db.Item as DbItem

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
        repository.setItem(item)

        repository.getItems().first() shouldBe listOf(item)
    }

    @Test
    fun `setItem should call dao correctly`() = runTest {
        repository.setItem(item)

        dao.items.value shouldBe listOf(item.toEntity())
    }

    @Test
    fun `setItems should call dao correctly`() = runTest {
        val item2 = Item(name = "name2")
        repository.setItems(listOf(item, item2))

        repository.getItems().first() shouldBe listOf(item, item2)
    }

    @Test
    fun `deleteItem should call dao correctly`() = runTest {
        repository.setItem(item)
        repository.deleteItem(item)

        dao.items.value.shouldBeEmpty()
    }

    @Test
    fun `clearItems should call dao correctly`() = runTest {
        repository.setItem(item)
        repository.clearItems()

        dao.items.value.shouldBeEmpty()
    }
}

private class FakeItemsDao : ItemsDao {
    val items = MutableStateFlow<List<DbItem>>(emptyList())

    override fun getItems(): Flow<List<DbItem>> = items

    override fun getItem(uid: Int): Flow<DbItem?> = items.map { list -> list.find { it.uid == uid } }

    override suspend fun upsertItem(item: DbItem) {
        items.value = items.value.filterNot { it.name == item.name } + item
    }

    override suspend fun insertItems(items: List<DbItem>) {
        this.items.value += items
    }

    override suspend fun deleteItem(item: DbItem) {
        items.value = items.value.filterNot { it.name == item.name }
    }

    override suspend fun deleteAllItems() {
        items.value = emptyList()
    }
}
