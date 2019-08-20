package se.yverling.wearto.chars

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.db.ItemDao
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.whenever

class CharsViewModelTest {
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
    lateinit var charsRecyclerViewAdapterMock: CharsRecyclerViewAdapter

    lateinit var viewModel: CharsViewModel

    @Test
    fun `Should show list of chars successfully`() {
        initViewModelWithValidCharsList()

        argumentCaptor<List<Char>>().apply {
            verify(charsRecyclerViewAdapterMock).setChars(capture())
            assert(firstValue).isEqualTo(listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'))
        }
    }

    @Test
    fun `Should skip showing list of chars if the number of items are too few`() {
        initViewModelWithTooFewItems()

        assert(viewModel.events.value is Event.StartItemsActivity)
        val result: Event.StartItemsActivity = viewModel.events.value as Event.StartItemsActivity

        assert(result.char).isNull()
    }

    @Test
    fun `Should skip showing list of chars if the resulting list of chars are too small`() {
        initViewModelWithTooFewResultingChars()

        assert(viewModel.events.value is Event.StartItemsActivity)
        val result: Event.StartItemsActivity = viewModel.events.value as Event.StartItemsActivity
        assert(result.char).isNull()
    }

    @Test
    fun `Should show items list based on char successfully`() {
        val selectedChar = 'A'

        initViewModelWithValidCharsList(selectedChar)

        viewModel.showItemsList(selectedChar)

        assert(viewModel.events.value is Event.StartItemsActivity)
        val result: Event.StartItemsActivity = viewModel.events.value as Event.StartItemsActivity
        assert(result.char).isEqualTo(selectedChar)
    }

    private fun initViewModelWithValidCharsList(firstItemName: Char = 'A') {
        createViewModelWithItemsList(
                listOf(
                        Item("uuid1", firstItemName.toString(), "Project1", 0),
                        Item("uuid2", "B", "Project1", 0),
                        Item("uuid3", "C", "Project1", 0),
                        Item("uuid4", "D", "Project1", 0),
                        Item("uuid5", "E", "Project1", 0),
                        Item("uuid6", "F", "Project1", 0),
                        Item("uuid7", "G", "Project1", 0),
                        Item("uuid8", "H", "Project1", 0),
                        Item("uuid9", "I", "Project1", 0),
                        Item("uuid10", "J", "Project1", 0)
                )
        )
    }

    private fun initViewModelWithTooFewItems() {
        createViewModelWithItemsList(
                listOf(
                        Item("uuid1", "A", "Project1", 0),
                        Item("uuid2", "B", "Project1", 0),
                        Item("uuid3", "C", "Project1", 0)
                )
        )
    }

    private fun initViewModelWithTooFewResultingChars() {
        createViewModelWithItemsList(
                listOf(
                        Item("uuid1", "A", "Project1", 0),
                        Item("uuid2", "A", "Project1", 0),
                        Item("uuid3", "A", "Project1", 0),
                        Item("uuid4", "A", "Project1", 0),
                        Item("uuid5", "A", "Project1", 0),
                        Item("uuid6", "A", "Project1", 0),
                        Item("uuid7", "B", "Project1", 0),
                        Item("uuid8", "B", "Project1", 0),
                        Item("uuid9", "B", "Project1", 0),
                        Item("uuid10", "B", "Project1", 0)
                )
        )
    }

    private fun createViewModelWithItemsList(items: List<Item>) {
        val itemDaoMock = mock(ItemDao::class.java)
        whenever(appDatabaseMock.itemDao()).thenReturn(itemDaoMock)
        whenever(itemDaoMock.findAll()).thenReturn(Flowable.just(items))

        viewModel = CharsViewModel(applicationMock, appDatabaseMock, charsRecyclerViewAdapterMock)
    }
}