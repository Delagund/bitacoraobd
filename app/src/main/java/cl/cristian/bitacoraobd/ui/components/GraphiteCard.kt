package cl.cristian.bitacoraobd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.ui.theme.PrototypeCardContent
import cl.cristian.bitacoraobd.ui.theme.PrototypeDarkSurface
import cl.cristian.bitacoraobd.ui.theme.PrototypeOutline

@Composable
fun GraphiteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = PrototypeDarkSurface,
            contentColor = PrototypeCardContent
        ),
        border = BorderStroke(1.dp, PrototypeOutline),
        content = content
    )
}
