package se.yverling.wearto.mobile.ui

import android.content.Intent
import android.net.Uri
import se.yverling.wearto.mobile.feature.login.LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.yverling.wearto.mobile.app.R
import se.yverling.wearto.mobile.common.design.theme.WearToTheme
import timber.log.Timber

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
                        startDestination = NavigationItem.Login.route
                    ) {
                        composable(NavigationItem.Login.route) {
                            LoginScreen(
                                onOpenUrl = {
                                    openTodoist()
                                },
                                onLogin = {
                                    Timber.tag("WearTo").d("Login")
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
