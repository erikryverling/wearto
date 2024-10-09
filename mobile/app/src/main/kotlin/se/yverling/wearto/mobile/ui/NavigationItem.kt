package se.yverling.wearto.mobile.ui

import se.yverling.wearto.mobile.feature.login.LoginRoute

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(LoginRoute)
}
