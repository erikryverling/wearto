package se.yverling.wearto.items

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test
import assertk.assert
import assertk.assertions.isEqualTo

import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.db.ItemDao
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.items.Events.*
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.whenever

class ItemsViewModelTest {
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    var rxRule = RxRule()

    @Mock
    lateinit var applicationMock: Application
    @Mock
    lateinit var appDatabaseMock: AppDatabase
    @Mock
    lateinit var dataLayerClientMock: DataLayerClient
    @Mock
    lateinit var itemsRecyclerViewAdapterMock: ItemsRecyclerViewAdapter

    lateinit var viewModel: ItemsViewModel
    
    private val testItem = Item("uuid", "Item1", "Project1", 0)

    @Before
    fun setup() {
        val items = listOf(testItem)
        val itemDaoMock = mock(ItemDao::class.java)
        whenever(appDatabaseMock.itemDao()).thenReturn(itemDaoMock)
        whenever(itemDaoMock.findAll()).thenReturn(Flowable.just(items))

        viewModel = ItemsViewModel(applicationMock, appDatabaseMock, dataLayerClientMock, itemsRecyclerViewAdapterMock)
    }

    @Test
    fun `Should send selected item successfully`() {
        whenever(dataLayerClientMock.sendSelectedItem(testItem)).thenReturn(Single.just(testItem))

        viewModel.sendItem(testItem)

        assert(viewModel.selectedItem.value).isEqualTo(testItem)
    }

    @Test
    fun `Should show ErrorMessage when sending selected item failed`() {
        whenever(dataLayerClientMock.sendSelectedItem(testItem)).thenReturn(Single.error(RuntimeException()))

        viewModel.sendItem(testItem)

        assert(viewModel.events.value).isEqualTo(SHOW_ITEM_SELECTION_FAILED_EVENT)
    }
}