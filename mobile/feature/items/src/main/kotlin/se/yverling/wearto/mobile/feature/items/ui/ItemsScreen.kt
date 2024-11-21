package se.yverling.wearto.mobile.feature.items.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.Sentences
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import se.yverling.wearto.common.ui.LoadingScreen
import se.yverling.wearto.mobile.common.design.theme.DefaultSpace
import se.yverling.wearto.mobile.common.design.theme.LargeSpace
import se.yverling.wearto.mobile.common.design.theme.MaxWith
import se.yverling.wearto.mobile.common.design.theme.SmallSpace
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.feature.items.R
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.Loading
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.LoggedOut
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.Success
import theme.AddItemSize
import theme.IconAnimationDurationInMillis
import theme.IconAnimationRotation
import theme.ItemCardElevation
import timber.log.Timber

const val ItemsRoute = "ItemsRoute"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onLoggedOut: () -> Unit,
    onSync: () -> Unit,
    viewModel: ItemsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        Loading -> LoadingScreen()

        is LoggedOut -> onLoggedOut()

        is Success -> {
            val scope = rememberCoroutineScope()

            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    skipHiddenState = false,
                    initialValue = SheetValue.Hidden
                )
            )

            var selectedItem by remember { mutableStateOf<Item?>(null) }
            var nameInputValue by remember { mutableStateOf("") }
            var isError: Boolean by remember { mutableStateOf(false) }

            val keyboardController = LocalSoftwareKeyboardController.current
            val focusRequester = remember { FocusRequester() }

            BottomSheetScaffold(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            scope.launch {
                                if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                    keyboardController?.hide()
                                }
                            }
                        })
                    },

                scaffoldState = bottomSheetScaffoldState,

                sheetContent = {

                    BottomSheet(
                        inputValue = nameInputValue,
                        isError = isError,
                        canDelete = selectedItem != null,
                        focusRequester = focusRequester,

                        onSave = {
                            scope.launch {
                                val newItem = if (selectedItem == null) Item(name = nameInputValue)
                                else selectedItem!!.copy(name = nameInputValue)

                                try {
                                    viewModel.setItem(newItem)
                                    keyboardController?.hide()
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                } catch (e: Exception) {
                                    Timber.e(e)
                                    isError = true
                                }
                            }
                        },

                        onInputValidationError = {
                            isError = true
                        },

                        onDelete = {
                            scope.launch {
                                selectedItem?.let { viewModel.deleteItem(it) }

                                keyboardController?.hide()
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        },

                        onInputValueChanged = { newValue ->
                            nameInputValue = newValue
                        },

                        onClearInputField = {
                            nameInputValue = ""
                        },
                    )
                }
            ) { padding ->
                val currentValue = bottomSheetScaffoldState.bottomSheetState.currentValue
                LaunchedEffect(currentValue) {
                    if (currentValue == SheetValue.Hidden) {
                        isError = false
                    }
                }

                Scaffold(
                    topBar = {

                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),

                            title = {
                                Text(stringResource(R.string.items_title))
                            },

                            actions = {
                                var rotationValue by remember { mutableFloatStateOf(0f) }

                                IconButton(onClick = {
                                    rotationValue -= IconAnimationRotation
                                    onSync()
                                }) {
                                    val angle by animateFloatAsState(
                                        targetValue = rotationValue,
                                        animationSpec = tween(
                                            easing = FastOutSlowInEasing,
                                            durationMillis = IconAnimationDurationInMillis,
                                        ), label = "syncIconRotation"
                                    )

                                    Icon(
                                        modifier = Modifier.rotate(angle),
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = stringResource(R.string.sync_action_description)
                                    )
                                }
                            }
                        )
                    },

                    floatingActionButton = {
                        AddFab {
                            scope.launch {
                                selectedItem = null
                                nameInputValue = ""

                                bottomSheetScaffoldState.bottomSheetState.expand()
                                keyboardController?.show()
                                focusRequester.requestFocus()
                            }
                        }
                    }
                ) { padding ->
                    val successState = uiState as Success
                    val items = successState.items

                    if (items.isEmpty()) {
                        EmptyScreen(modifier = Modifier.padding(padding))
                    } else {
                        ItemsList(
                            items = items,
                            modifier = Modifier.padding(padding)
                        ) { item ->
                            scope.launch {
                                isError = false

                                selectedItem = item
                                nameInputValue = item.name

                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheet(
    inputValue: String,
    isError: Boolean,
    canDelete: Boolean,
    focusRequester: FocusRequester,
    onSave: () -> Unit,
    onInputValidationError: () -> Unit,
    onDelete: () -> Unit,
    onInputValueChanged: (String) -> Unit,
    onClearInputField: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(
            bottom = LargeSpace,
            start = LargeSpace,
            end = LargeSpace
        )
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(bottom = DefaultSpace),
            value = inputValue,
            isError = isError,
            keyboardOptions = KeyboardOptions(capitalization = Sentences),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (inputValue.isBlank()) onInputValidationError()
                    else onSave()
                }
            ),
            onValueChange = { onInputValueChanged(it) },
            label = { Text(stringResource(R.string.item_input_field_label)) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { onClearInputField() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_button_description)
                    )
                }
            }
        )

        Row {
            DeleteButton(
                modifier = Modifier.weight(1f),
                enabled = canDelete
            ) {
                onDelete()
            }

            Spacer(Modifier.width(LargeSpace))

            SaveButton(modifier = Modifier.weight(1f)) {
                if (inputValue.isBlank()) onInputValidationError()
                else onSave()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemsList(
    items: List<Item>,
    modifier: Modifier = Modifier,
    onItemClicked: (Item) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(DefaultSpace)
                .widthIn(max = MaxWith)
        ) {
            itemsIndexed(items) { index, item ->
                Item(item) {
                    onItemClicked(item)
                }

                Spacer(modifier = Modifier.padding(SmallSpace))
            }
        }
    }
}

