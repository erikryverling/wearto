package se.yverling.wearto.wear.feature.items.ui

import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.common.ui.LoadingScreen
import se.yverling.wearto.wear.common.design.theme.DefaultSpace
import se.yverling.wearto.wear.common.design.theme.SmallSpace
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState.*
import se.yverling.wearto.wear.feature.items.R
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.*
import se.yverling.wearto.wear.feature.items.ui.theme.AddIconSize


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ItemsScreen(
    modifier: Modifier = Modifier,
    viewModel: ItemsViewModel = hiltViewModel(),
    onAddItem: (Item) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()

    when (uiState) {
        UiState.Loading -> LoadingScreen()

        is UiState.Success -> {
            val items = (uiState as UiState.Success).items

            if (items.isEmpty()) {
                EmptyScreen()
            } else {
                ItemsList(
                    items = (uiState as UiState.Success).items,
                    modifier = modifier,
                    onAddItem = { item ->
                        scope.launch {
                            viewModel.setItemStateToLoading(item)
                            onAddItem(item)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tint = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(AddIconSize),
            imageVector = Icons.Outlined.LibraryAdd,
            contentDescription = stringResource(R.string.add_icon_description)
        )
        Text(
            color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = DefaultSpace),
            text = stringResource(R.string.empty_state_description),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
    }
}

private const val COLOR_ANIMATION_DURATION_IN_MILLIS = 200

@Composable
@OptIn(ExperimentalHorologistApi::class)
private fun ItemsList(
    items: List<Item>,
    modifier: Modifier = Modifier,
    onAddItem: (Item) -> Unit,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip,
        ),
        rotaryMode = ScalingLazyColumnState.RotaryMode.Snap
    )
    val context = LocalContext.current

    val vibratorManager: VibratorManager by lazy {
        (context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager)
    }

    var vibrationEffect: Int = 0
    var animationTargetValue: Color


    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        columnState = columnState,
    ) {
        items(items) { item ->
            when (item.state) {
                Init -> {
                    animationTargetValue = MaterialTheme.colorScheme.onSurface
                }

                Loading -> {
                    animationTargetValue = MaterialTheme.colorScheme.onSurface
                }

                Successful -> {
                    vibrationEffect = VibrationEffect.EFFECT_DOUBLE_CLICK
                    animationTargetValue = MaterialTheme.colorScheme.primary
                }

                Error -> {
                    vibrationEffect = VibrationEffect.EFFECT_HEAVY_CLICK
                    animationTargetValue = MaterialTheme.colorScheme.error
                }
            }

            val itemStateColor by animateColorAsState(
                animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_IN_MILLIS),
                targetValue = animationTargetValue,
                label = "itemStateColor",
            )

            vibratorManager.defaultVibrator.vibrate(
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
                        if (item.state == Loading) {
                            ChipIconWithProgress(
                                progressIndicatorColor = MaterialTheme.colorScheme.primary,
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

                onClick = { onAddItem(item) }
            )
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
)
@Composable
fun ItemsListPreview() {
    WearToTheme {
        Surface {
            ItemsList(
                items = listOf(
                    Item(name = "Milk"),
                    Item(name = "Paper"),
                    Item(name = "Flour"),
                ),
                onAddItem = {}
            )
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
)
@Composable
fun EmptyScreenPreview() {
    WearToTheme {
        Surface {
            EmptyScreen()
        }
    }
}

