package se.yverling.wearto.wear.feature.items.ui

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.test.MainDispatcherExtension
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.UiState

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class ItemsViewModelTest {
    @RelaxedMockK
    lateinit var itemsRepository: ItemsRepository

    lateinit var viewModel: ItemsViewModel

    val item = Item(name = "Item")

    @Test
    fun `uiState should emit Success`() = runTest {
        every { itemsRepository.getItems() } returns flowOf(listOf(item))

        viewModel = ItemsViewModel(itemsRepository)

        viewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<UiState.Success>()
            successItem.items.shouldBe(listOf((item)))
        }
    }

    @Test
    fun `setItemStateToLoading should call repository`() = runTest {
        viewModel = ItemsViewModel(itemsRepository)

        viewModel.setItemStateToLoading(item)

        coVerify { itemsRepository.updateItemState(any(), any()) }
    }
}
