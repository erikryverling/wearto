package se.yverling.wearto.mobile.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
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
import se.yverling.wearto.mobile.feature.login.R as LoginR

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearToTheme {
                val navController = rememberNavController()

                val navigationItems = listOf(
                    NavigationItem.Items,
                    NavigationItem.Settings,
                )

                var showBottomBar by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigation(
                                navController = navController,
                                items = navigationItems
                            )
                        }
                    }
                ) { padding ->
                    NavHost(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        startDestination = ItemsRoute
                    ) {
                        composable<LoginRoute> { backStackEntry ->
                            showBottomBar = false

                            val route: LoginRoute = backStackEntry.toRoute()

                            LoginScreen(
                                errorMessage = route.errorMessage,

                                onOpenUrl = { openTodoist() },

                                onLogin = {
                                    navController.popBackStack()
                                    navController.navigate(SettingsRoute)
                                }
                            )
                        }

                        composable(ItemsRoute) {
                            showBottomBar = true

                            ItemsScreen {
                                navController.navigate(LoginRoute(errorMessage = null))
                            }
                        }

                        composable(SettingsRoute) {
                            showBottomBar = true

                            SettingsScreen { logoutReason ->
                                val loginError = when (logoutReason) {
                                    Unauthorized -> LoginR.string.login_error
                                    RequestedByUser, NoToken -> null
                                }

                                navController.navigate(LoginRoute(loginError))
                            }
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
