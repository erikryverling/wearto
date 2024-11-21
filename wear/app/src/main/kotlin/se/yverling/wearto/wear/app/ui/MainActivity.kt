package se.yverling.wearto.wear.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dagger.hilt.android.AndroidEntryPoint
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.feature.items.ui.ItemsRoute
import se.yverling.wearto.wear.feature.items.ui.ItemsScreen
import kotlin.getValue

@AndroidEntryPoint
@OptIn(ExperimentalHorologistApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearToTheme {
                AppScaffold {
                    val navController = rememberSwipeDismissableNavController()

                    val columnState = rememberResponsiveColumnState(
                        contentPadding = ScalingLazyColumnDefaults.padding(
                            first = ItemType.Chip,
                            last = ItemType.Chip,
                        ),
                    )

                    ScreenScaffold(scrollState = columnState) {
                        SwipeDismissableNavHost(
                            navController = navController,
                            startDestination = ItemsRoute
                        ) {
                            composable(ItemsRoute) {
                                ItemsScreen(columnState) { item ->
                                    viewModel.sendItem(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
