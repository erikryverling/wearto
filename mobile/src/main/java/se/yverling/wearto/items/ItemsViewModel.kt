package se.yverling.wearto.items

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.res.ColorStateList
import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.items.ItemsViewModel.Events.*
import se.yverling.wearto.sync.datalayer.DataLayerClient
import se.yverling.wearto.sync.network.NetworkClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ItemsViewModel @Inject constructor(
        private val app: Application,
        private val databaseClient: DatabaseClient,
        private val networkClient: NetworkClient,
        private val dataLayerClient: DataLayerClient,
        val viewAdapter: ItemsRecyclerViewAdapter
) : AndroidViewModel(app), AnkoLogger {

    val hasItems = ObservableBoolean()

    internal val events = SingleLiveEvent<Events>()

    @VisibleForTesting
    internal var isSyncing = false

    private val disposables = CompositeDisposable()

    private val itemToEditEvents = SingleLiveEvent<String>()

    init {
        viewAdapter.onItemClick {
            itemToEditEvents.value = it.uuid
        }

        val disposable = databaseClient.findAllItemsWithProjectContinuously()
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
                                    databaseClient.updateItem(it.uuid, it.name, "Inbox")
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

    fun addItem() {
        events.value = START_ITEM_ACTIVITY_EVENT
    }

    internal fun clear() {
        disposables.clear()
    }

    fun itemDecoration() = DividerItemDecoration(app, LinearLayoutManager.VERTICAL)

    fun layoutManager() = LinearLayoutManager(app)

    class ItemsRecyclerViewAdapter @Inject constructor() : RecyclerView.Adapter<ItemsRecyclerViewAdapter.TemplateViewHolder>() {
        private var itemsWithProject: List<ItemWithProject> = mutableListOf()
        private lateinit var onItemClick: (Item) -> Unit

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
            val itemView = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.items_list_item, parent, false)
            return TemplateViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
            holder.item.text = itemsWithProject[position].item.name
            holder.project.text = itemsWithProject[position].project.name
            holder.project.backgroundTintList = ColorStateList.valueOf(itemsWithProject[position].project.color)

            holder.itemView.setOnClickListener { onItemClick(itemsWithProject[position].item) }
        }

        override fun getItemCount(): Int {
            return itemsWithProject.size
        }

        fun onItemClick(function: (Item) -> (Unit)) {
            this.onItemClick = function
        }

        fun setItems(itemsWithProject: List<ItemWithProject>) {
            this.itemsWithProject = itemsWithProject
            notifyDataSetChanged()
        }

        class TemplateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val item: TextView = view.findViewById(R.id.item_name)
            val project: TextView = view.findViewById(R.id.project_name)
        }
    }
    
    enum class Events {
        START_ITEM_ACTIVITY_EVENT,

        SHOW_ADD_TAP_TARGET_EVENT,
        SHOW_SYNC_TAP_TARGET_EVENT,

        ROTATE_SYNC_ICON_EVENT,

        SYNC_SUCCEEDED_SNACKBAR_EVENT,
        SYNC_FAILED_DUE_TO_NETWORK_DIALOG_EVENT,
        SYNC_FAILED_DUE_TO_DATA_LAYER_DIALOG_EVENT,
        SYNC_FAILED_DUE_TO_GENERAL_ERROR_EVENT
    }
}

@BindingAdapter("viewAdapter")
fun adapter(view: RecyclerView, adapter: ItemsViewModel.ItemsRecyclerViewAdapter) {
    view.adapter = adapter
}

@BindingAdapter("itemDecoration")
fun adapter(view: RecyclerView, decoration: RecyclerView.ItemDecoration) {
    view.addItemDecoration(decoration)
}