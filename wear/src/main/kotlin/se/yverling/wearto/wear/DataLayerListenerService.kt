package se.yverling.wearto.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber
import java.util.*

private const val PING_PATH = "/ping"
private const val PONG_PATH = "/pong"

private const val REQUEST_UUID_KEY = "REQUEST_UUID"
private const val MESSAGE_KEY = "Message"
private const val MESSAGE_VALUE = "Pong"


class DataLayerListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
        Timber.tag("WearTo").d("DataLayerListenerService initialized")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == PING_PATH) {
                    DataMapItem.fromDataItem(item).dataMap.getString(MESSAGE_KEY)?.let {
                        Timber.tag("WearTo").d("Got ping from mobile. Replying with pong.")
                        sendDataRequest()
                    }
                }
            }
        }
    }

    private fun sendDataRequest() {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(PONG_PATH)
        val dataMap = dataMapRequest.dataMap
        dataMap.putString(MESSAGE_KEY, MESSAGE_VALUE)
        dataMap.putString(REQUEST_UUID_KEY, UUID.randomUUID().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        val dataClient = Wearable.getDataClient(this)

        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Timber.tag("WearTo").d("DataRequest: Success")
            }

            .addOnFailureListener {
                Timber.tag("WearTo").d("DataRequest: Failure")
            }
    }
}
