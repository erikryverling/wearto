package se.yverling.wearto.mobile.app

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.app.data.DataLayerRepository
import se.yverling.wearto.mobile.app.ui.MainViewModel
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

    @Test
    fun `sendItems should call ItemsRepository and DataLayerRepository`() = runTest {
        every { itemsRepositoryMock.getItems() } returns flowOf(listOf(Item(name = "name")))

        val mainViewModel = MainViewModel(itemsRepositoryMock, dataLayerRepositoryMock)

        mainViewModel.sendItems()

        verify { itemsRepositoryMock.getItems() }
        verify { dataLayerRepositoryMock.sendItems(any()) }
    }
}
