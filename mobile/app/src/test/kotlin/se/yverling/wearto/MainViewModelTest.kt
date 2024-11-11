package se.yverling.wearto

import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.app.ui.MainViewModel
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
class MainViewModelTest {
    @RelaxedMockK
    internal lateinit var itemsRepositoryMock: ItemsRepository

    @Test
    fun `projectState should emit Success`() = runTest {
        val mainViewModel = MainViewModel(itemsRepositoryMock)

        mainViewModel.getItems()

        verify { itemsRepositoryMock.getItems() }
    }
}
