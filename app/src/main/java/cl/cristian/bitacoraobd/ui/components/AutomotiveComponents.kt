package cl.cristian.bitacoraobd.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.ui.theme.AutomotiveColors
import cl.cristian.bitacoraobd.ui.theme.AutomotiveDimensions
import cl.cristian.bitacoraobd.ui.theme.AutomotiveText

enum class AutomotiveButtonKind { Primary, Secondary }

@Composable
fun AutomotiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: AutomotiveButtonKind = AutomotiveButtonKind.Primary,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AutomotiveDimensions.RadiusMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (kind == AutomotiveButtonKind.Primary) AutomotiveColors.Accent else AutomotiveColors.Elevated,
            contentColor = if (kind == AutomotiveButtonKind.Primary) AutomotiveColors.Background else AutomotiveColors.TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = AutomotiveDimensions.Space4, vertical = AutomotiveDimensions.Space3)
    ) {
        Text(text, style = AutomotiveText.ActionLabel, textAlign = TextAlign.Center)
    }
}

@Composable
fun AutomotiveCard(
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AutomotiveDimensions.RadiusLarge),
        color = if (emphasis) AutomotiveColors.Elevated else AutomotiveColors.Card,
        contentColor = AutomotiveColors.TextPrimary
    ) {
        Column(
            Modifier.padding(AutomotiveDimensions.Space4),
            verticalArrangement = Arrangement.spacedBy(AutomotiveDimensions.Space2),
            content = content
        )
    }
}

enum class AutomotiveStatus { Success, Warning, Error }

@Composable
fun StatusBadge(
    text: String,
    status: AutomotiveStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 44.dp).semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(10.dp),
        color = AutomotiveColors.Elevated,
        contentColor = when (status) {
            AutomotiveStatus.Success -> AutomotiveColors.Success
            AutomotiveStatus.Warning -> AutomotiveColors.Warning
            AutomotiveStatus.Error -> AutomotiveColors.Error
        }
    ) {
        Text(
            text,
            modifier = Modifier.padding(10.dp),
            style = AutomotiveText.StatusLabel
        )
    }
}

@Composable
fun VehicleTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier = modifier.semantics { heading() },
        style = AutomotiveText.VehicleTitle,
        color = AutomotiveColors.TextPrimary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

// Figma VehicleCard / Selected (53:22); deliberately accepts display text, not a domain model.
@Composable
fun VehicleCard(
    title: String,
    eyebrow: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    AutomotiveCard(modifier) {
        Text(eyebrow, style = AutomotiveText.CardEyebrow, color = AutomotiveColors.TextSecondary)
        VehicleTitle(title)
        Text(detail, style = AutomotiveText.CardDetail, color = AutomotiveColors.TextSecondary)
    }
}
