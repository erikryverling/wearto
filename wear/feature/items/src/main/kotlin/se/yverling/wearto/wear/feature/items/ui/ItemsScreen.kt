package se.yverling.wearto.wear.feature.items.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ChipIconWithProgress
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.common.ui.LoadingScreen
import se.yverling.wearto.wear.common.design.theme.SmallSpace
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.feature.items.R
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.*
import kotlin.random.Random

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: ItemsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        UiState.Loading -> LoadingScreen()

        is UiState.Success -> ItemsList(
            items = (uiState as UiState.Success).items,
            modifier = modifier,
        )

        // TODO Add empty state when sync is place
    }
}

// TODO Put into ViewModel when sync is in place
private enum class ItemState {
    Init,
    Loading,
    Success,
    Error
}

private const val COLOR_ANIMATION_DURATION_IN_MILLIS = 200

@Composable
@OptIn(ExperimentalHorologistApi::class)
private fun ItemsList(
    items: List<Item>,
    modifier: Modifier = Modifier,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip,
        ),
        rotaryMode = ScalingLazyColumnState.RotaryMode.Snap
    )

    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        items(items) { item ->
            // TODO Move out when sync is place

            val scope = rememberCoroutineScope()
            var itemState: ItemState by remember { mutableStateOf(ItemState.Init) }

            val context = LocalContext.current
            val vibratorManager = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

            var vibrationEffect: Int = 0
            var animationTargetValue: Color

            when (itemState) {
                ItemState.Init -> {
                    animationTargetValue = MaterialTheme.colorScheme.onSurface
                }

                ItemState.Loading -> {
                    animationTargetValue = MaterialTheme.colorScheme.onSurface
                }

                ItemState.Success -> {
                    vibrationEffect = VibrationEffect.EFFECT_DOUBLE_CLICK
                    animationTargetValue = MaterialTheme.colorScheme.primary
                }

                ItemState.Error -> {
                    vibrationEffect = VibrationEffect.EFFECT_HEAVY_CLICK
                    animationTargetValue = MaterialTheme.colorScheme.error
                }
            }

            val itemStateColor by animateColorAsState(
                animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_IN_MILLIS),
                targetValue = animationTargetValue,
                label = "itemStateColor",
                finishedListener = {
                    itemState = ItemState.Init
                }
            )

            vibratorManager.vibrate(
                VibrationEffect.createPredefined(vibrationEffect)
            )

            Chip(
                largeIcon = true,
                label = item.name,
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = itemStateColor,
                    iconColor = itemStateColor,
                ),
                icon = {
                    Box(modifier = Modifier.padding(end = SmallSpace)) {
                        if (itemState == ItemState.Loading) {
                            ChipIconWithProgress(
                                largeIcon = true,
                                icon = ImageVectorPaintable(Icons.Default.AddTask)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddTask,
                                tint = itemStateColor,
                                contentDescription = stringResource(R.string.add_icon_description)
                            )
                        }
                    }
                },

                onClick = {
                    // TODO Call ViewModel instead when sync in in place
                    scope.launch {
                        itemState = ItemState.Loading
                        delay(1000)
                        itemState = if (Random.nextBoolean()) ItemState.Success else ItemState.Error
                    }
                }
            )
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
)
@Composable
fun DefaultPreview() {
    WearToTheme {
        Surface {
            ItemsList(
                listOf(
                    Item(name = "Milk"),
                    Item(name = "Paper"),
                    Item(name = "Flour"),
                )
            )
        }
    }
}
