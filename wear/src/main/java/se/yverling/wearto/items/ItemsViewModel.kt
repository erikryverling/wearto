package se.yverling.wearto.items

import android.app.Application
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.items.Event.ShowConfirmationAndFinish
import se.yverling.wearto.items.Event.ShowItemSelectionFailed
import se.yverling.wearto.sync.datalayer.DataLayerClient

class ItemsViewModel constructor(
        app: Application,
        dataBase: AppDatabase,
        private val dataLayerClient: DataLayerClient,
        val viewAdapter: ItemsRecyclerViewAdapter,
        private val char: Char?
) : AndroidViewModel(app), AnkoLogger {
    internal val events = MutableLiveData<Event>()

    val hasItems = MutableLiveData(false)

    private val disposables = CompositeDisposable()

    init {
        viewAdapter.init(events)

        val disposable = dataBase.itemDao().findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { allItems ->

                            val finalList = if (char == null) {
                                allItems
                            } else {
                                allItems.filter { it.name.first() == char }.toList()
                            }

                            viewAdapter.setItems(finalList)
                            hasItems.value = finalList.isNotEmpty()
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

    fun layoutManager() = WearableLinearLayoutManager(getApplication())

    internal fun sendItem(item: Item) {
        info("SELECTED ITEM: Item name: ${item.name}")
        val disposable = dataLayerClient.sendSelectedItem(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            info("SELECTED ITEM: Item sent to mobile")
                            events.value = ShowConfirmationAndFinish(it)
                        },

                        onError = {
                            error(it)
                            events.value = ShowItemSelectionFailed
                        }
                )
        disposables.add(disposable)
    }
}

sealed class Event {
    object ShowItemSelectionFailed : Event()
    data class ShowConfirmationAndFinish(val item: Item) : Event()
    data class SendItem(val item: Item) : Event()
}

@BindingAdapter("viewAdapter")
fun adapter(view: WearableRecyclerView, adapter: ItemsRecyclerViewAdapter) {
    view.adapter = adapter
}

@BindingAdapter("isEdgeItemsCenteringEnabled")
fun adapter(view: WearableRecyclerView, enabled: Boolean) {
    view.isEdgeItemsCenteringEnabled = enabled
}