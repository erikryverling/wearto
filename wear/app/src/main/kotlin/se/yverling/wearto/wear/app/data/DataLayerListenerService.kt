package se.yverling.wearto.wear.app.data

import android.os.VibrationEffect
import android.os.VibratorManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.data.items.ItemStatusHolder
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import javax.inject.Inject

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {
    @Inject
    internal lateinit var itemsRepository: ItemsRepository

    @Inject
    internal lateinit var itemStatusHolder: ItemStatusHolder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val vibratorManager: VibratorManager by lazy {
        getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == ITEMS_PATH) {
                    DataMapItem.fromDataItem(item).dataMap.getStringArrayList(ITEMS_KEY)?.let { itemsAsStrings ->
                        val items = itemsAsStrings.map { Item(name = it) }

                        vibratorManager.defaultVibrator.vibrate(
                            VibrationEffect.createOneShot(
                                ITEMS_VIBRATION_CONFIRMATION_LENGTH,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )

                        serviceScope.launch {
                            itemsRepository.replaceItems(items)
                        }
                    }
                } else if (item.uri.path == ITEM_CONFIRM_PATH) {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    dataMap.getString(ITEM_SUCCESS_KEY)?.let { itemName ->
                        itemStatusHolder.setConfirmed(itemName, isSuccess = true)
                    }

                    dataMap.getString(ITEM_ERROR_KEY)?.let { itemName ->
                        itemStatusHolder.setConfirmed(itemName, isSuccess = false)
                    }
                }
            }
        }
    }

    companion object {
        private const val ITEMS_PATH = "/items"
        private const val ITEMS_KEY = "ITEMS"

        private const val ITEM_CONFIRM_PATH = "/item-confirm"
        private const val ITEM_SUCCESS_KEY = "ITEM_SUCCESS"
        private const val ITEM_ERROR_KEY = "ITEM_ERROR"

        private const val ITEMS_VIBRATION_CONFIRMATION_LENGTH = 500L
    }
}