@Composable
internal fun Item(
    item: Item,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = ItemCardElevation,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick.invoke() }
    ) {
        Row(
            Modifier.padding(DefaultSpace),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(end = DefaultSpace),
                imageVector = Icons.Default.AddTask,
                contentDescription = stringResource(R.string.edit_item_description)
            )

            Text(
                modifier = Modifier.animateContentSize(),
                text = item.name,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
internal fun AddFab(onClicked: () -> Unit) {
    return FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = onClicked
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = stringResource(R.string.add_button_description)
        )
    }
}

@Composable
internal fun SaveButton(
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = { onSave() },
    ) {
        Text(text = stringResource(R.string.save_button_label))
    }
}

@Composable
internal fun DeleteButton(
    enabled: Boolean = false,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onDelete() },
        border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        Text(text = stringResource(R.string.delete_button_label))
    }
}

@Composable
internal fun EmptyScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(AddItemSize),
            imageVector = Icons.Outlined.LibraryAdd,
            contentDescription = stringResource(R.string.add_icon_description)
        )
        Text(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = DefaultSpace),
            text = stringResource(R.string.empty_state_description),
            style = MaterialTheme.typography.titleLarge
        )
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
private fun BottomSheetPreview() {
    WearToTheme {
        Surface {
            BottomSheet(
                inputValue = "Test",
                isError = false,
                canDelete = true,
                focusRequester = remember { FocusRequester() },
                onSave = {},
                onInputValidationError = {},
                onDelete = {},
                onInputValueChanged = {},
                onClearInputField = {},
            )
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
private fun AddFabPreview() {
    WearToTheme {
        AddFab { }
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
private fun ItemsListPreview() {
    WearToTheme {
        ItemsList(
            items = listOf(
                Item(1, "Item 1"),
                Item(2, "Item 1"),
                Item(3, "Item 1"),
            ),
        )
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
private fun DeleteButtonPreview() {
    WearToTheme {
        DeleteButton(onDelete = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Light Mode"
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SaveButtonPreview() {
    WearToTheme {
        SaveButton {}
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
private fun EmptyScreenPreview() {
    WearToTheme {
        EmptyScreen()
    }
}
