package se.yverling.wearto.mobile.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.app.R
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.feature.items.ui.ItemsRoute
import se.yverling.wearto.mobile.feature.items.ui.ItemsScreen
import se.yverling.wearto.mobile.feature.login.ui.LoginRoute
import se.yverling.wearto.mobile.feature.login.ui.LoginScreen
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.NoToken
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.RequestedByUser
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.Unauthorized
import se.yverling.wearto.mobile.feature.settings.ui.SettingsRoute
import se.yverling.wearto.mobile.feature.settings.ui.SettingsScreen
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.getValue
import se.yverling.wearto.mobile.feature.login.R as LoginR
import androidx.core.net.toUri
import kotlinx.coroutines.flow.first
import se.yverling.wearto.mobile.app.ui.MainViewModel.*
import se.yverling.wearto.mobile.app.ui.MainViewModel.UiState
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val openFileLauncher = buildLauncher(R.string.import_error_message) { uri ->
        readTextFromFile(
            uri = uri,
            onError = { viewModel.showMessage(R.string.import_error_message) },
        )
    }

    private val createFileLauncher = buildLauncher(R.string.export_error_message) { uri ->
        lifecycleScope.launch {
            val csv = viewModel.getItemsAsCsv().first()
            saveTextToFile(
                uri = uri,
                text = csv,
                onError = { viewModel.showMessage(R.string.export_error_message) },
            )
        }
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearToTheme {
                val navController = rememberNavController()

                var showBottomBar by remember { mutableStateOf(false) }

                val navigationItems = listOf(
                    NavigationItem.Items,
                    NavigationItem.Settings,
                )

                val scope = rememberCoroutineScope()

                val snackbarHostState = remember { SnackbarHostState() }

                val state by viewModel.uiState.collectAsState()

                if (state is UiState.Message) {
                    LaunchedEffect(state) {
                        scope.launch {
                            val message = (state as UiState.Message).message
                            snackbarHostState.showSnackbar(getString(message))
                            viewModel.hideMessage()
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigation(
                                items = navigationItems,
                                navController = navController,
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { padding ->
                    NavHost(
                        modifier = Modifier
                            .padding(padding)
                            .consumeWindowInsets(padding),
                        navController = navController,
                        startDestination = ItemsRoute
                    ) {
                        composable<LoginRoute> { backStackEntry ->
                            showBottomBar = false

                            val route: LoginRoute = backStackEntry.toRoute()

                            LoginScreen(
                                errorMessage = route.errorMessage,

                                onLogin = {
                                    navController.popBackStack()
                                    navController.navigate(SettingsRoute)
                                },

                                onOpenUrl = { openTodoist() }
                            )
                        }

                        composable(ItemsRoute) {
                            showBottomBar = true

                            ItemsScreen(
                                onLoggedOut = {
                                    navController.navigate(LoginRoute(errorMessage = null))
                                },

                                onSync = {
                                    lifecycleScope.launch {
                                        viewModel.sendItems()
                                    }
                                }
                            )
                        }

                        composable(SettingsRoute) {
                            showBottomBar = true

                            SettingsScreen(
                                onLoggedOut = { logoutReason ->
                                    val loginError = when (logoutReason) {
                                        Unauthorized -> LoginR.string.login_error
                                        RequestedByUser, NoToken -> null
                                    }

                                    navController.navigate(LoginRoute(loginError))
                                },
                                onImport = { openFile() },
                                onExport = { createFile() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildLauncher(@StringRes errorMessage: Int, onSuccess: (Uri) -> Unit) =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data?.data?.let { uri -> onSuccess(uri) }

                if (data == null) {
                    Timber.e("Intent data was null")
                    viewModel.showMessage(errorMessage)
                }
            } else {
                Timber.e("Result code was not RESULT_OK")
                viewModel.showMessage(errorMessage)
            }
        }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }

        openFileLauncher.launch(intent)
    }

    private fun readTextFromFile(uri: Uri, onError: () -> Unit) {
        try {
            val inputStream = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val csv = reader.readText()
                    viewModel.replaceWithItemsFromCsv(csv)
                    viewModel.showMessage(R.string.import_success_message)
                }
            }
            if (inputStream == null) {
                Timber.e("Input steam is null")
                onError()
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not read file")
            onError()
        }
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, getString(R.string.backup_file_name))
        }

        createFileLauncher.launch(intent)
    }

    private fun saveTextToFile(uri: Uri, text: String, onError: () -> Unit) {
        try {
            val outputStream = contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(text.toByteArray())
                viewModel.showMessage(R.string.export_success_message)
            }
            if (outputStream == null) {
                Timber.e("Output steam is null")
                onError()
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not write file")
            onError()
        }
    }

    private fun openTodoist() {
        val intent = Intent(Intent.ACTION_VIEW, getString(R.string.todoist_link).toUri())
        ContextCompat.startActivity(this@MainActivity, intent, null)
    }
}
