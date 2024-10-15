package se.yverling.wearto.mobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import se.yverling.wearto.mobile.app.R
import se.yverling.wearto.mobile.feature.login.R as LoginR
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import se.yverling.wearto.mobile.feature.login.ui.LoginRoute
import se.yverling.wearto.mobile.feature.login.ui.LoginScreen
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.NoToken
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.RequestedByUser
import se.yverling.wearto.mobile.feature.settings.ui.LogoutReason.Unauthorized
import se.yverling.wearto.mobile.feature.settings.ui.SettingsRoute
import se.yverling.wearto.mobile.feature.settings.ui.SettingsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearToTheme {
                val navController = rememberNavController()

                Scaffold { padding ->
                    NavHost(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        startDestination = SettingsRoute
                    ) {
                        composable<LoginRoute> { backStackEntry ->
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

                        composable<SettingsRoute> {
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
