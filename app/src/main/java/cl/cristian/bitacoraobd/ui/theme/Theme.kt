package cl.cristian.bitacoraobd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ObdGreenBright,
    onPrimary = Graphite,
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFF8AF8BD),
    secondary = ObdGreenBright,
    onSecondary = Graphite,
    secondaryContainer = GraphiteSurface,
    onSecondaryContainer = ObdOnDark,
    tertiary = ObdGreenBright,
    onTertiary = Graphite,
    tertiaryContainer = GraphiteSurface,
    onTertiaryContainer = ObdOnDark,
    background = Graphite,
    onBackground = ObdOnDark,
    surface = GraphiteSurface,
    onSurface = ObdOnDark,
    surfaceVariant = Color(0xFF303A37),
    onSurfaceVariant = Color(0xFFC3CCC7),
    surfaceTint = ObdGreenBright,
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF434D49),
    inverseSurface = ObdOnDark,
    inverseOnSurface = Graphite,
    inversePrimary = ObdGreen,
    surfaceContainerLowest = GraphiteSurface,
    surfaceContainerLow = GraphiteSurface,
    surfaceContainer = GraphiteSurface,
    surfaceContainerHigh = GraphiteSurface,
    surfaceContainerHighest = GraphiteSurface,
    error = ObdErrorDark,
    onError = Graphite,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = ObdGreen,
    onPrimary = ObdOnGreen,
    primaryContainer = Color(0xFF8AF8BD),
    onPrimaryContainer = Color(0xFF002114),
    secondary = ObdGreen,
    onSecondary = ObdOnGreen,
    secondaryContainer = Color(0xFFB5F2D0),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = ObdGreen,
    onTertiary = ObdOnGreen,
    tertiaryContainer = Color(0xFFB5F2D0),
    onTertiaryContainer = Color(0xFF002114),
    background = LightBackground,
    onBackground = Graphite,
    surface = LightSurface,
    onSurface = Graphite,
    surfaceVariant = Color(0xFFDCE5E0),
    onSurfaceVariant = Color(0xFF404A45),
    surfaceTint = ObdGreen,
    outline = Color(0xFF707A74),
    outlineVariant = Color(0xFFC0C9C3),
    inverseSurface = Graphite,
    inverseOnSurface = ObdOnDark,
    inversePrimary = ObdGreenBright,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurface,
    error = ObdErrorLight,
    onError = ObdOnGreen,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun BitacoraOBDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
