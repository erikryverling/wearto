package se.yverling.wearto.items

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.ArrayAdapter
import androidx.annotation.VisibleForTesting
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.R
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.items.ItemsViewModel.Event.DismissImportDialog
import se.yverling.wearto.items.ItemsViewModel.Event.RotateSyncIcon
import se.yverling.wearto.items.ItemsViewModel.Event.ShowAddTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.ShowImportFailedDialog
import se.yverling.wearto.items.ItemsViewModel.Event.ShowSyncTapTarget
import se.yverling.wearto.items.ItemsViewModel.Event.StartItemActivity
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToDataLayerDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToGeneralError
import se.yverling.wearto.items.ItemsViewModel.Event.SyncFailedDueToNetworkDialog
import se.yverling.wearto.items.ItemsViewModel.Event.SyncSucceededSnackbar
import se.yverling.wearto.items.edit.LATEST_SELECTED_PROJECT_PREFERENCES_KEY
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.sync.network.NetworkClient
import se.yverling.wearto.sync.network.dtos.ProjectDataResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

// We'll only fetch the first 200 completed items
@VisibleForTesting
const val COMPLETED_ITEMS_OFFSET = 0

@VisibleForTesting
const val COMPLETED_ITEMS_LIMIT = 200

const val DEFAULT_PROJECT = "Inbox"

