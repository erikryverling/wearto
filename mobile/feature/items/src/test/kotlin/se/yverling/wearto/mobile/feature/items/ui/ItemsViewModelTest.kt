package se.yverling.wearto.mobile.feature.items.ui

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MainDispatcherExtension::class)
private class ItemsViewModelTest {
    val item = Item(name = "Item")

    @Test
    fun `projectState should emit Success`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<UiState.Success>()
            successItem.items shouldBe listOf(item)
        }
    }

    @Test
    fun `projectState should emit LoggedOut on no token`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = false)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            awaitItem().shouldBeInstanceOf<UiState.LoggedOut>()
        }
    }

    @Test
    fun `setItem should call ItemsRepository`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository()
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.setItem(item)

        itemsRepository.lastSavedItem shouldBe item
    }

    @Test
    fun `deleteItem should call ItemsRepository`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.deleteItem(item)

        itemsRepository.lastDeletedItem shouldBe item
    }
}

private class FakeTokenRepository(
    var hasToken: Boolean = true,
) : TokenRepository {
    override fun getToken(): Flow<String?> = flowOf(if (hasToken) "token" else null)
    override suspend fun setToken(token: String) { hasToken = true }
    override suspend fun clearToken() { hasToken = false }
    override fun hasToken(): Flow<Boolean> = flowOf(hasToken)
}

private class FakeItemsRepository(
    initialItems: List<Item> = emptyList(),
) : ItemsRepository {
    private val itemsFlow = MutableStateFlow(initialItems)
    var lastSavedItem: Item? = null
        private set
    var lastDeletedItem: Item? = null
        private set

    override fun getItems(): Flow<List<Item>> = itemsFlow

    override suspend fun setItem(item: Item) {
        lastSavedItem = item
        itemsFlow.value = itemsFlow.value + item
    }

    override suspend fun setItems(items: List<Item>) {
        itemsFlow.value = items
    }

    override suspend fun deleteItem(item: Item) {
        lastDeletedItem = item
        itemsFlow.value = itemsFlow.value - item
    }

    override suspend fun clearItems() {
        itemsFlow.value = emptyList()
    }
}
