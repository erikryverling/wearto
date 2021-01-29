package se.yverling.wearto.chars

import android.app.Application
import androidx.annotation.VisibleForTesting
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
import se.yverling.wearto.core.db.AppDatabase
import javax.inject.Inject

class CharsViewModel @Inject constructor(
        app: Application,
        dataBase: AppDatabase,
        val viewAdapter: CharsRecyclerViewAdapter
) : AndroidViewModel(app), AnkoLogger {
    internal val events = MutableLiveData<Event>()

    private val disposables = CompositeDisposable()

    init {
        viewAdapter.onCharClick { char -> showItemsList(char) }

        val disposable = dataBase.itemDao().findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { result ->

                            val listOfChars: List<Char> =
                                    result.map {
                                        it.name.first()
                                    }.distinct()

                            // If the list of items or the list of chars are too small, skip showing the char list
                            if (result.size < 10 || listOfChars.size < 3) {
                                showItemsList(null)
                            } else {
                                viewAdapter.setChars(listOfChars)
                            }
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }

    @VisibleForTesting
    internal fun showItemsList(char: Char?) {
        events.value = Event.StartItemsActivity(char)
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun layoutManager() = WearableLinearLayoutManager(getApplication())
}

sealed class Event {
    data class StartItemsActivity(val char: Char?) : Event()
}

@BindingAdapter("viewAdapter")
fun adapter(view: WearableRecyclerView, adapter: CharsRecyclerViewAdapter) {
    view.adapter = adapter
}

@BindingAdapter("isEdgeItemsCenteringEnabled")
fun adapter(view: WearableRecyclerView, enabled: Boolean) {
    view.isEdgeItemsCenteringEnabled = enabled
}