package cl.cristian.bitacoraobd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8CAD8),
    onPrimary = PrototypeDarkBackground,
    primaryContainer = PrototypePrimary,
    onPrimaryContainer = PrototypeSurface,
    secondary = Color(0xFFC1D6C9),
    onSecondary = PrototypeDarkBackground,
    secondaryContainer = Color(0xFF34483D),
    onSecondaryContainer = PrototypeDarkOnSurface,
    tertiary = Color(0xFFE4C98E),
    onTertiary = PrototypeDarkBackground,
    tertiaryContainer = Color(0xFF594719),
    onTertiaryContainer = PrototypeDarkOnSurface,
    background = PrototypeDarkBackground,
    onBackground = PrototypeDarkOnSurface,
    surface = PrototypeDarkSurface,
    onSurface = PrototypeDarkOnSurface,
    surfaceVariant = PrototypeDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC9CBD0),
    surfaceTint = Color(0xFFB8CAD8),
    outline = Color(0xFF90949B),
    outlineVariant = Color(0xFF555960),
    inverseSurface = PrototypeDarkOnSurface,
    inverseOnSurface = PrototypeDarkBackground,
    inversePrimary = PrototypePrimary,
    surfaceContainerLowest = PrototypeDarkBackground,
    surfaceContainerLow = PrototypeDarkSurface,
    surfaceContainer = PrototypeDarkSurface,
    surfaceContainerHigh = PrototypeDarkSurfaceVariant,
    surfaceContainerHighest = PrototypeDarkSurfaceVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = PrototypePrimary,
    onPrimary = PrototypeSurface,
    primaryContainer = PrototypePrimarySoft,
    onPrimaryContainer = PrototypeInk,
    secondary = PrototypeGood,
    onSecondary = PrototypeSurface,
    secondaryContainer = PrototypeGoodSoft,
    onSecondaryContainer = PrototypeInk,
    tertiary = PrototypeWarn,
    onTertiary = PrototypeSurface,
    tertiaryContainer = PrototypeWarnSoft,
    onTertiaryContainer = PrototypeInk,
    background = PrototypeBackground,
    onBackground = PrototypeInk,
    surface = PrototypeSurface,
    onSurface = PrototypeInk,
    surfaceVariant = PrototypeSurface2,
    onSurfaceVariant = PrototypeMuted,
    surfaceTint = PrototypePrimary,
    outline = PrototypeMuted,
    outlineVariant = PrototypeLine,
    inverseSurface = PrototypeInk,
    inverseOnSurface = PrototypeSurface,
    inversePrimary = Color(0xFFB8CAD8),
    surfaceContainerLowest = PrototypeSurface,
    surfaceContainerLow = PrototypeSurface2,
    surfaceContainer = PrototypeSurface2,
    surfaceContainerHigh = PrototypeSurface3,
    surfaceContainerHighest = PrototypeSurface3,
    error = PrototypeBad,
    onError = PrototypeSurface,
    errorContainer = PrototypeBadSoft,
    onErrorContainer = PrototypeInk
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
