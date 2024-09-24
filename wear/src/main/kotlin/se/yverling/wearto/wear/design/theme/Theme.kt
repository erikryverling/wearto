package se.yverling.wearto.wear.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun WearToTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        content = content
    )
}
