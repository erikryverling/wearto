package se.yverling.wearto.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.design.theme.WearToTheme
import timber.log.Timber
import java.util.*

private const val PONG_PATH = "/pong"

private const val REQUEST_UUID_KEY = "REQUEST_UUID"
internal const val MESSAGE_KEY = "Message"
private const val MESSAGE_VALUE = "Pong"

@OptIn(ExperimentalHorologistApi::class)
class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkConnectionStatus()

        setContent {
            WearToTheme {
                MainScreen {
                    sendDataRequest()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == PONG_PATH) {
                    DataMapItem.fromDataItem(item).dataMap.getString(MESSAGE_KEY)?.let {
                        Timber.tag("WearTo").d("Got pong from wear")
                    }
                }
            }
        }
    }

    // TODO Extract common WearDataLayerRegistry code to a common module
    private fun checkConnectionStatus() {
        val wearDataLayerRegistry = WearDataLayerRegistry.fromContext(application, lifecycleScope)
        val appHelper = PhoneDataLayerAppHelper(this, wearDataLayerRegistry)

        lifecycleScope.launch {
            val nodes = appHelper.connectedNodes()
            Timber.tag("WearTo").d("Connected nodes: $nodes")
        }
    }

    // TODO Extract common DataEvent code to a common module
    private fun sendDataRequest() {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create("/ping")
        val dataMap = dataMapRequest.dataMap
        dataMap.putString(MESSAGE_KEY, MESSAGE_VALUE)
        dataMap.putString(REQUEST_UUID_KEY, UUID.randomUUID().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        val dataClient = Wearable.getDataClient(this)

        Timber.tag("WearTo").d("Sending ping to wear")
        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Timber.tag("WearTo").d("DataRequest: Success")
            }

            .addOnFailureListener {
                Timber.tag("WearTo").d("DataRequest: Failure")
            }
    }
}
