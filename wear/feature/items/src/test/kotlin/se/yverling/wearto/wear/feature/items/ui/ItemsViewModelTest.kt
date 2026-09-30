package se.yverling.wearto.wear.feature.items.ui

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.test.MainDispatcherExtension
import se.yverling.wearto.wear.data.items.ItemStatusHolder
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState
import se.yverling.wearto.wear.data.items.sync.ItemSyncClient
import se.yverling.wearto.wear.feature.items.model.ItemUiModel
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.UiState

@ExtendWith(MainDispatcherExtension::class)
private class ItemsViewModelTest {
    val item = Item(name = "Item")

    @Test
    fun `uiState should emit Success combining items with default Init state`() = runTest {
        val itemsRepository = FakeItemsRepository(itemsFlow = flowOf(listOf(item)))
        val statusHolder = FakeItemStatusHolder()
        val itemsSyncClient = FakeItemSyncClient()

        val viewModel = ItemsViewModel(itemsRepository, statusHolder, itemsSyncClient)

        viewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<UiState.Success>()
            successItem.items shouldBe listOf(ItemUiModel(item = item, state = ItemState.Init))
        }
    }

    @Test
    fun `uiState should reflect updated status from ItemStatusHolder`() = runTest {
        val itemsRepository = FakeItemsRepository(itemsFlow = flowOf(listOf(item)))
        val statusHolder = FakeItemStatusHolder()
        val itemsSyncClient = FakeItemSyncClient()

        val viewModel = ItemsViewModel(itemsRepository, statusHolder, itemsSyncClient)

        viewModel.uiState.test {
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            awaitItem().shouldBeInstanceOf<UiState.Success>()

            statusHolder.setConfirmed("Item", isSuccess = true)

            val updatedState = awaitItem()
            updatedState.shouldBeInstanceOf<UiState.Success>()
            updatedState.items shouldBe listOf(ItemUiModel(item = item, state = ItemState.Successful))
        }
    }

    @Test
    fun `sendItem should set status to Loading and dispatch task`() = runTest {
        val itemsRepository = FakeItemsRepository(itemsFlow = flowOf(listOf(item)))
        val statusHolder = FakeItemStatusHolder()
        val itemsSyncClient = FakeItemSyncClient()

        val viewModel = ItemsViewModel(itemsRepository, statusHolder, itemsSyncClient)
        viewModel.sendItem(item)
        testScheduler.runCurrent()

        statusHolder.lastLoadedItemName shouldBe item.name
        itemsSyncClient.lastSentTaskName shouldBe item.name
        statusHolder.statuses.value[item.name] shouldBe ItemState.Loading
    }

    @Test
    fun `sendItem should transition status to Error immediately if dispatch fails`() = runTest {
        val itemsRepository = FakeItemsRepository(itemsFlow = flowOf(listOf(item)))
        val statusHolder = FakeItemStatusHolder()
        val itemsSyncClient = FakeItemSyncClient(result = Result.failure(RuntimeException("Network failure")))

        val viewModel = ItemsViewModel(itemsRepository, statusHolder, itemsSyncClient)
        viewModel.sendItem(item)
        testScheduler.runCurrent()

        statusHolder.statuses.value[item.name] shouldBe ItemState.Error
    }
}

private class FakeItemsRepository(
    private val itemsFlow: Flow<List<Item>>,
) : ItemsRepository {
    override fun getItems(): Flow<List<Item>> = itemsFlow
    override suspend fun replaceItems(items: List<Item>) {}
}

private class FakeItemStatusHolder : ItemStatusHolder {
    private val mutableStatuses = MutableStateFlow<Map<String, ItemState>>(emptyMap())
    override val statuses: StateFlow<Map<String, ItemState>> = mutableStatuses

    var lastLoadedItemName: String? = null
        private set

    override fun setLoading(name: String) {
        lastLoadedItemName = name
        mutableStatuses.value = mutableStatuses.value + (name to ItemState.Loading)
    }

    override fun setConfirmed(name: String, isSuccess: Boolean) {
        val state = if (isSuccess) ItemState.Successful else ItemState.Error
        mutableStatuses.value = mutableStatuses.value + (name to state)
    }

    override fun reset(name: String) {
        mutableStatuses.value = mutableStatuses.value - name
    }

    override fun resetAll() {
        mutableStatuses.value = emptyMap()
    }
}

private class FakeItemSyncClient(
    var result: Result<Unit> = Result.success(Unit),
) : ItemSyncClient {
    var lastSentTaskName: String? = null
        private set

    override suspend fun sendItem(name: String): Result<Unit> {
        lastSentTaskName = name
        return result
    }
}
