package se.yverling.wearto.wear.app.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import se.yverling.wearto.wear.data.items.model.Item
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class DataLayerRepository @Inject constructor(private val dataClient: DataClient) {
    fun sendItem(item: Item) {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(ITEM_PATH)
        val dataMap = dataMapRequest.dataMap

        dataMap.putString(ITEM_KEY, item.name)
        dataMap.putString(REQUEST_UUID_KEY, Uuid.random().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        dataClient.putDataItem(request)
            .addOnFailureListener {
                Timber.e("Failed to send item")
            }
    }

    companion object {
        private const val REQUEST_UUID_KEY = "REQUEST_UUID"

        private const val ITEM_PATH = "/item"
        private const val ITEM_KEY = "ITEM"
    }
}
