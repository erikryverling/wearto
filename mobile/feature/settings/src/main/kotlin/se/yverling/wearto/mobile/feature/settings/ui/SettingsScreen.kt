package se.yverling.wearto.mobile.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import se.yverling.wearto.mobile.common.design.theme.DefaultSpace
import se.yverling.wearto.mobile.common.design.theme.LargeSpace
import se.yverling.wearto.mobile.common.design.theme.VeryLargeSpace
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.feature.settings.R
import se.yverling.wearto.mobile.feature.settings.theme.DropDownMenuHeightInPercent
import se.yverling.wearto.mobile.feature.settings.theme.DropDownMenuWidthInPercent
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.NoToken
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.RequestedByUser
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.Unauthorized
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.Error
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.Loading
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.LoggedOut
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.Success

@Serializable
object SettingsRoute

enum class LogoutReason {
    NoToken,
    Unauthorized,
    RequestedByUser,
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onLoggedOut: (LogoutReason) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    when (uiState) {
        Loading -> {
            // Ignore
        }

        is Success -> {
            val successState = (uiState as Success)

            MainContent(
                project = successState.project,
                projects = successState.projects,
                onLogout = {
                    scope.launch {
                        viewModel.logout()
                        onLoggedOut(RequestedByUser)
                    }
                },
                onProjectSelected = { viewModel.setProject(it) },
                modifier = modifier,
            )
        }

        is LoggedOut -> {
            LaunchedEffect(uiState) {
                viewModel.logout()

                val loggedOutState = uiState as LoggedOut
                if (!loggedOutState.hasToken) onLoggedOut(NoToken)
                else onLoggedOut(Unauthorized)
            }
        }

        Error -> {
            // Ignore
        }
    }
}

@Composable
private fun MainContent(
    project: String?,
    projects: List<Project>,
    onLogout: () -> Unit,
    onProjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = LargeSpace,
                end = LargeSpace,
                top = VeryLargeSpace,
                bottom = LargeSpace
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Todoist project to sync with", style = MaterialTheme.typography.titleLarge)

        var dropDownExpanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .padding(top = DefaultSpace)
                .wrapContentSize(Alignment.TopStart)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { dropDownExpanded = !dropDownExpanded }
            ) {
                Text(project ?: stringResource(R.string.drop_down_button_empty_state_description))
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
                onDismissRequest = { dropDownExpanded = false }
            ) {
                projects.map { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                            onProjectSelected(project.name)
                            dropDownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        var showDialog by remember { mutableStateOf(false) }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showDialog = true
            }
        ) {
            Icon(
                modifier = Modifier.padding(end = DefaultSpace),
                imageVector = Icons.AutoMirrored.Default.Logout,
                contentDescription = stringResource(R.string.logout_button_icon_content_description),
            )
            Text("Logout")
        }

        if (showDialog) {
            LogoutDialog(
                onDismissRequest = { showDialog = false },
                onConfirmation = {
                    showDialog = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
fun LogoutDialog(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = stringResource(R.string.logout_dialog_icon_content_description)
            )
        },
        title = { Text(text = stringResource(R.string.logout_dialog_title)) },
        text = { Text(text = stringResource(R.string.logout_dialog_text)) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(stringResource(R.string.logout_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.logout_dialog_dismiss))
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
                onProjectSelected = {},
                onLogout = {}
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
private fun LogoutDialogPreview() {
    LogoutDialog(
        onDismissRequest = {},
        onConfirmation = {},
    )
}
