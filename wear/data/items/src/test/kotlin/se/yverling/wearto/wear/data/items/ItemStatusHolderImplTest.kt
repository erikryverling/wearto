package se.yverling.wearto.wear.data.items

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import se.yverling.wearto.wear.data.items.ItemStatusHolderImpl.Companion.CONFIRMATION_DELAY_MS
import se.yverling.wearto.wear.data.items.ItemStatusHolderImpl.Companion.LOADING_TIMEOUT_MS
import se.yverling.wearto.wear.data.items.model.ItemState

@OptIn(ExperimentalCoroutinesApi::class)
private class ItemStatusHolderImplTest {

    @Test
    fun `setLoading should emit Loading state immediately`() = runTest {
        val statusHolder = ItemStatusHolderImpl(scope = backgroundScope)

        statusHolder.setLoading("Milk")

        statusHolder.statuses.value["Milk"] shouldBe ItemState.Loading
    }

    @Test
    fun `setLoading should transition to Error after timeout`() = runTest {
        val testScope = TestScope()
        val statusHolder = ItemStatusHolderImpl(scope = testScope.backgroundScope)

        statusHolder.setLoading("Milk")
        statusHolder.statuses.value["Milk"] shouldBe ItemState.Loading

        testScope.advanceTimeBy(LOADING_TIMEOUT_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Milk"] shouldBe ItemState.Error

        testScope.advanceTimeBy(CONFIRMATION_DELAY_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Milk"] shouldBe null
    }

    @Test
    fun `setConfirmed with success should emit Successful state then reset after delay`() = runTest {
        val testScope = TestScope()
        val statusHolder = ItemStatusHolderImpl(scope = testScope.backgroundScope)

        statusHolder.setLoading("Milk")
        statusHolder.setConfirmed("Milk", isSuccess = true)

        statusHolder.statuses.value["Milk"] shouldBe ItemState.Successful

        testScope.advanceTimeBy(CONFIRMATION_DELAY_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Milk"] shouldBe null
    }

    @Test
    fun `setConfirmed with error should emit Error state then reset after delay`() = runTest {
        val testScope = TestScope()
        val statusHolder = ItemStatusHolderImpl(scope = testScope.backgroundScope)

        statusHolder.setConfirmed("Bread", isSuccess = false)

        statusHolder.statuses.value["Bread"] shouldBe ItemState.Error

        testScope.advanceTimeBy(CONFIRMATION_DELAY_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Bread"] shouldBe null
    }

    @Test
    fun `setConfirmed should cancel pending loading timeout`() = runTest {
        val testScope = TestScope()
        val statusHolder = ItemStatusHolderImpl(scope = testScope.backgroundScope)

        statusHolder.setLoading("Milk")
        testScope.advanceTimeBy(5_000L)
        testScope.runCurrent()

        statusHolder.setConfirmed("Milk", isSuccess = true)
        statusHolder.statuses.value["Milk"] shouldBe ItemState.Successful

        testScope.advanceTimeBy(CONFIRMATION_DELAY_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Milk"] shouldBe null

        // Advance past original 10s timeout to verify it does not re-fire
        testScope.advanceTimeBy(LOADING_TIMEOUT_MS)
        testScope.runCurrent()
        statusHolder.statuses.value["Milk"] shouldBe null
    }

    @Test
    fun `reset should remove item immediately`() = runTest {
        val statusHolder = ItemStatusHolderImpl(scope = backgroundScope)

        statusHolder.setLoading("Milk")
        statusHolder.reset("Milk")

        statusHolder.statuses.value["Milk"] shouldBe null
    }

    @Test
    fun `resetAll should clear all statuses immediately`() = runTest {
        val statusHolder = ItemStatusHolderImpl(scope = backgroundScope)

        statusHolder.setLoading("Milk")
        statusHolder.setLoading("Bread")
        statusHolder.resetAll()

        statusHolder.statuses.value.shouldBeEmpty()
    }
}
