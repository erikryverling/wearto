package se.yverling.wearto.items

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.items.ItemsViewModel.Events.*
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.sync.network.NetworkClient
import se.yverling.wearto.sync.network.dtos.SyncResponse
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.whenever
import java.net.UnknownHostException

class ItemsViewModelTest {
    @get:Rule
    var mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    var rxRule: RxRule = RxRule()

    @Mock
    lateinit var applicationMock: Application
    @Mock
    lateinit var databaseClientMock: DatabaseClient
    @Mock
    lateinit var networkClientMock: NetworkClient
    @Mock
    lateinit var dataLayerClientMock: DataLayerClient
    @Mock
    lateinit var recyclerViewAdapter: ItemsRecyclerViewAdapter

    lateinit var viewModel: ItemsViewModel

    private val testProjects = listOf(se.yverling.wearto.sync.network.dtos.Project(1, "Project1", 0))

    @Before
    fun setup() {
        whenever(databaseClientMock.findAllItemsWithProjectContinuously()).thenReturn(Flowable.just(emptyList()))

        viewModel = ItemsViewModel(applicationMock, databaseClientMock, networkClientMock, dataLayerClientMock, recyclerViewAdapter)
    }

    @Test
    fun `Should potentially show add tap target when items list is empty`() {
        assert(viewModel.events.value).isEqualTo(SHOW_ADD_TAP_TARGET_EVENT)
    }


    @Test
    fun `Should potentially show sync tap target when items list is empty`() {
        whenever(databaseClientMock.findAllItemsWithProjectContinuously()).thenReturn(Flowable.just(listOf(ItemWithProject())))

        viewModel = ItemsViewModel(applicationMock, databaseClientMock, networkClientMock, dataLayerClientMock, recyclerViewAdapter)

        assert(viewModel.events.value).isEqualTo(SHOW_SYNC_TAP_TARGET_EVENT)
    }


    @Test
    fun `Should sync successfully`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjects)))
        whenever(databaseClientMock.replaceAllProjects(testProjects)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(databaseClientMock.updateItem(anyString(), anyString(), anyString())).thenReturn(Completable.complete())
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.complete())

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assert(viewModel.events.value).isEqualTo(SYNC_SUCCEEDED_SNACKBAR_EVENT)
    }

    @Test
    fun `Should show error message when network error occurs`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.error(UnknownHostException()))
        whenever(databaseClientMock.replaceAllProjects(testProjects)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(databaseClientMock.updateItem(anyString(), anyString(), anyString())).thenReturn(Completable.complete())
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.complete())

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assert(viewModel.events.value).isEqualTo(SYNC_FAILED_DUE_TO_NETWORK_DIALOG_EVENT)
    }

    @Test
    fun `Should show error message when DataLayerError occurs`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjects)))
        whenever(databaseClientMock.replaceAllProjects(testProjects)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(databaseClientMock.updateItem(anyString(), anyString(), anyString())).thenReturn(Completable.complete())
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.error(DataLayerClient.DataLayerException("")))

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assert(viewModel.events.value).isEqualTo(SYNC_FAILED_DUE_TO_DATA_LAYER_DIALOG_EVENT)
    }

    @Test
    fun `Should show error message when GeneralError occurs`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjects)))
        whenever(databaseClientMock.replaceAllProjects(testProjects)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(databaseClientMock.updateItem(anyString(), anyString(), anyString())).thenReturn(Completable.error(RuntimeException()))
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.error(RuntimeException()))

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assert(viewModel.events.value).isEqualTo(SYNC_FAILED_DUE_TO_GENERAL_ERROR_EVENT)
    }
}