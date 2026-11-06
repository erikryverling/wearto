package se.yverling.wearto.mobile.feature.items.ui

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
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
@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun `deleteItem should remove item and update uiState`() = runTest {
        val item1 = Item(uid = 1, name = "Item 1")
        val item2 = Item(uid = 2, name = "Item 2")
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item1, item2))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem() shouldBe UiState.Success(items = listOf(item1, item2))

            itemsViewModel.deleteItem(item1)

            awaitItem() shouldBe UiState.Success(items = listOf(item2))
            itemsRepository.lastDeletedItem shouldBe item1
        }
    }

    @Test
    fun `deleteItem on last remaining item should emit Success with empty list`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem() shouldBe UiState.Success(items = listOf(item))

            itemsViewModel.deleteItem(item)

            awaitItem() shouldBe UiState.Success(items = emptyList())
            itemsRepository.lastDeletedItem shouldBe item
        }
    }

    @Test
    fun `dismissing item should delete it`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.dismissItem(item)
        runCurrent()

        itemsRepository.lastDeletedItem shouldBe item
        itemsRepository.getItems().test {
            awaitItem() shouldBe emptyList()
        }
    }

    @Test
    fun `dismissing item should update ui state`() = runTest {
        val item1 = Item(uid = 1, name = "Item 1")
        val item2 = Item(uid = 2, name = "Item 2")
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item1, item2))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem() shouldBe UiState.Success(items = listOf(item1, item2))

            itemsViewModel.dismissItem(item1)
            awaitItem() shouldBe UiState.Success(items = listOf(item2))
        }
    }

    @Test
    fun `dismissing last item should show empty list`() = runTest {
        val tokenRepository = FakeTokenRepository(hasToken = true)
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val itemsViewModel = ItemsViewModel(tokenRepository, itemsRepository)

        itemsViewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem() shouldBe UiState.Success(items = listOf(item))

            itemsViewModel.dismissItem(item)
            awaitItem() shouldBe UiState.Success(items = emptyList())
        }
    }

    @Test
    fun `rapid dismissals should delete both items`() = runTest {
        val first = Item(uid = 1, name = "First")
        val latest = Item(uid = 2, name = "Latest")
        val itemsRepository = FakeItemsRepository(initialItems = listOf(first, latest))
        val viewModel = ItemsViewModel(FakeTokenRepository(), itemsRepository)

        viewModel.dismissItem(first)
        viewModel.dismissItem(latest)
        runCurrent()

        itemsRepository.getItems().test {
            awaitItem() shouldBe emptyList()
        }
    }

    @Test
    fun `dismissing item should publish deletion notice`() = runTest {
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val viewModel = ItemsViewModel(FakeTokenRepository(), itemsRepository)

        viewModel.dismissItem(item)
        runCurrent()

        viewModel.deletionNotice.value?.itemName shouldBe item.name
    }

    @Test
    fun `dismissing deletion notice should clear it and leave item deleted`() = runTest {
        val itemsRepository = FakeItemsRepository(initialItems = listOf(item))
        val viewModel = ItemsViewModel(FakeTokenRepository(), itemsRepository)
        viewModel.dismissItem(item)
        runCurrent()
        val notice = requireNotNull(viewModel.deletionNotice.value)

        viewModel.dismissDeletionNotice(notice)

        viewModel.deletionNotice.value shouldBe null
        itemsRepository.lastSavedItem shouldBe null
        itemsRepository.getItems().test {
            awaitItem() shouldBe emptyList()
        }
    }

    @Test
    fun `successive dismissals should retain latest deletion notice`() = runTest {
        val first = Item(uid = 1, name = "First")
        val latest = Item(uid = 2, name = "Latest")
        val itemsRepository = FakeItemsRepository(initialItems = listOf(first, latest))
        val viewModel = ItemsViewModel(FakeTokenRepository(), itemsRepository)
        viewModel.dismissItem(first)
        runCurrent()
        val oldNotice = requireNotNull(viewModel.deletionNotice.value)
        viewModel.dismissItem(latest)
        runCurrent()
        val latestNotice = requireNotNull(viewModel.deletionNotice.value)

        viewModel.dismissDeletionNotice(oldNotice)

        viewModel.deletionNotice.value shouldBe latestNotice
        latestNotice.itemName shouldBe latest.name

        itemsRepository.getItems().test {
            awaitItem() shouldBe emptyList()
        }
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
