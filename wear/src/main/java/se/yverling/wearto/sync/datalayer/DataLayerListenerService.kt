package se.yverling.wearto.sync.datalayer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.AppDatabase
import se.yverling.wearto.core.entities.Item
import javax.inject.Inject

private const val ITEMS_PATH = "/items"

private const val ITEMS_KEY = "ITEMS"

private const val ITEM_UUID_KEY = "UUID"
private const val ITEM_NAME_KEY = "NAME"
private const val ITEM_PROJECT_NAME = "PROJECT_NAME"
private const val ITEM_COLOR_KEY = "COLOR"

class DataLayerListenerService : WearableListenerService(), AnkoLogger {
    @Inject
    internal lateinit var database: AppDatabase

    private val disposables = CompositeDisposable()

    override fun onCreate() {
        WearToApplication.appComponent.inject(this)
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == ITEMS_PATH) {
                    val items = DataMapItem.fromDataItem(item).dataMap.getDataMapArrayList(ITEMS_KEY)
                    persistItems(items)
                }
            }
        }
    }

    private fun persistItems(dataMapItems: List<DataMap>) {
        val items = dataMapItems.map {
            info("SYNC: Got item: $it")
            Item(
                    it.getString(ITEM_UUID_KEY),
                    it.getString(ITEM_NAME_KEY),
                    it.getString(ITEM_PROJECT_NAME),
                    it.getInt(ITEM_COLOR_KEY)
            )
        }

        val disposable = Completable.fromCallable {
            database.itemDao().deleteAll()
            database.itemDao().saveAll(items)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            info("SYNC: All local items replaced")
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT < 26) {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(150)
                            } else {
                                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                        },

                        onError = {
                            error(it)
                        }
                )
        disposables.add(disposable)
    }
}
