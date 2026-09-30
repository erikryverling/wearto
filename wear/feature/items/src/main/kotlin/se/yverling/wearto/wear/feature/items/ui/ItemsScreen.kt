package se.yverling.wearto.wear.feature.items.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.ChipIconWithProgress
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable
import se.yverling.wearto.common.ui.LoadingScreen
import se.yverling.wearto.wear.common.design.theme.DefaultSpace
import se.yverling.wearto.wear.common.design.theme.SmallSpace
import se.yverling.wearto.wear.common.design.theme.WearToTheme
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState
import se.yverling.wearto.wear.feature.items.R
import se.yverling.wearto.wear.feature.items.model.ItemUiModel
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.UiState.Loading
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.UiState.Success
import se.yverling.wearto.wear.feature.items.ui.theme.AddIconSize

const val ItemsRoute = "ItemsRoute"

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ItemsScreen(
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier,
    viewModel: ItemsViewModel = hiltViewModel(),
    onAddItem: (Item) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        Loading -> LoadingScreen()

        is Success -> {
            val items = (uiState as Success).items

            if (items.isEmpty()) {
                EmptyScreen()
            } else {
                ItemsList(items, columnState, modifier) { item ->
                    viewModel.setItemStateToLoading(item)
                    onAddItem(item)
                }
            }
        }
    }
}

private const val COLOR_ANIMATION_DURATION_IN_MILLIS = 200

@Composable
@OptIn(ExperimentalHorologistApi::class)
private fun ItemsList(
    items: List<ItemUiModel>,
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier,
    onAddItem: (Item) -> Unit,
) {
    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        columnState = columnState,
    ) {
        items(items) { itemUi ->
            val animationTargetValue = when (itemUi.state) {
                ItemState.Init, ItemState.Loading -> MaterialTheme.colorScheme.onSurface
                ItemState.Successful -> MaterialTheme.colorScheme.primary
                ItemState.Error -> MaterialTheme.colorScheme.error
            }

            val itemStateColor by animateColorAsState(
                animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_IN_MILLIS),
                targetValue = animationTargetValue,
                label = "itemStateColor",
            )

            Item(itemUi, itemStateColor, onAddItem)
        }
    }
}

@Composable
@OptIn(ExperimentalHorologistApi::class)
private fun Item(
    itemUi: ItemUiModel,
    itemStateColor: Color,
    onAddItem: (Item) -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = itemUi.item.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                ),
            )
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = itemStateColor,
            iconColor = itemStateColor,
        ),
        icon = {
            Box(modifier = Modifier.padding(end = SmallSpace)) {
                if (itemUi.state == ItemState.Loading) {
                    ChipIconWithProgress(
                        progressIndicatorColor = MaterialTheme.colorScheme.primary,
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
        onClick = { onAddItem(itemUi.item) }
    )
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(AddIconSize),
            imageVector = Icons.Outlined.LibraryAdd,
            contentDescription = stringResource(R.string.add_icon_description)
        )

        Text(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = DefaultSpace),
            text = stringResource(R.string.empty_state_description),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalHorologistApi::class)
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
                    ItemUiModel(Item(name = "Milk")),
                    ItemUiModel(Item(name = "Paper")),
                    ItemUiModel(Item(name = "Flour")),
                ),
                columnState = rememberResponsiveColumnState(),
            ) {}
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    locale = "sv",
)
@Composable
fun HyphenationPreview() {
    WearToTheme {
        Surface {
            ItemsList(
                items = listOf(
                    ItemUiModel(Item(name = "Matlagningsgrädde")),
                    ItemUiModel(Item(name = "Hushållspapper")),
                    ItemUiModel(Item(name = "Diskmaskinstabletter")),
                    ItemUiModel(Item(name = "Kolsyrepatroner")),
                ),
                columnState = rememberResponsiveColumnState(),
            ) {}
        }
    }
}

@Preview(
    name = "Light Mode"
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ListItemPreview() {
    WearToTheme {
        Surface {
            Item(
                itemUi = ItemUiModel(
                    item = Item(name = "Item"),
                    state = ItemState.Successful,
                ),
                itemStateColor = MaterialTheme.colorScheme.primary,
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
