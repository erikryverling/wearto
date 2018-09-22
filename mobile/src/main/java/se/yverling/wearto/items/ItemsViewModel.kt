package se.yverling.wearto.items

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.content.SharedPreferences
import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ArrayAdapter
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.R
import se.yverling.wearto.core.SingleLiveEvent
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.core.entities.Project
import se.yverling.wearto.items.ItemsViewModel.Events.*
import se.yverling.wearto.items.edit.LATEST_SELECTED_PROJECT_PREFERENCES_KEY
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.sync.network.NetworkClient
import se.yverling.wearto.sync.network.dtos.ProjectDataResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

// We'll only fetch the first 200 completed items
private const val COMPLETED_ITEMS_OFFSET = 0
private const val COMPLETED_ITEMS_LIMIT = 200

private const val DEFAULT_PROJECT = "Inbox"

class ItemsViewModel @Inject constructor(
        private val app: Application,
        private val databaseClient: DatabaseClient,
        private val networkClient: NetworkClient,
        private val dataLayerClient: DataLayerClient,
        private val sharedPreferences: SharedPreferences,
        val viewAdapter: ItemsRecyclerViewAdapter
) : AndroidViewModel(app), AnkoLogger {

    val hasItems = ObservableBoolean()

    val projectToBeImported = ObservableField<String>()
    val includeCompletedItemsInImport = ObservableBoolean()
    val includeRemovedItemsWhenImporting = ObservableBoolean()
    val isImporting = ObservableBoolean()

    val importItemsAdapter = ArrayAdapter<String>(getApplication() as Context, R.layout.spinner_list_header, arrayListOf())

    internal val events = SingleLiveEvent<Events>()

    @VisibleForTesting
    internal var isSyncing = false


    private val disposables = CompositeDisposable()

    private val itemToEditEvents = SingleLiveEvent<String>()

    init {
        viewAdapter.onItemClick {
            itemToEditEvents.value = it.uuid
        }

        importItemsAdapter.setDropDownViewResource(R.layout.spinner_list_item)

        var disposable = databaseClient.findAllItemsWithProjectContinuously()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { !isSyncing }
                .subscribeBy(
                        onNext = {
                            viewAdapter.setItems(it)
                            if (it.isEmpty()) {
                                events.value = SHOW_ADD_TAP_TARGET_EVENT
                                hasItems.set(false)
                            } else {
                                events.value = SHOW_SYNC_TAP_TARGET_EVENT
                                hasItems.set(true)
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
                        val selectedProjectName
                                = sharedPreferences.getString(LATEST_SELECTED_PROJECT_PREFERENCES_KEY, DEFAULT_PROJECT)

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
                            projectToBeImported.set(it)
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

    fun getItemToEditEvents(): SingleLiveEvent<String> = itemToEditEvents

    internal fun sync() {
        isSyncing = true
        events.value = ROTATE_SYNC_ICON_EVENT
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
                            events.value = SYNC_SUCCEEDED_SNACKBAR_EVENT
                            isSyncing = false
                        },

                        onError = {
                            error(it)

                            if (it is UnknownHostException || it is SocketTimeoutException) {
                                events.value = SYNC_FAILED_DUE_TO_NETWORK_DIALOG_EVENT
                            } else if (it is DataLayerClient.DataLayerException) {
                                events.value = SYNC_FAILED_DUE_TO_DATA_LAYER_DIALOG_EVENT
                            } else {
                                events.value = SYNC_FAILED_DUE_TO_GENERAL_ERROR_EVENT
                            }
                            isSyncing = false
                        }
                )
        disposables.add(disposable)
    }

    fun importItems() {
        events.value = DISMISS_IMPORT_DIALOG_EVENT

        isImporting.set(true)

        val disposable = databaseClient.findProjectByName(projectToBeImported.get()!!)
                .flatMap {
                    networkClient.getItems(it.id)
                }
                .mergeWith(
                        if (!includeCompletedItemsInImport.get()) {
                            // Pass an empty list so we don't emit events for completed items at this point
                            Single.just(ProjectDataResponse(listOf()))
                        } else {
                            databaseClient.findProjectByName(projectToBeImported.get()!!).flatMap {
                                networkClient.getCompletedItems(it.id, COMPLETED_ITEMS_OFFSET, COMPLETED_ITEMS_LIMIT)
                            }
                        }
                )
                .flatMap {
                    it.items.toFlowable()
                }
                .filter {
                    val duplicateItem = databaseClient.findByNameAndProjectId(it.content, it.projectId).blockingGet()
                    duplicateItem == null || (includeRemovedItemsWhenImporting.get() && duplicateItem.deleted)
                }.flatMapCompletable {
                    databaseClient.saveItem(it.content, projectToBeImported.get() as String)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            isImporting.set(false)
                        },

                        onError = {
                            isImporting.set(false)
                            events.value = SHOW_IMPORT_FAILED_DIALOG_EVENT
                        }
                )
        disposables.add(disposable)
    }

    fun cancelItemImport() {
        events.value = DISMISS_IMPORT_DIALOG_EVENT
    }

    fun addItem() {
        events.value = START_ITEM_ACTIVITY_EVENT
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

    enum class Events {
        START_ITEM_ACTIVITY_EVENT,

        SHOW_ADD_TAP_TARGET_EVENT,
        SHOW_SYNC_TAP_TARGET_EVENT,

        ROTATE_SYNC_ICON_EVENT,

        SYNC_SUCCEEDED_SNACKBAR_EVENT,
        SYNC_FAILED_DUE_TO_NETWORK_DIALOG_EVENT,
        SYNC_FAILED_DUE_TO_DATA_LAYER_DIALOG_EVENT,
        SYNC_FAILED_DUE_TO_GENERAL_ERROR_EVENT,

        DISMISS_IMPORT_DIALOG_EVENT,
        SHOW_IMPORT_FAILED_DIALOG_EVENT,
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