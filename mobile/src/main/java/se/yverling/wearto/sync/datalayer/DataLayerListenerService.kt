package se.yverling.wearto.sync.datalayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import se.yverling.wearto.R
import se.yverling.wearto.core.WearToApplication
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.sync.network.NetworkClient
import java.util.*
import javax.inject.Inject

private const val SELECTED_ITEM_PATH = "/items/selected"

private const val SELECTED_ITEM_KEY = "ITEM_SELECTED"

private const val CHANNEL_ID = "wearto_error"

class DataLayerListenerService : WearableListenerService(), AnkoLogger {
    @Inject
    internal lateinit var networkClient: NetworkClient
    @Inject
    internal lateinit var databaseClient: DatabaseClient
    @Inject
    internal lateinit var analytics: FirebaseAnalytics

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
                if (item.uri.path == SELECTED_ITEM_PATH) {
                    val uuid = DataMapItem.fromDataItem(item).dataMap.getString(SELECTED_ITEM_KEY)
                    info("SELECTED ITEM: Item uuid: $uuid")
                    val disposable = databaseClient.findItemByUuid(uuid)
                            .doOnSuccess {
                                info("SELECTED ITEM: Found: $it")
                            }
                            .flatMapCompletable {
                                networkClient.addItem(it)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        info("SELECTED ITEM: Item added to Todoist API successfully")
                                        analytics.logEvent("add_item_to_api", bundleOf(Pair("result", "succeeded")))
                                    },

                                    onError = {
                                        analytics.logEvent("add_item_to_api", bundleOf(Pair("result", "failed")))
                                        error(it)
                                        sendErrorNotification()
                                    }
                            )
                    disposables.add(disposable)
                }
            }
        }
    }

    private fun sendErrorNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, "WearTO", importance)
            notificationManager.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_white_24dp)
                .setContentTitle(getString(R.string.could_not_send_item_text))
                .setContentText(getString(R.string.could_not_send_item_message))

        notificationManager.notify(Random().nextInt(1000), mBuilder.build())
    }
}