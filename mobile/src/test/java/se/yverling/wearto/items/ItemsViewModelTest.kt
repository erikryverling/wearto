package se.yverling.wearto.items

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.SharedPreferences
import assertk.assert
import assertk.assertions.isFalse
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.items.ItemsViewModel.Event.ShowAddTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.ShowImportFailedDialog
import se.yverling.wearto.items.ItemsViewModel.Event.ShowSyncTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToDataLayerDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToGeneralError
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToNetworkDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncSucceededSnackbar
import se.yverling.wearto.items.edit.LATEST_SELECTED_PROJECT_PREFERENCES_KEY
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.sync.network.NetworkClient
import se.yverling.wearto.sync.network.dtos.ProjectDataResponse
import se.yverling.wearto.sync.network.dtos.SyncResponse
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.whenever
import java.net.UnknownHostException
import java.util.*

typealias ItemDto = se.yverling.wearto.sync.network.dtos.Item

private const val PROJECT_NAME = "Project1"
private const val ITEM_NAME = "Item1"

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
    lateinit var recyclerViewAdapterMock: ItemsRecyclerViewAdapter
    @Mock
    lateinit var sharedPreferencesMock: SharedPreferences
    @Mock
    lateinit var analyticsMock: FirebaseAnalytics

    lateinit var viewModel: ItemsViewModel

    private val testProjectDtos = listOf(se.yverling.wearto.sync.network.dtos.Project(1, PROJECT_NAME, 0))

    @Before
    fun setup() {
        whenever(databaseClientMock.findAllItemsWithProjectContinuously()).thenReturn(Flowable.just(emptyList()))
        whenever(databaseClientMock.findAllProjects()).thenReturn(Single.just(listOf(Project(0, PROJECT_NAME, 0))))
        whenever(sharedPreferencesMock.getString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, DEFAULT_PROJECT)).thenReturn(PROJECT_NAME)

        viewModel = ItemsViewModel(
                applicationMock,
                databaseClientMock,
                networkClientMock,
                dataLayerClientMock,
                sharedPreferencesMock,
                recyclerViewAdapterMock,
                analyticsMock)
    }

    @Test
    fun `Should potentially show add tap target when items list is empty`() {
        assertThat(viewModel.events.value, instanceOf(ShowAddTapTarget::class.java))
    }

    @Test
    fun `Should potentially show sync tap target when items list is empty`() {
        whenever(databaseClientMock.findAllItemsWithProjectContinuously()).thenReturn(Flowable.just(listOf(ItemWithProject())))

        viewModel = ItemsViewModel(
                applicationMock,
                databaseClientMock,
                networkClientMock,
                dataLayerClientMock,
                sharedPreferencesMock,
                recyclerViewAdapterMock,
                analyticsMock)

        assertThat(viewModel.events.value, instanceOf(ShowSyncTapTarget::class.java))
    }


    @Test
    fun `Should sync successfully`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjectDtos)))
        whenever(databaseClientMock.replaceAllProjects(testProjectDtos)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.complete())

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assertThat(viewModel.events.value, instanceOf(SyncSucceededSnackbar::class.java))
    }

    @Test
    fun `Should show error message when network error occurs while syncing`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.error(UnknownHostException()))
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.complete())

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assertThat(viewModel.events.value, instanceOf(SyncFailedDueToNetworkDialog::class.java))
    }

    @Test
    fun `Should show error message when DataLayerError occurs while syncing`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjectDtos)))
        whenever(databaseClientMock.replaceAllProjects(testProjectDtos)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.error(DataLayerClient.DataLayerException("")))

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assertThat(viewModel.events.value, instanceOf(SyncFailedDueToDataLayerDialog::class.java))
    }

    @Test
    fun `Should show error message when GeneralError occurs while syncing`() {
        whenever(networkClientMock.getProjects()).thenReturn(Single.just(SyncResponse(testProjectDtos)))
        whenever(databaseClientMock.replaceAllProjects(testProjectDtos)).thenReturn(Completable.complete())
        whenever(databaseClientMock.findAllOrphanItems()).thenReturn(Single.just(emptyList()))
        whenever(dataLayerClientMock.syncProjects()).thenReturn(Completable.error(RuntimeException()))

        viewModel.sync()

        assert(viewModel.isSyncing).isFalse()
        assertThat(viewModel.events.value, instanceOf(SyncFailedDueToGeneralError::class.java))
    }

    @Test
    fun `Should import items successfully when completed items and locally removed are not included`() {
        val testProject = Project(0, PROJECT_NAME, 0)
        val testItem = Item(UUID.randomUUID().toString(), ITEM_NAME, testProject.id)
        val testItemDto = ItemDto(0, testProject.id, ITEM_NAME)

        whenever(databaseClientMock.findProjectByName(testProject.name)).thenReturn(Single.just(testProject))
        whenever(networkClientMock.getItems(testProject.id)).thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.findByNameAndProjectId(testItem.name, testProject.id)).thenReturn(Maybe.empty())
        whenever(databaseClientMock.saveItem(testItem.name, testProject.name)).thenReturn(Completable.complete())

        viewModel.importItems()

        assert(viewModel.isImporting.value as Boolean).isFalse()
        assertThat(viewModel.events.value, not(instanceOf(ShowImportFailedDialog::class.java)))
    }

    @Test
    fun `Should import items successfully when completed items are included and locally removed items are not included`() {
        val testProject = Project(0, PROJECT_NAME, 0)
        val testItem = Item(UUID.randomUUID().toString(), ITEM_NAME, testProject.id)
        val testItemDto = ItemDto(0, testProject.id, ITEM_NAME)

        viewModel.includeCompletedItemsInImport.value = true

        whenever(databaseClientMock.findProjectByName(testProject.name)).thenReturn(Single.just(testProject))
        whenever(networkClientMock.getItems(testProject.id)).thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.findByNameAndProjectId(testItem.name, testProject.id)).thenReturn(Maybe.empty())
        whenever(networkClientMock.getCompletedItems(testProject.id, COMPLETED_ITEMS_OFFSET, COMPLETED_ITEMS_LIMIT))
                .thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.saveItem(testItem.name, testProject.name)).thenReturn(Completable.complete())

        viewModel.importItems()

        assert(viewModel.isImporting.value as Boolean).isFalse()
        assertThat(viewModel.events.value, not(instanceOf(ShowImportFailedDialog::class.java)))
    }

    @Test
    fun `Should import items successfully when completed items are not included and locally removed items are included`() {
        val testProject = Project(0, PROJECT_NAME, 0)
        val testItem = Item(UUID.randomUUID().toString(), ITEM_NAME, testProject.id)
        val testItemDto = ItemDto(0, testProject.id, ITEM_NAME)

        viewModel.includeRemovedItemsWhenImporting.value = true

        whenever(databaseClientMock.findProjectByName(testProject.name)).thenReturn(Single.just(testProject))
        whenever(networkClientMock.getItems(testProject.id)).thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.findByNameAndProjectId(testItem.name, testProject.id)).thenReturn(Maybe.empty())
        whenever(databaseClientMock.saveItem(testItem.name, testProject.name)).thenReturn(Completable.complete())

        viewModel.importItems()

        assert(viewModel.isImporting.value as Boolean).isFalse()
        assertThat(viewModel.events.value, not(instanceOf(ShowImportFailedDialog::class.java)))
    }

    @Test
    fun `Should import items successfully when completed items are included and locally removed items are included`() {
        val testProject = Project(0, PROJECT_NAME, 0)
        val testItem = Item(UUID.randomUUID().toString(), ITEM_NAME, testProject.id)
        val testItemDto = ItemDto(0, testProject.id, ITEM_NAME)

        viewModel.includeRemovedItemsWhenImporting.value = true
        viewModel.includeCompletedItemsInImport.value = true

        whenever(databaseClientMock.findProjectByName(testProject.name)).thenReturn(Single.just(testProject))
        whenever(networkClientMock.getItems(testProject.id)).thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.findByNameAndProjectId(testItem.name, testProject.id))
                .thenReturn(Maybe.just(Item(UUID.randomUUID().toString(), ITEM_NAME, testProject.id, true)))
        whenever(networkClientMock.getCompletedItems(testProject.id, COMPLETED_ITEMS_OFFSET, COMPLETED_ITEMS_LIMIT))
                .thenReturn(Single.just(ProjectDataResponse(listOf(testItemDto))))
        whenever(databaseClientMock.saveItem(testItem.name, testProject.name)).thenReturn(Completable.complete())

        viewModel.importItems()

        assert(viewModel.isImporting.value as Boolean).isFalse()
        assertThat(viewModel.events.value, not(instanceOf(ShowImportFailedDialog::class.java)))
    }

    @Test
    fun `Should show error dialog when import items fails`() {
        val testProject = Project(0, PROJECT_NAME, 0)

        whenever(databaseClientMock.findProjectByName(testProject.name)).thenReturn(Single.just(testProject))

        viewModel.importItems()

        assert(viewModel.isImporting.value as Boolean).isFalse()
        assertThat(viewModel.events.value, instanceOf(ShowImportFailedDialog::class.java))
    }
}