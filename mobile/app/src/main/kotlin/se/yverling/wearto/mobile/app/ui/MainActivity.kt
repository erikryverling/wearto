package se.yverling.wearto.mobile.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.getValue
import se.yverling.wearto.mobile.feature.login.R as LoginR

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigation(
                                items = navigationItems,
                                navController = navController,
                            )
                        }
                    }
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openTodoist() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.todoist_link)))
        ContextCompat.startActivity(this@MainActivity, intent, null)
    }
}
