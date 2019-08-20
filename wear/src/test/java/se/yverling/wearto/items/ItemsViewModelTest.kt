package se.yverling.wearto.items

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assert
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.db.ItemDao
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.items.Event.ShowConfirmationAndFinish
import se.yverling.wearto.items.Event.ShowItemSelectionFailed
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
        val itemDaoMock = mock<ItemDao>()
        whenever(appDatabaseMock.itemDao()).thenReturn(itemDaoMock)
        whenever(itemDaoMock.findAll()).thenReturn(Flowable.just(items))
    }

    @Test
    fun `Should show filtered list when char is present successfully`() {
        setViewModel('A')

        argumentCaptor<List<Item>>().apply {
            verify(itemsRecyclerViewAdapterMock).setItems(capture())
            assert(firstValue).isEmpty()
        }
    }

    @Test
    fun `Should show unfiltered list when char is not present successfully`() {
        setViewModel()

        argumentCaptor<List<Item>>().apply {
            verify(itemsRecyclerViewAdapterMock).setItems(capture())
            assert(firstValue).isNotEmpty()
        }
    }

    @Test
    fun `Should send selected item successfully`() {
        setViewModel()

        whenever(dataLayerClientMock.sendSelectedItem(testItem)).thenReturn(Single.just(testItem))

        viewModel.sendItem(testItem)

        assert(viewModel.events.value is ShowConfirmationAndFinish)

        val result: ShowConfirmationAndFinish = viewModel.events.value as ShowConfirmationAndFinish
        assert(result.item).isEqualTo(testItem)
    }

    @Test
    fun `Should show ErrorMessage when sending selected item failed`() {
        setViewModel()

        whenever(dataLayerClientMock.sendSelectedItem(testItem)).thenReturn(Single.error(RuntimeException()))

        viewModel.sendItem(testItem)

        assert(viewModel.events.value is ShowItemSelectionFailed)
    }

    private fun setViewModel(char: Char? = null) {
        viewModel = ItemsViewModel(applicationMock, appDatabaseMock, dataLayerClientMock, itemsRecyclerViewAdapterMock, char)
    }
}