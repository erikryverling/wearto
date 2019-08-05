package se.yverling.wearto.items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.annotation.VisibleForTesting
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.core.SingleLiveEvent
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.items.Events.SHOW_ITEM_SELECTION_FAILED_EVENT
import se.yverling.wearto.sync.datalayer.DataLayerClient
import javax.inject.Inject

class ItemsViewModel @Inject constructor(
        app: Application,
        dataBase: AppDatabase,
        private val dataLayerClient: DataLayerClient,
        val viewAdapter: ItemsRecyclerViewAdapter
) : AndroidViewModel(app), AnkoLogger {
    internal val events = SingleLiveEvent<Events>()

    val hasItems = ObservableBoolean()

    @VisibleForTesting
    internal val selectedItem = MutableLiveData<Item>()

    private val disposables = CompositeDisposable()

    init {
        viewAdapter.onItemClick { item ->
            info("SELECTED ITEM: Item name: ${item.name}")
            sendItem(item)
        }

        val disposable = dataBase.itemDao().findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            viewAdapter.setItems(it)
                            hasItems.set(it.isNotEmpty())
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

    fun getSelectedItem(): LiveData<Item> {
        return selectedItem
    }

    fun layoutManager() = WearableLinearLayoutManager(getApplication())

    @VisibleForTesting
    internal fun sendItem(item: Item) {
        info("SELECTED ITEM: Item name: ${item.name}")
        dataLayerClient.sendSelectedItem(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            info("SELECTED ITEM: Item sent to mobile")
                            selectedItem.value = it
                        },

                        onError = {
                            error(it)
                            events.value = SHOW_ITEM_SELECTION_FAILED_EVENT
                        }
                )
    }
}

enum class Events {
    SHOW_ITEM_SELECTION_FAILED_EVENT
}

@BindingAdapter("viewAdapter")
fun adapter(view: WearableRecyclerView, adapter: ItemsRecyclerViewAdapter) {
    view.adapter = adapter
}

@BindingAdapter("isEdgeItemsCenteringEnabled")
fun adapter(view: WearableRecyclerView, enabled: Boolean) {
    view.isEdgeItemsCenteringEnabled = enabled
}