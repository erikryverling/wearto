package se.yverling.wearto.mobile.feature.items.ui

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
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
class ItemsViewModelTest {
    @RelaxedMockK
    internal lateinit var tokenRepositoryMock: TokenRepository

    @RelaxedMockK
    internal lateinit var itemsRepositoryMock: ItemsRepository

    private lateinit var itemsViewModel: ItemsViewModel

    @Test
    fun `projectState should emit Success`() = runTest {
        every { itemsRepositoryMock.getItems() } returns flowOf(listOf(item))
        every { tokenRepositoryMock.hasToken() } returns flowOf(true)

        itemsViewModel = createViewModel()

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<UiState.Success>()
            successItem.items.shouldBe(listOf(item))
        }
    }

    @Test
    fun `projectState should emit LoggedOut on no token`() = runTest {
        every { itemsRepositoryMock.getItems() } returns flowOf(listOf(item))
        every { tokenRepositoryMock.hasToken() } returns flowOf(false)

        itemsViewModel = createViewModel()

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            awaitItem().shouldBeInstanceOf<UiState.LoggedOut>()
        }
    }

    @Test
    fun `setItem should call ItemsRepository`() = runTest {
        itemsViewModel = createViewModel()

        itemsViewModel.setItem(item)

        coVerify { itemsRepositoryMock.setItem(item) }
    }

    @Test
    fun `deleteItem should call ItemsRepository`() = runTest {
        itemsViewModel = createViewModel()

        itemsViewModel.deleteItem(item)

        coVerify { itemsRepositoryMock.deleteItem(item) }
    }

    private fun createViewModel() = ItemsViewModel(
        tokenRepositoryMock,
        itemsRepositoryMock
    )

    companion object {
        val item = Item(name = "Item")
    }
}
