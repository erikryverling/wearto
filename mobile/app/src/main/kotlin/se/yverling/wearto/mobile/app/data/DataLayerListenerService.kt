package se.yverling.wearto.mobile.app.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import se.yverling.wearto.mobile.data.item.ItemRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {
    @Inject
    internal lateinit var itemRepository: ItemRepository

    @Inject
    internal lateinit var dataClient: DataClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == ITEM_PATH) {
                    DataMapItem.fromDataItem(item).dataMap.getString(ITEM_KEY).let { itemName ->
                        if (itemName == null) throw IllegalArgumentException("Item name is null")

                        // We need to make sure that the service is not destroyed while the request is running
                        runBlocking {
                            val isSuccessful = try {
                                itemRepository.addItem(itemName)
                                true
                            } catch (e: Exception) {
                                Timber.e(e)
                                false
                            }

                            confirmItem(itemName, isSuccessful)
                        }
                    }
                }
            }
        }
    }

    private fun confirmItem(itemName: String, isSuccess: Boolean) {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(ITEM_CONFIRM_PATH)
        val dataMap = dataMapRequest.dataMap

        val key = if (isSuccess) ITEM_SUCCESS_KEY else ITEM_ERROR_KEY

        dataMap.putString(key, itemName)
        dataMap.putString(REQUEST_UUID_KEY, Uuid.random().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        dataClient.putDataItem(request)
            .addOnFailureListener {
                Timber.e("Could not confirm item")
            }
    }

    companion object {
        private const val REQUEST_UUID_KEY = "REQUEST_UUID"

        private const val ITEM_PATH = "/item"
        private const val ITEM_KEY = "ITEM"

        private const val ITEM_CONFIRM_PATH = "/item-confirm"
        private const val ITEM_SUCCESS_KEY = "ITEM_SUCCESS"
        private const val ITEM_ERROR_KEY = "ITEM_ERROR"
    }
}
