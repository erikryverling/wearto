package se.yverling.wearto.wear.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dagger.hilt.android.AndroidEntryPoint
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.feature.items.ui.MainScreen

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
                    startDestination = "main_screen"
                ) {
                    composable("main_screen") {
                        MainScreen()
                    }
                }
            }
        }
    }
}
