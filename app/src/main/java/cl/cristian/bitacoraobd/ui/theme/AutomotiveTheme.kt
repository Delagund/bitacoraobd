package cl.cristian.bitacoraobd.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Figma Automotive / Colors, Dark mode; semantic aliases resolve to these primitives.
object AutomotiveColors {
    val Background = Color(0xFF0B1220)
    val Card = Color(0xFF172235)
    val Elevated = Color(0xFF223149)
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val Accent = Color(0xFF38BDF8)
    val Success = Color(0xFF34D399)
    val Warning = Color(0xFFFBBF24)
    val Error = Color(0xFFFB7185)
}

object AutomotiveDimensions {
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val RadiusSmall = 8.dp
    val RadiusMedium = 12.dp
    val RadiusLarge = 16.dp
    val RadiusExtraLarge = 24.dp
}

object AutomotiveText {
    // Android sans-serif resolves to the platform Roboto family; AUTO line heights remain unspecified.
    val DisplayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp)
    val TitleLarge = DisplayLarge.copy(fontSize = 22.sp, lineHeight = 28.sp)
    val TitleMedium = DisplayLarge.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp)
    val BodyMedium = DisplayLarge.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val LabelMedium = DisplayLarge.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    val VehicleTitle = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, letterSpacing = 0.sp)
    val VehicleCompactTitle = VehicleTitle.copy(fontSize = 18.sp)
    val CardTitle = VehicleTitle.copy(fontSize = 19.sp)
    val CardEyebrow = VehicleTitle.copy(fontSize = 11.sp)
    val CardDetail = VehicleTitle.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val CardSupporting = CardDetail.copy(fontSize = 13.sp)
    val ActionLabel = VehicleTitle.copy(fontSize = 14.sp)
    val ActionLargeLabel = VehicleTitle.copy(fontSize = 15.sp)
    val StatusLabel = VehicleTitle.copy(fontSize = 12.sp)
}

val AutomotiveDarkColorScheme = darkColorScheme(
    primary = AutomotiveColors.Accent,
    onPrimary = AutomotiveColors.Background,
    primaryContainer = AutomotiveColors.Elevated,
    onPrimaryContainer = AutomotiveColors.TextPrimary,
    secondary = AutomotiveColors.TextSecondary,
    onSecondary = AutomotiveColors.Background,
    secondaryContainer = AutomotiveColors.Elevated,
    onSecondaryContainer = AutomotiveColors.TextPrimary,
    tertiary = AutomotiveColors.Warning,
    onTertiary = AutomotiveColors.Background,
    tertiaryContainer = AutomotiveColors.Elevated,
    onTertiaryContainer = AutomotiveColors.TextPrimary,
    background = AutomotiveColors.Background,
    onBackground = AutomotiveColors.TextPrimary,
    surface = AutomotiveColors.Card,
    onSurface = AutomotiveColors.TextPrimary,
    surfaceVariant = AutomotiveColors.Elevated,
    onSurfaceVariant = AutomotiveColors.TextSecondary,
    surfaceTint = AutomotiveColors.Accent,
    outline = AutomotiveColors.TextSecondary,
    outlineVariant = AutomotiveColors.Elevated,
    inverseSurface = AutomotiveColors.TextPrimary,
    inverseOnSurface = AutomotiveColors.Background,
    inversePrimary = AutomotiveColors.Background,
    error = AutomotiveColors.Error,
    onError = AutomotiveColors.Background,
    errorContainer = AutomotiveColors.Elevated,
    onErrorContainer = AutomotiveColors.Error,
    surfaceBright = AutomotiveColors.Elevated,
    surfaceDim = AutomotiveColors.Background,
    surfaceContainerLowest = AutomotiveColors.Background,
    surfaceContainerLow = AutomotiveColors.Card,
    surfaceContainer = AutomotiveColors.Card,
    surfaceContainerHigh = AutomotiveColors.Elevated,
    surfaceContainerHighest = AutomotiveColors.Elevated
)

val AutomotiveTypography = Typography(
    displayLarge = AutomotiveText.DisplayLarge,
    titleLarge = AutomotiveText.TitleLarge,
    titleMedium = AutomotiveText.TitleMedium,
    bodyMedium = AutomotiveText.BodyMedium,
    labelMedium = AutomotiveText.LabelMedium
)

val AutomotiveShapes = Shapes(
    extraSmall = RoundedCornerShape(AutomotiveDimensions.RadiusSmall),
    small = RoundedCornerShape(AutomotiveDimensions.RadiusSmall),
    medium = RoundedCornerShape(AutomotiveDimensions.RadiusMedium),
    large = RoundedCornerShape(AutomotiveDimensions.RadiusLarge),
    extraLarge = RoundedCornerShape(AutomotiveDimensions.RadiusExtraLarge)
)

// Explicit opt-in keeps existing screens and their light/dark theme unchanged during migration.
@Composable
fun AutomotiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AutomotiveDarkColorScheme,
        typography = AutomotiveTypography,
        shapes = AutomotiveShapes,
        content = content
    )
}
