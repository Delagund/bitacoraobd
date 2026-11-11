package cl.cristian.bitacoraobd.ui.navigation

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Local vector versions of the four navigation icons in the approved prototype. */
internal object PrototypeIcons {
    val Home: ImageVector by lazy {
        ImageVector.Builder("Home", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 11.5f)
                lineTo(12f, 4f)
                lineTo(21f, 11.5f)
                moveTo(5.5f, 10f)
                verticalLineTo(20f)
                horizontalLineTo(18.5f)
                verticalLineTo(10f)
                moveTo(9.5f, 20f)
                verticalLineTo(14f)
                horizontalLineTo(14.5f)
                verticalLineTo(20f)
            }
        }.build()
    }

    val Vehicle: ImageVector by lazy {
        ImageVector.Builder("Vehicle", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 16f)
                lineTo(3.5f, 14.5f)
                verticalLineTo(10.5f)
                lineTo(6f, 6f)
                horizontalLineTo(18f)
                lineTo(20.5f, 10.5f)
                verticalLineTo(14.5f)
                lineTo(19f, 16f)
                moveTo(5f, 10.5f)
                horizontalLineTo(19f)
                moveTo(7f, 16f)
                verticalLineTo(18f)
                moveTo(17f, 16f)
                verticalLineTo(18f)
                moveTo(7.5f, 13f)
                horizontalLineTo(7.51f)
                moveTo(16.5f, 13f)
                horizontalLineTo(16.51f)
            }
        }.build()
    }

    val Diagnostic: ImageVector by lazy {
        ImageVector.Builder("Diagnostic", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 8f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                moveTo(16f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(8f)
                moveTo(20f, 16f)
                verticalLineTo(20f)
                horizontalLineTo(16f)
                moveTo(8f, 20f)
                horizontalLineTo(4f)
                verticalLineTo(16f)
                moveTo(8f, 12f)
                horizontalLineTo(16f)
                moveTo(12f, 8f)
                verticalLineTo(16f)
            }
        }.build()
    }

    val Maintenance: ImageVector by lazy {
        ImageVector.Builder("Maintenance", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14.5f, 6.5f)
                curveTo(13f, 5f, 11f, 5f, 9.5f, 6.5f)
                lineTo(12f, 9f)
                lineTo(4f, 17f)
                curveTo(2f, 19f, 5f, 22f, 7f, 20f)
                lineTo(15f, 12f)
                lineTo(17.5f, 14.5f)
                curveTo(19.5f, 12f, 18f, 9f, 16f, 8f)
                lineTo(14.5f, 6.5f)
            }
        }.build()
    }
}
