package se.yverling.wearto.mobile.feature.login.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import se.yverling.wearto.mobile.common.design.theme.DefaultSpace
import se.yverling.wearto.mobile.common.design.theme.LargeSpace
import se.yverling.wearto.mobile.common.design.theme.MaxWith
import se.yverling.wearto.mobile.common.design.theme.VeryLargeSpace
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.feature.login.R
import se.yverling.wearto.mobile.feature.login.theme.ItemIconSize
import se.yverling.wearto.mobile.feature.login.theme.ItemOneDescriptionStartPadding
import se.yverling.wearto.mobile.feature.login.theme.ItemOneTopPadding
import se.yverling.wearto.mobile.feature.login.theme.ItemTwoTopPadding

@Serializable
data class LoginRoute(@StringRes val errorMessage: Int? = null)

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    errorMessage: Int? = null,
) {
    var errorMessage by remember { mutableStateOf(errorMessage) }

    val scope = rememberCoroutineScope()

    MainContent(
        onLogin = { token ->
            scope.launch {
                if (token.isBlank()) {
                    errorMessage = R.string.login_error
                } else {
                    scope.launch {
                        viewModel.setToken(token)
                        onLogin()
                    }
                }
            }
        },
        onOpenUrl = onOpenUrl,
        modifier = modifier,
        errorMessage = errorMessage,
    )
}

@Composable
fun MainContent(
    onLogin: (String) -> Unit,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: Int? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .widthIn(max = MaxWith)
                .fillMaxHeight()
                .padding(
                    horizontal = LargeSpace,
                    vertical = VeryLargeSpace
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = VeryLargeSpace),
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.tertiary
            )

            ItemOne {
                onOpenUrl()
            }

            var apiTokenInputValue by remember { mutableStateOf("") }

            ItemTwo(apiTokenInputValue, errorMessage) { inputValue ->
                apiTokenInputValue = inputValue
            }

            LoginButton {
                onLogin(apiTokenInputValue)
            }
        }
    }
}

@Composable
private fun ItemOne(
    modifier: Modifier = Modifier,
    onOpenUrl: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = VeryLargeSpace),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            modifier = Modifier
                .padding(end = DefaultSpace, top = ItemOneTopPadding)
                .size(ItemIconSize),
            imageVector = Icons.Default.LooksOne,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = stringResource(R.string.item_one_icon_content_description),
        )

        Column {
            TextButton(onClick = onOpenUrl) {
                Text(
                    stringResource(R.string.todoist_link),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                modifier = Modifier.padding(start = ItemOneDescriptionStartPadding),
                text = stringResource(R.string.item_one_description),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ItemTwo(
    inputValue: String,
    errorMessage: Int?,
    onInputValueChanged: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = VeryLargeSpace),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            modifier = Modifier
                .padding(end = DefaultSpace, top = ItemTwoTopPadding)
                .size(ItemIconSize),
            imageVector = Icons.Default.LooksTwo,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = stringResource(R.string.item_one_icon_content_description),
        )

        var showPassword by remember { mutableStateOf(value = false) }
        val keyboardController = LocalSoftwareKeyboardController.current

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),

            isError = errorMessage != null,

            supportingText = {
                if (errorMessage != null) Text(stringResource(errorMessage))
                else Text(stringResource(R.string.input_field_supporting_text))
            },

            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),

            value = inputValue,
            singleLine = true,
            label = { Text(stringResource(R.string.login_input_field_label)) },

            onValueChange = { onInputValueChanged(it) },

            trailingIcon = {
                if (showPassword) {
                    IconButton(onClick = { showPassword = false }) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = stringResource(R.string.password_icon_visible_content_description)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showPassword = true }) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(R.string.password_icon_no_visible_content_description)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.padding(end = DefaultSpace),
            imageVector = Icons.AutoMirrored.Default.Login,
            contentDescription = stringResource(R.string.login_button_icon_content_description),
        )

        Text(
            text = stringResource(R.string.login_button_label),
            color = MaterialTheme.colorScheme.onPrimary
        )
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
private fun LoginScreenPreview() {
    WearToTheme {
        Surface {
            MainContent(
                errorMessage = R.string.login_error,
                onLogin = {},
                onOpenUrl = {},
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
private fun ItemOnePreview() {
    WearToTheme {
        Surface {
            ItemOne {}
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
private fun ItemTwoPreview() {
    WearToTheme {
        Surface {
            ItemTwo(
                inputValue = "",
                errorMessage = R.string.login_error,
                onInputValueChanged = {}
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
private fun LoginButtonPreview() {
    WearToTheme {
        LoginButton {}
    }
}
