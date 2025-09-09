package se.yverling.wearto.mobile.app.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import se.yverling.wearto.mobile.data.items.model.Item
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class DataLayerRepository @Inject constructor(private val dataClient: DataClient) {
    fun sendItems(items: List<Item>) {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(ITEMS_PATH)
        val dataMap = dataMapRequest.dataMap

        dataMap.putStringArrayList(ITEMS_KEY, items.toStringArrayList())
        dataMap.putString(REQUEST_UUID_KEY, Uuid.random().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        dataClient.putDataItem(request)
            .addOnFailureListener {
                Timber.e("Could not send items to wear")
            }
    }

    companion object {
        private const val REQUEST_UUID_KEY = "REQUEST_UUID"
        private const val ITEMS_PATH = "/items"
        private const val ITEMS_KEY = "ITEMS"
    }
}

private fun List<Item>.toStringArrayList(): java.util.ArrayList<String> = ArrayList(map { it.name })