class ItemsViewModel @Inject constructor(
        private val app: Application,
        private val databaseClient: DatabaseClient,
        private val networkClient: NetworkClient,
        private val dataLayerClient: DataLayerClient,
        private val sharedPreferences: SharedPreferences,
        val viewAdapter: ItemsRecyclerViewAdapter,
        private val analytics: FirebaseAnalytics
) : AndroidViewModel(app), AnkoLogger {

    val hasItems = MutableLiveData<Boolean>()

    val projectToBeImported = MutableLiveData<String>()
    val includeCompletedItemsInImport = MutableLiveData<Boolean>(false)
    val includeRemovedItemsWhenImporting = MutableLiveData<Boolean>()
    val isImporting = MutableLiveData<Boolean>()

    val importItemsAdapter = ArrayAdapter<String>(getApplication() as Context, R.layout.spinner_list_header, arrayListOf())

    internal val events = MutableLiveData<Event>()

    @VisibleForTesting
    internal var isSyncing = false

    private val disposables = CompositeDisposable()

    private val itemToEditEvents = MutableLiveData<String>()

    init {
        viewAdapter.init(itemToEditEvents)

        importItemsAdapter.setDropDownViewResource(R.layout.spinner_list_item)

        var disposable = databaseClient.findAllItemsWithProjectContinuously()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !isSyncing }
                .subscribeBy(
                        onNext = {
                            viewAdapter.setItems(it)
                            if (it.isEmpty()) {
                                events.value = ShowAddTapTarget
                                hasItems.value = false
                            } else {
                                events.value = ShowSyncTapTarget
                                hasItems.value = true
                            }
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)

        disposable = databaseClient.findAllProjects()
                .doOnSuccess {
                    importItemsAdapter.addAll(getSpinnerArray(it))
                }
                .flatMapMaybe {
                    Maybe.fromCallable<String> {
                        val selectedProjectName = sharedPreferences.getString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, DEFAULT_PROJECT)

                        if (it.find { it.name == selectedProjectName } != null) {
                            selectedProjectName
                        } else {
                            null
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            projectToBeImported.value = it
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun getItemToEditEvents(): MutableLiveData<String> = itemToEditEvents

    internal fun sync() {
        isSyncing = true
        events.value = RotateSyncIcon
        val disposable = networkClient.getProjects()
                .doOnSuccess {
                    info("SYNC: New projects fetched")
                }
                .flatMapCompletable {
                    databaseClient.replaceAllProjects(it.projects)
                }
                .doOnComplete {
                    println("SYNC: All local projects replaced")
                }
                .andThen(
                        databaseClient.findAllOrphanItems()
                                .flatMapObservable {
                                    it.toObservable()
                                }
                                .flatMapCompletable {
                                    info("SYNC: Updating orphan item: $it")
                                    databaseClient.updateItem(it.uuid, it.name, DEFAULT_PROJECT)
                                }
                )
                .andThen(
                        dataLayerClient.syncProjects()
                                .doOnComplete {
                                    println("SYNC: Items sent to wear")
                                }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            info("SYNC: Completed!")
                            analytics.logEvent("sync", bundleOf(Pair("result", "succeeded")))
                            events.value = SyncSucceededSnackbar
                            isSyncing = false
                        },

                        onError = {
                            error(it)

                            if (it is UnknownHostException || it is SocketTimeoutException) {
                                analytics.logEvent("sync", bundleOf(Pair("result", "failed due to network error")))
                                events.value = SyncFailedDueToNetworkDialog
                            } else if (it is DataLayerClient.DataLayerException) {
                                analytics.logEvent("sync", bundleOf(Pair("result", "failed due to data layer error")))
                                events.value = SyncFailedDueToDataLayerDialog
                            } else {
                                analytics.logEvent("sync", bundleOf(Pair("result", "failed due to general error")))
                                events.value = SyncFailedDueToGeneralError
                            }
                            isSyncing = false
                        }
                )
        disposables.add(disposable)
    }

    fun importItems() {
        events.value = DismissImportDialog

        isImporting.value = true

        val disposable = databaseClient.findProjectByName(projectToBeImported.value!!)
                .flatMap {
                    networkClient.getItems(it.id)
                }
                .mergeWith(
                        if (!includeCompletedItemsInImport.value!!) {
                            // Pass an empty list so we don't emit events for completed items at this point
                            Single.just(ProjectDataResponse(listOf()))
                        } else {
                            databaseClient.findProjectByName(projectToBeImported.value!!).flatMap {
                                networkClient.getCompletedItems(it.id, COMPLETED_ITEMS_OFFSET, COMPLETED_ITEMS_LIMIT)
                            }
                        }
                )
                .flatMap {
                    it.items.toFlowable()
                }
                .filter {
                    val duplicateItem = databaseClient.findByNameAndProjectId(it.content, it.projectId).blockingGet()
                    duplicateItem == null || (includeRemovedItemsWhenImporting.value!! && duplicateItem.deleted)
                }.flatMapCompletable {
                    databaseClient.saveItem(it.content, projectToBeImported.value as String)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            info("IMPORT: Items imported")
                            analytics.logEvent("import", bundleOf(Pair("result", "succeeded")))
                            isImporting.value = false
                        },

                        onError = {
                            error(it)
                            analytics.logEvent("import", bundleOf(Pair("result", "failed")))
                            isImporting.value = false
                            events.value = ShowImportFailedDialog
                        }
                )
        disposables.add(disposable)
    }

    fun cancelItemImport() {
        events.value = DismissImportDialog
    }

    fun addItem() {
        events.value = StartItemActivity
    }

    internal fun clear() {
        disposables.clear()
    }

    fun itemDecoration() = DividerItemDecoration(app, LinearLayoutManager.VERTICAL)

    fun layoutManager() = LinearLayoutManager(app)

    private fun getSpinnerArray(projects: List<Project>): ArrayList<String> {
        val names = projects.asSequence().filter { it.name != DEFAULT_PROJECT }.map { it.name }.toList()
        return ArrayList(listOf(DEFAULT_PROJECT) + names)
    }

    sealed class Event {
        object StartItemActivity : Event()

        object ShowAddTapTarget : Event()
        object ShowSyncTapTarget : Event()

        object RotateSyncIcon : Event()

        object SyncSucceededSnackbar : Event()
        object SyncFailedDueToNetworkDialog : Event()
        object SyncFailedDueToDataLayerDialog : Event()
        object SyncFailedDueToGeneralError : Event()

        object DismissImportDialog : Event()
        object ShowImportFailedDialog : Event()
    }
}

@BindingAdapter("viewAdapter")
fun adapter(view: RecyclerView, adapter: ItemsRecyclerViewAdapter) {
    view.adapter = adapter
}

@BindingAdapter("itemDecoration")
fun adapter(view: RecyclerView, decoration: RecyclerView.ItemDecoration) {
    view.addItemDecoration(decoration)
}