package se.yverlingwearto.mobile.data.items

import android.content.Context
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.items.ItemsRepositoryImpl
import se.yverling.wearto.mobile.data.items.db.AppDatabase
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.test.MainDispatcherExtension


@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
class ItemsRepositoryImplTest {
    @MockK
    internal lateinit var contextMock: Context

    @RelaxedMockK
    internal lateinit var dbMock: AppDatabase

    private lateinit var itemsRepositoryImpl: ItemsRepositoryImpl

    @BeforeEach
    fun setUp() {
        itemsRepositoryImpl = ItemsRepositoryImpl(
            context = contextMock,
            db = dbMock
        )
    }

    @Test
    fun `getItems() should call dao correctly`() = runTest {
        itemsRepositoryImpl.getItems()
        verify { dbMock.itemsDao().getItems() }
    }

    @Test
    fun `setItem() should call dao correctly`() = runTest {
        itemsRepositoryImpl.setItem(item)

        coVerify { dbMock.itemsDao().upsertItem(any<se.yverling.wearto.mobile.data.items.db.Item>()) }
    }

    @Test
    fun `deleteItem() should call dao correctly`() = runTest {
        itemsRepositoryImpl.deleteItem(item)

        coVerify { dbMock.itemsDao().deleteItem(any<se.yverling.wearto.mobile.data.items.db.Item>()) }
    }

    @Test
    fun `clearItems() should call dao correctly`() = runTest {
        itemsRepositoryImpl.clearItems()

        coVerify { dbMock.itemsDao().deleteAllItems() }
    }

    companion object {
        val item = Item(name = "name")

    }
}