package se.yverling.wearto.items.edit

import android.app.Application
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assert
import assertk.assertions.isEqualTo
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.items.ItemsViewModel
import se.yverling.wearto.items.edit.ItemViewModel.Event.FinishActivity
import se.yverling.wearto.items.edit.ItemViewModel.Event.ShowSaveFailedDialog
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.whenever

private const val UUID = "UUID"

private const val ITEM_NAME = "Item1"

private const val FIRST_PROJECT_ID = 1L
private const val FIRST_PROJECT_NAME = "Project1"
private const val FIRST_PROJECT_COLOR = 0

private const val SECOND_PROJECT_ID = 2L
private const val SECOND_PROJECT_NAME = "Project2"
private const val SECOND_PROJECT_COLOR = 1

class ItemViewModelTest {
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
    lateinit var sharedPreferencesMock: SharedPreferences

    @Mock
    lateinit var analyticsMock: FirebaseAnalytics

    lateinit var viewModel: ItemViewModel

    @Before
    fun setup() {
        val testProjects = listOf(
                Project(FIRST_PROJECT_ID, FIRST_PROJECT_NAME, FIRST_PROJECT_COLOR),
                Project(SECOND_PROJECT_ID, SECOND_PROJECT_NAME, SECOND_PROJECT_COLOR)
        )

        whenever(databaseClientMock.findAllProjects()).thenReturn(Single.just(testProjects))

        viewModel = ItemViewModel(applicationMock, databaseClientMock, sharedPreferencesMock, analyticsMock)
    }

    @Test
    fun `Should edit item successfully`() {
        val itemWithProject = ItemWithProject()
        itemWithProject.item = Item(UUID, ITEM_NAME, FIRST_PROJECT_ID)
        itemWithProject.project = Project(FIRST_PROJECT_ID, FIRST_PROJECT_NAME, FIRST_PROJECT_COLOR)

        whenever(databaseClientMock.findItemWithProjectByUuid(UUID)).thenReturn(Single.just(itemWithProject))

        viewModel.edit(UUID)

        assert(viewModel.name.value).isEqualTo(ITEM_NAME)
        assert(viewModel.projectName.value).isEqualTo(FIRST_PROJECT_NAME)
    }

    @Test
    fun `Should save new item successfully`() {
        viewModel.name.value = ITEM_NAME
        viewModel.projectName.value = FIRST_PROJECT_NAME

        whenever(databaseClientMock.saveItem(ITEM_NAME, FIRST_PROJECT_NAME)).thenReturn(Completable.complete())

        viewModel.save()

        verify(databaseClientMock).saveItem(ITEM_NAME, FIRST_PROJECT_NAME)
        assertThat(viewModel.events.value, instanceOf(FinishActivity::class.java))
    }


    @Test
    fun `Should show error message when item could not be saved`() {
        viewModel.name.value = ITEM_NAME
        viewModel.projectName.value = FIRST_PROJECT_NAME

        whenever(databaseClientMock.saveItem(ITEM_NAME, FIRST_PROJECT_NAME)).thenReturn(Completable.error(RuntimeException()))

        viewModel.save()

        verify(databaseClientMock).saveItem(ITEM_NAME, FIRST_PROJECT_NAME)
        assertThat(viewModel.events.value, instanceOf(ShowSaveFailedDialog::class.java))
    }

    @Test
    fun `Should save existing item Successfully`() {
        viewModel.uuid.value = UUID
        viewModel.name.value = ITEM_NAME
        viewModel.projectName.value = FIRST_PROJECT_NAME

        whenever(databaseClientMock
                .updateItem(UUID, ITEM_NAME, FIRST_PROJECT_NAME))
                .thenReturn(Completable.complete())

        viewModel.save()

        verify(databaseClientMock).updateItem(UUID, ITEM_NAME, FIRST_PROJECT_NAME)
        assertThat(viewModel.events.value, instanceOf(FinishActivity::class.java))
    }

    @Test
    fun `Should show error message when saving existing item failed`() {
        viewModel.uuid.value = UUID
        viewModel.name.value = ITEM_NAME
        viewModel.projectName.value = FIRST_PROJECT_NAME

        whenever(databaseClientMock
                .updateItem(UUID, ITEM_NAME, FIRST_PROJECT_NAME))
                .thenReturn(Completable.error(RuntimeException()))

        viewModel.save()

        verify(databaseClientMock).updateItem(UUID, ITEM_NAME, FIRST_PROJECT_NAME)
        assertThat(viewModel.events.value, instanceOf(ShowSaveFailedDialog::class.java))
    }

    @Test
    fun `Should delete item successfully`() {
        viewModel.uuid.value = UUID

        whenever(databaseClientMock.deleteItem(UUID)).thenReturn(Completable.complete())

        viewModel.delete()

        verify(databaseClientMock).deleteItem(UUID)
        assertThat(viewModel.events.value, instanceOf(FinishActivity::class.java))
    }

    @Test
    fun `Should mark item as valid when name is set`() {
        viewModel.name.value = ITEM_NAME


        assert(viewModel.isValid()).isEqualTo(true)
    }

    @Test
    fun `Should not mark item as valid when name is not set`() {
        assert(viewModel.isValid()).isEqualTo(false)
    }

    @Test
    fun `Should set selected project to last used`() {
        whenever(
                sharedPreferencesMock.getString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, "Inbox")
        ).thenReturn(SECOND_PROJECT_NAME)

        viewModel = ItemViewModel(applicationMock, databaseClientMock, sharedPreferencesMock, analyticsMock)

        assert(viewModel.projectName.value).isEqualTo(SECOND_PROJECT_NAME)
    }
}