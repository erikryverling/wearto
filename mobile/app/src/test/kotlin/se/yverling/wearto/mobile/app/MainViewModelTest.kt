package se.yverling.wearto.mobile.app

import app.cash.turbine.test
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.app.data.DataLayerRepository
import se.yverling.wearto.mobile.app.ui.MainViewModel
import se.yverling.wearto.mobile.app.ui.MainViewModel.UiState
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class MainViewModelTest {
    @RelaxedMockK
    lateinit var itemsRepositoryMock: ItemsRepository

    @RelaxedMockK
    lateinit var dataLayerRepositoryMock: DataLayerRepository

    val csv = "name1,name2,name3"

    val items = listOf(
        Item(name = "name1"),
        Item(name = "name2"),
        Item(name = "name3"),
    )

    lateinit var mainViewModel: MainViewModel

    @BeforeEach
    fun setup() {
        every { itemsRepositoryMock.getItems() } returns flowOf(items)

        mainViewModel = MainViewModel(
            itemsRepository = itemsRepositoryMock,
            dataLayerRepository = dataLayerRepositoryMock,
        )
    }

    @Test
    fun `sendItems should call ItemsRepository and DataLayerRepository`() = runTest {
        mainViewModel.sendItems()

        verify { itemsRepositoryMock.getItems() }
        verify { dataLayerRepositoryMock.sendItems(any()) }
    }

    @Test
    fun `showMessage should emit Message and hideMessage should emit Default`() = runTest {
        mainViewModel.uiState.test {
            awaitItem().shouldBeEqual(UiState.Default)

            mainViewModel.showMessage(1)

            awaitItem().shouldBeEqual(UiState.Message(1))

            mainViewModel.hideMessage()

            awaitItem().shouldBeEqual(UiState.Default)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getItemsAsCsv should return names in CSV`() {
        runTest {
            val first = mainViewModel.getItemsAsCsv().first()
            first.shouldBeEqual(csv)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `replaceWithItemsFromCsv should clear and set items`() = runTest {
        mainViewModel.replaceWithItemsFromCsv(csv)
        advanceUntilIdle()

        coVerify { itemsRepositoryMock.clearItems() }

        val listSlot = slot<List<Item>>()
        coVerify { itemsRepositoryMock.setItems(capture(listSlot)) }
        listSlot.captured.shouldBeEqual(items)
    }
}
