package se.yverling.wearto.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.watch.WearDataLayerAppHelper
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.design.theme.WearToTheme
import timber.log.Timber

@OptIn(ExperimentalHorologistApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkConnectionStatus()

        setContent {
            WearToTheme {
                MainScreen()
            }
        }
    }

    private fun checkConnectionStatus() {
        val wearDataLayerRegistry = WearDataLayerRegistry.fromContext(this, lifecycleScope)
        val appHelper = WearDataLayerAppHelper(this, wearDataLayerRegistry, lifecycleScope)

        lifecycleScope.launch {
            val connectedNodes = appHelper.connectedNodes()
            Timber.tag("WearTo").d("Connected nodes: $connectedNodes")
        }
    }
}
