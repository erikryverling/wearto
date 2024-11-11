package se.yverling.wearto.wear.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dagger.hilt.android.AndroidEntryPoint
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.feature.items.ui.ItemsScreen
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
@OptIn(ExperimentalHorologistApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearToTheme {
                val navController = rememberSwipeDismissableNavController()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "items_screen"
                ) {
                    composable("items_screen") {
                        ItemsScreen { item ->
                            sendItem(item)
                        }
                    }
                }
            }
        }
    }

    private fun sendItem(item: Item) {
        val dataMapRequest: PutDataMapRequest = PutDataMapRequest.create(ITEM_PATH)
        val dataMap = dataMapRequest.dataMap

        dataMap.putString(ITEM_KEY, item.name)
        dataMap.putString(REQUEST_UUID_KEY, UUID.randomUUID().toString())

        val request = dataMapRequest.asPutDataRequest()
        dataMapRequest.setUrgent()

        val dataClient = Wearable.getDataClient(this)

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
