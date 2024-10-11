package se.yverling.wearto.mobile.ui

import se.yverling.wearto.mobile.feature.login.ui.LoginRoute

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(LoginRoute)
}
