package se.yverling.wearto.mobile.app.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
internal fun BottomNavigation(
    items: List<NavigationItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                label = { Text(stringResource(item.title)) },
                icon = {
                    Icon(
                        if (selected) item.selectedIcon else item.icon,
                        contentDescription = stringResource(item.iconContentDescription)
                    )
                },
                selected = selected,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}
