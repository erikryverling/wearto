package se.yverling.wearto.sync.datalayer

import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import se.yverling.wearto.core.db.DatabaseClient
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val ITEMS_PATH = "/items"

private const val ITEMS_KEY = "ITEMS"

private const val REQUEST_UUID_KEY = "REQUEST_UUID"

private const val ITEM_UUID_KEY = "UUID"
private const val ITEM_NAME_KEY = "NAME"
private const val ITEM_COLOR_KEY = "COLOR"
private const val ITEM_PROJECT_NAME = "PROJECT_NAME"

private const val TIME_OUT_IN_SEC = 5L

class DataLayerClient @Inject constructor(
        private val dataClient: DataClient,
        private val nodeClient: NodeClient,
        private val databaseClient: DatabaseClient
) : AnkoLogger {

    fun syncProjects(): Completable {
        return checkConnectivity()
                .observeOn(Schedulers.io())
                .andThen(getAllItemsAsDataMap())
                .flatMapCompletable {
                    getDataItemResult(it)
                }
    }

    private fun checkConnectivity(): Completable {
        return Completable.create {
            val task = nodeClient.connectedNodes
                    .addOnSuccessListener { connectedNodes ->
                        if (connectedNodes.isEmpty()) {
                            it.onError(DataLayerException("Not connected to any wearables"))
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

    private fun getAllItemsAsDataMap(): Single<ArrayList<DataMap>> {
        return databaseClient.findAllItemsWithProject()
                .map { itemsWithProject ->
                    val maps = itemsWithProject.map {
                        val dataMap = DataMap()
                        dataMap.putString(ITEM_UUID_KEY, it.item.uuid)
                        dataMap.putString(ITEM_NAME_KEY, it.item.name)
                        dataMap.putInt(ITEM_COLOR_KEY, it.project.color)
                        dataMap.putString(ITEM_PROJECT_NAME, it.project.name)
                        dataMap
                    }
                    java.util.ArrayList<DataMap>(maps)
                }
    }

    private fun getDataItemResult(dataMapList: ArrayList<DataMap>): Completable {
        return Completable.create {
            val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(ITEMS_PATH)
            val dataMap = dataMapRequest.dataMap
            dataMap.putDataMapArrayList(ITEMS_KEY, dataMapList)
            dataMap.putString(REQUEST_UUID_KEY, UUID.randomUUID().toString())

            val request = dataMapRequest.asPutDataRequest()
            dataMapRequest.setUrgent()

            val task = dataClient.putDataItem(request)
                    .addOnSuccessListener { _ ->
                        it.onComplete()
                    }

                    .addOnFailureListener { exception ->
                        it.onError(DataLayerException("Failed sending Data layer request due to: ${exception.message}"))
                    }
            Tasks.await(task, TIME_OUT_IN_SEC, TimeUnit.SECONDS)
        }
    }

    class DataLayerException(message: String) : RuntimeException(message)
}