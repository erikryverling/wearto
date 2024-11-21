package se.yverling.wearto.wear.data.items

import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.test.MainDispatcherExtension
import se.yverling.wearto.wear.data.items.db.AppDatabase
import se.yverling.wearto.wear.data.items.model.Item

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class ItemsRepositoryImplTest {
    @RelaxedMockK
    lateinit var dbMock: AppDatabase

    lateinit var repository: ItemsRepositoryImpl

    val item = Item(name = "name")

    @BeforeEach
    fun setUp() {
        repository = ItemsRepositoryImpl(db = dbMock)
    }

    @Test
    fun `getItems() should call dao correctly`() = runTest {
        repository.getItems()

        verify { dbMock.itemsDao().getItems() }
    }

    @Test
    fun `replaceItems() should call dao correctly`() = runTest {
        repository.replaceItems(listOf(item))

        coVerify { dbMock.itemsDao().deleteAllItems() }
        coVerify { dbMock.itemsDao().setItems(any()) }
    }
}
