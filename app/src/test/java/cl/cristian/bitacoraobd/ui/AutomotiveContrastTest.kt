package cl.cristian.bitacoraobd.ui

import androidx.compose.ui.graphics.Color
import cl.cristian.bitacoraobd.ui.theme.AutomotiveColors
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AutomotiveContrastTest {
    @Test
    fun textAndStatusPairsMeetNormalTextContrast() {
        val pairs = listOf(
            AutomotiveColors.TextPrimary to AutomotiveColors.Card,
            AutomotiveColors.TextSecondary to AutomotiveColors.Card,
            AutomotiveColors.TextSecondary to AutomotiveColors.Elevated,
            AutomotiveColors.Background to AutomotiveColors.Accent,
            AutomotiveColors.TextPrimary to AutomotiveColors.Elevated,
            AutomotiveColors.Success to AutomotiveColors.Elevated,
            AutomotiveColors.Warning to AutomotiveColors.Elevated,
            AutomotiveColors.Error to AutomotiveColors.Elevated
        )
        pairs.forEach { (foreground, background) ->
            val contrast = (maxOf(luminance(foreground), luminance(background)) + 0.05) /
                (minOf(luminance(foreground), luminance(background)) + 0.05)
            assertTrue("$foreground on $background has contrast $contrast", contrast >= 4.5)
        }
    }

    private fun luminance(color: Color): Double {
        fun linear(value: Float): Double =
            if (value <= 0.04045f) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        return linear(color.red) * 0.2126 + linear(color.green) * 0.7152 + linear(color.blue) * 0.0722
    }
}
