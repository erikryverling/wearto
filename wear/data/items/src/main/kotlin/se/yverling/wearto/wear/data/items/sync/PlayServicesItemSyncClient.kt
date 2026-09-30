package se.yverling.wearto.wear.data.items.sync

import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class PlayServicesItemSyncClient @Inject constructor(
    private val dataClient: DataClient,
) : ItemSyncClient {
    override suspend fun sendItem(name: String): Result<Unit> = runCatching {
        val dataMapRequest = PutDataMapRequest.create(ITEM_PATH).apply {
            dataMap.putString(ITEM_KEY, name)
            dataMap.putString(REQUEST_UUID_KEY, Uuid.random().toString())
            setUrgent()
        }

        dataClient.putDataItem(dataMapRequest.asPutDataRequest()).await()
        Unit
    }.onFailure { e ->
        Timber.e(e, "Failed to send item")
    }

    companion object {
        const val REQUEST_UUID_KEY = "REQUEST_UUID"
        const val ITEM_PATH = "/item"
        const val ITEM_KEY = "ITEM"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
