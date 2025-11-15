package se.yverling.wearto.mobile.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import se.yverling.wearto.common.ui.LoadingScreen
import se.yverling.wearto.mobile.common.design.theme.DefaultSpace
import se.yverling.wearto.mobile.common.design.theme.LargeSpace
import se.yverling.wearto.mobile.common.design.theme.MaxWith
import se.yverling.wearto.mobile.common.design.theme.SmallSpace
import se.yverling.wearto.mobile.common.design.theme.VeryLargeSpace
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.feature.settings.R
import se.yverling.wearto.mobile.feature.settings.theme.DropDownMenuHeightInPercent
import se.yverling.wearto.mobile.feature.settings.theme.DropDownMenuWidthInPercent
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.NoToken
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.RequestedByUser
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.Unauthorized
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectUiState.Loading
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectUiState.Success
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectsUiState
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectsUiState.LoggedOut

const val SettingsRoute = "SettingsRoute"

enum class LogoutReason {
    NoToken,
    Unauthorized,
    RequestedByUser,
}

@Composable
fun SettingsScreen(
    onLoggedOut: (LogoutReason) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()

    val projectState by viewModel.projectState.collectAsState()
    val projectsState by viewModel.projectsState.collectAsState()

    when (projectState) {
        Loading -> LoadingScreen()

        is Success -> {
            val projects by remember {
                derivedStateOf {
                    if (projectsState is ProjectsUiState.Success) {
                        (projectsState as ProjectsUiState.Success).projects
                    } else {
                        emptyList()
                    }
                }
            }

            val successState = (projectState as Success)

            MainContent(
                project = successState.project,
                projects = projects,
                versionName = successState.versionName,
                onProjectSelected = {
                    scope.launch {
                        viewModel.setProject(it)
                    }
                },
                onLogout = {
                    scope.launch {
                        viewModel.logout()
                        onLoggedOut(RequestedByUser)
                    }
                },
                onImport = onImport,
                onExport = onExport,
                modifier = modifier,
            )
        }
    }

    when (projectsState) {
        is LoggedOut -> {
            LaunchedEffect(projectsState) {
                viewModel.logout()

                val loggedOutState = projectsState as LoggedOut
                if (!loggedOutState.hasToken) onLoggedOut(NoToken)
                else onLoggedOut(Unauthorized)
            }
        }

        else -> {
            // Ignore
        }
    }
}

@Composable
private fun MainContent(
    project: String?,
    projects: List<Project>,
    versionName: String,
    onProjectSelected: (Project) -> Unit,
    onLogout: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = modifier
                .widthIn(max = MaxWith)
                .padding(
                    start = LargeSpace,
                    end = LargeSpace,
                    top = VeryLargeSpace,
                    bottom = LargeSpace
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.setting_title), style = MaterialTheme.typography.titleLarge)

            var dropDownExpanded by remember { mutableStateOf(false) }

            DropDownMenu(
                selectedProject = project,
                projects = projects,
                dropDownExpanded = dropDownExpanded,

                onShowMenu = {
                    dropDownExpanded = true
                },

                onDismissMenu = {
                    dropDownExpanded = false
                },

                onItemClick = { project ->
                    onProjectSelected(project)
                    dropDownExpanded = false
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            var showImportDialog by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }

            ImportExportRow(
                onImport = { showImportDialog = true },
                onExport = onExport
            )

            LogoutButton { showLogoutDialog = true }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${stringResource(R.string.version_prefix)} $versionName",
                style = MaterialTheme.typography.labelSmall,
            )

            if (showImportDialog) {
                WarningDialog(
                    text = stringResource(R.string.import_dialog_text),
                    confirmTitle = stringResource(R.string.import_dialog_confirm),
                    onDismissRequest = { showImportDialog = false },
                    onConfirmation = {
                        showImportDialog = false
                        onImport()
                    }
                )
            }

            if (showLogoutDialog) {
                WarningDialog(
                    text = stringResource(R.string.logout_dialog_text),
                    confirmTitle = stringResource(R.string.logout_dialog_confirm),
                    onDismissRequest = { showLogoutDialog = false },
                    onConfirmation = {
                        showLogoutDialog = false
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun ImportExportRow(onImport: () -> Unit, onExport: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = DefaultSpace)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onClick = onImport
        ) {
            Icon(
                modifier = Modifier.padding(end = DefaultSpace),
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = stringResource(R.string.import_button_icon_content_description),
            )
            Text(stringResource(R.string.import_button_label))
        }

        Spacer(Modifier.padding(SmallSpace))

        FilledTonalButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onClick = onExport
        ) {
            Icon(
                modifier = Modifier.padding(end = DefaultSpace),
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = stringResource(R.string.export_button_icon_content_description),
            )
            Text(stringResource(R.string.export_button_label))
        }
    }
}

@Composable
private fun DropDownMenu(
    selectedProject: String?,
    projects: List<Project>,
    dropDownExpanded: Boolean,
    onShowMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onItemClick: (Project) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = DefaultSpace)
            .wrapContentSize(Alignment.TopStart)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onShowMenu() }
        ) {
            Text(selectedProject ?: stringResource(R.string.drop_down_button_empty_state_description))

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.drop_down_content_description),
            )
        }

        DropdownMenu(
            modifier = Modifier
                .fillMaxHeight(DropDownMenuHeightInPercent)
                .fillMaxWidth(DropDownMenuWidthInPercent),
            expanded = dropDownExpanded,
            onDismissRequest = { onDismissMenu() }
        ) {
            if (projects.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                projects.map { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = { onItemClick(project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onLogout
    ) {
        Icon(
            modifier = Modifier.padding(end = DefaultSpace),
            imageVector = Icons.AutoMirrored.Default.Logout,
            contentDescription = stringResource(R.string.logout_button_icon_content_description),
        )
        Text(stringResource(R.string.logout_button_label))
    }
}

@Composable
fun WarningDialog(
    text: String,
    confirmTitle: String,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = stringResource(R.string.warning_dialog_icon_content_description)
            )
        },
        title = { Text(text = stringResource(R.string.warning_dialog_title)) },
        text = { Text(text) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(confirmTitle)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.warning_dialog_dismiss))
            }
        }
    )
}

@Preview(
    name = "Light Mode",
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MainContentPreview() {
    WearToTheme {
        Surface {
            MainContent(
                project = "Livsmedel",
                projects = emptyList(),
                versionName = "1.0.0",
                onProjectSelected = {},
                onLogout = {},
                onImport = {},
                onExport = {},
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
private fun DropDrownMenuPreview() {
    WearToTheme {
        Surface {
            DropDownMenu(
                selectedProject = "Livsmedel",
                projects = emptyList(),
                dropDownExpanded = false,
                onShowMenu = {},
                onDismissMenu = {},
                onItemClick = {}
            )
        }
    }
}

@Preview(
    name = "Light Mode",
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ImportExportPreview() {
    WearToTheme {
        ImportExportRow(
            onExport = {},
            onImport = {}
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
private fun LogoutButtonPreview() {
    WearToTheme {
        Surface {
            LogoutButton { }
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
private fun WarningDialogPreview() {
    WearToTheme {
        WarningDialog(
            text = stringResource(R.string.logout_dialog_text),
            confirmTitle = stringResource(R.string.logout_dialog_confirm),
            onDismissRequest = {},
            onConfirmation = {},
        )    }
}
