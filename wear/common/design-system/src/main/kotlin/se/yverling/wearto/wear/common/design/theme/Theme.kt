package se.yverling.wearto.wear.common.design.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

internal val wearColorPalette = ColorScheme(
    primary = md_theme_dark_primary,
    // primaryDim
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimary = md_theme_dark_onPrimary,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    // secondaryDim
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    // tertiaryDim
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    onTertiaryContainer = md_theme_dark_tertiaryContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHighest,
    onSurface = md_theme_dark_onSurface,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
)

@Composable
fun WearToTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        content = content,
        colorScheme = wearColorPalette,
    )
}
