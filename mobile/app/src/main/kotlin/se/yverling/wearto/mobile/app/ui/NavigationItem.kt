package se.yverling.wearto.mobile.app.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import se.yverling.wearto.mobile.app.R
import se.yverling.wearto.mobile.feature.items.ui.ItemsRoute
import se.yverling.wearto.mobile.feature.settings.ui.SettingsRoute

internal sealed class NavigationItem(
    val route: String,
    @param:StringRes val title: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @param:StringRes val iconContentDescription: Int,
) {
    data object Items : NavigationItem(
        route = ItemsRoute,
        title = R.string.items_item_label,
        icon = Icons.Outlined.TaskAlt,
        selectedIcon = Icons.Filled.TaskAlt,
        iconContentDescription = R.string.items_item_icon_content_description,
    )

    data object Settings : NavigationItem(
        route = SettingsRoute,
        title = R.string.settings_item_label,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        iconContentDescription = R.string.settings_item_icon_content_description
    )
}
