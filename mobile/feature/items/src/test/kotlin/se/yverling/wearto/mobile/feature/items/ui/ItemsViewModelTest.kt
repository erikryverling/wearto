package se.yverling.wearto.mobile.feature.items.ui

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.token.UnrecoverableTokenException
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState
import se.yverling.wearto.test.MainDispatcherExtension
import java.io.IOException

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
    fun `projectState should emit CredentialRecovery on UnrecoverableTokenException`() = runTest {
        val tokenRepository = FakeTokenRepository(
            hasToken = false,
            errorToThrow = UnrecoverableTokenException("Corrupted credential"),
        )
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem().shouldBeInstanceOf<UiState.CredentialRecovery>()
        }
    }

    @Test
    fun `projectState should emit TransientError on IOException and allow retry`() = runTest {
        val tokenRepository = FakeTokenRepository(
            hasToken = true,
            errorToThrow = IOException("Storage unavailable"),
        )
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem().shouldBeInstanceOf<UiState.TransientError>()

            // Resolve transient error and retry
            tokenRepository.errorToThrow = null
            itemsViewModel.retry()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<UiState.Success>()
            successItem.items shouldBe listOf(item)
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
    var errorToThrow: Throwable? = null,
) : TokenRepository {
    override fun getToken(): Flow<String?> = flow {
        errorToThrow?.let { throw it }
        emit(if (hasToken) "token" else null)
    }

    override suspend fun setToken(token: String) {
        hasToken = true
    }

    override suspend fun clearToken() {
        hasToken = false
    }

    override fun hasToken(): Flow<Boolean> = flow {
        errorToThrow?.let { throw it }
        emit(hasToken)
    }
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
        itemsFlow.value = itemsFlow.value.filterNot { it == item }
    }

    override suspend fun clearItems() {
        itemsFlow.value = emptyList()
    }
}
