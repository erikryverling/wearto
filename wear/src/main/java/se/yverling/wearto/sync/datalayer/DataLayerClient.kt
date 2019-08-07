package se.yverling.wearto.sync.datalayer

import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import se.yverling.wearto.core.entities.Item
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SELECTED_ITEM_PATH = "/items/selected"

private const val ITEM_SELECTED_KEY = "ITEM_SELECTED"

private const val REQUEST_UUID_KEY = "REQUEST_UUID"

private const val TIME_OUT_IN_SEC = 5L

class DataLayerClient @Inject constructor(
        private val dataClient: DataClient,
        private val nodeClient: NodeClient
) {

    fun sendSelectedItem(item: Item): Single<Item> {
        return checkConnectivity()
                .observeOn(Schedulers.io())
                .andThen(getDataItemResult(item))
    }

    private fun checkConnectivity(): Completable {
        return Completable.create {
            val task = nodeClient.connectedNodes
                    .addOnSuccessListener { connectedNodes ->
                        if (connectedNodes.isEmpty()) {
                            it.onError(Exception("Not connected to any wearables"))
                        } else {
                            it.onComplete()
                        }
                    }

                    .addOnFailureListener { exception ->
                        it.onError(Exception("Failed checking connected wearables due to: ${exception.message}"))
                    }
            Tasks.await(task, TIME_OUT_IN_SEC, TimeUnit.SECONDS)
        }
    }

    private fun getDataItemResult(item: Item): Single<Item>? {
        return Single.create {
            val dataMapRequest = PutDataMapRequest.create(SELECTED_ITEM_PATH)

            val dataMap = dataMapRequest.dataMap
            dataMap.putString(ITEM_SELECTED_KEY, item.uuid)

            // This is to guarantee that the data map is unique and will be passed to Handheld
            dataMap.putString(REQUEST_UUID_KEY, UUID.randomUUID().toString())

            val request = dataMapRequest.asPutDataRequest()
            dataMapRequest.setUrgent()

            val task = dataClient.putDataItem(request)
            task.addOnSuccessListener { _ ->
                it.onSuccess(item)
            }.addOnFailureListener { exception ->
                it.onError(exception)
            }
            Tasks.await(task, 5, TimeUnit.SECONDS)
        }
    }
}