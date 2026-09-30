package se.yverling.wearto.mobile.data.items.sync

import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import se.yverling.wearto.mobile.data.items.model.Item
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class PlayServicesItemsSyncPublisher @Inject constructor(
    private val dataClient: DataClient,
) : ItemsSyncPublisher {
    override suspend fun publishItems(items: List<Item>): Result<Unit> = runCatching {
        val dataMapRequest = PutDataMapRequest.create(ITEMS_PATH).apply {
            dataMap.putStringArrayList(ITEMS_KEY, ArrayList(items.map { it.name }))
            dataMap.putString(REQUEST_UUID_KEY, Uuid.random().toString())
            setUrgent()
        }

        dataClient.putDataItem(dataMapRequest.asPutDataRequest()).await()
        Unit
    }.onFailure { e ->
        Timber.e(e, "Could not send items to wear")
    }

    companion object {
        const val REQUEST_UUID_KEY = "REQUEST_UUID"
        const val ITEMS_PATH = "/items"
        const val ITEMS_KEY = "ITEMS"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
