package dev.vskelk.cdf.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Vespa Theme
 *
 * Tema oscuro permanente - NO HAY LIGHT MODE.
 *
 * Per spec: "Modo oscuro permanente. Sin light mode."
 *
 * Esta es una regla absoluta del proyecto.
 */
private val VespaColorScheme = darkColorScheme(
    primary = VespaPrimary,
    onPrimary = VespaOnPrimary,
    primaryContainer = VespaSurfaceVariant,
    onPrimaryContainer = VespaOnSurface,

    secondary = VespaOnSurfaceMid,
    onSecondary = VespaBackground,
    secondaryContainer = VespaSurface,
    onSecondaryContainer = VespaOnSurface,

    tertiary = VespaWarning,
    onTertiary = VespaBackground,
    tertiaryContainer = VespaWarningContainer,
    onTertiaryContainer = VespaOnSurface,

    error = VespaError,
    onError = VespaBackground,
    errorContainer = VespaErrorContainer,
    onErrorContainer = VespaOnSurface,

    background = VespaBackground,
    onBackground = VespaOnSurface,

    surface = VespaSurface,
    onSurface = VespaOnSurface,
    surfaceVariant = VespaSurfaceVariant,
    onSurfaceVariant = VespaOnSurfaceMid,

    outline = VespaOutline,
    outlineVariant = VespaOnSurfaceLow,

    inverseSurface = VespaOnSurface,
    inverseOnSurface = VespaBackground,
    inversePrimary = VespaSurface
)

@Composable
fun VespaTheme(
    // Ignoramos el parámetro darkTheme - siempre oscuro
    // Esta es una decisión de diseño, no un fallback
    content: @Composable () -> Unit
) {
    val colorScheme = VespaColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Fondo negro para evitar flash
            window.statusBarColor = VespaBackground.toArgb()
            window.navigationBarColor = VespaBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VespaTypography,
        content = content
    )
}
