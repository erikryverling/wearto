package se.yverling.wearto.mobile.feature.items.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.common.design.theme.DefaultSpace
import se.yverling.wearto.mobile.common.design.theme.LargeSpace
import se.yverling.wearto.mobile.common.design.theme.SmallSpace
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.common.ui.LoadingScreen
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.feature.items.R
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.*
import theme.AddItemSize
import theme.ItemCardElevation
import timber.log.Timber

const val ItemsRoute = "ItemsRoute"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    viewModel: ItemsViewModel = hiltViewModel(),
    onLoggedOut: () -> Unit,
    onSync: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        UiState.Loading -> LoadingScreen()

        is UiState.LoggedOut -> onLoggedOut()

        is UiState.Success -> {
            val successState = uiState as UiState.Success

            val scope = rememberCoroutineScope()

            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    skipHiddenState = false,
                    initialValue = SheetValue.Hidden
                )
            )

            var selectedItem by remember { mutableStateOf<Item?>(null) }
            var nameInput by remember { mutableStateOf("") }
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
                    Column(
                        modifier = Modifier.padding(
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
                            value = nameInput,
                            isError = isError,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (nameInput.isBlank()) {
                                        isError = true
                                    } else {
                                        scope.launch {
                                            val newItem = if (selectedItem == null) Item(name = nameInput)
                                            else selectedItem!!.copy(name = nameInput)

                                            try {
                                                viewModel.setItem(newItem)
                                                keyboardController?.hide()
                                                bottomSheetScaffoldState.bottomSheetState.hide()
                                            } catch (e: Exception) {
                                                Timber.e(e)
                                                isError = true
                                            }
                                        }
                                    }
                                }
                            ),
                            onValueChange = { nameInput = it },
                            label = { Text(stringResource(R.string.item_input_field_label)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { nameInput = "" }) {
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
                                enabled = selectedItem != null,
                            ) {
                                scope.launch {
                                    selectedItem?.let { viewModel.deleteItem(it) }

                                    keyboardController?.hide()
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            }

                            Spacer(Modifier.width(LargeSpace))

                            SaveButton(modifier = Modifier.weight(1f)) {
                                if (nameInput.isBlank()) {
                                    isError = true
                                } else {
                                    scope.launch {
                                        val newItem = if (selectedItem == null) Item(name = nameInput)
                                        else selectedItem!!.copy(name = nameInput)

                                        try {
                                            viewModel.setItem(newItem)
                                            keyboardController?.hide()
                                            bottomSheetScaffoldState.bottomSheetState.hide()
                                        } catch (e: Exception) {
                                            Timber.e(e)
                                            isError = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
                    if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
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
                                Text(stringResource(R.string.title))
                            },

                            actions = {
                                IconButton(onClick = { onSync() }) {
                                    Icon(
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
                                nameInput = ""

                                bottomSheetScaffoldState.bottomSheetState.expand()
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                    }
                ) { padding ->
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
                                nameInput = item.name

                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    }


                }
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
    LazyColumn(modifier = modifier.padding(DefaultSpace)) {
        itemsIndexed(items) { index, item ->
            Item(item) {
                onItemClicked(item)
            }

            Spacer(modifier = Modifier.padding(SmallSpace))
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
            .clickable {
                onClick.invoke()
            }
    ) {
        Row(
            Modifier.padding(DefaultSpace),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.AddTask,
                modifier = Modifier.padding(end = DefaultSpace),
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
private fun EmptyScreenPreview() {
    WearToTheme {
        EmptyScreen()
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
