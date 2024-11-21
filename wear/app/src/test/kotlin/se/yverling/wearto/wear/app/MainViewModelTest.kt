package se.yverling.wearto.wear.app

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.test.MainDispatcherExtension
import se.yverling.wearto.wear.app.data.DataLayerRepository
import se.yverling.wearto.wear.app.ui.MainViewModel
import se.yverling.wearto.wear.data.items.model.Item

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class MainViewModelTest {
    @RelaxedMockK
    lateinit var dataLayerRepositoryMock: DataLayerRepository

    @Test
    fun `sendItem should call DataLayerRepository`() = runTest {
        val mainViewModel = MainViewModel(dataLayerRepositoryMock)

        val item = Item(name = "Name")

        mainViewModel.sendItem(item)

        verify { dataLayerRepositoryMock.sendItem(item) }
    }
}
