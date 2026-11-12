package cl.cristian.bitacoraobd.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cl.cristian.bitacoraobd.ui.theme.AutomotiveColors
import cl.cristian.bitacoraobd.ui.theme.AutomotiveDimensions
import cl.cristian.bitacoraobd.ui.theme.AutomotiveText
import cl.cristian.bitacoraobd.ui.theme.AutomotiveTheme

@Preview(name = "Automotive · 390", widthDp = 390, heightDp = 1100, showBackground = true)
@Preview(name = "Automotive · narrow · large font", widthDp = 320, heightDp = 1400, fontScale = 1.5f, showBackground = true)
@Preview(name = "Automotive · wide", widthDp = 600, heightDp = 1100, showBackground = true)
@Composable
fun AutomotiveComponentsPreview() {
    AutomotiveTheme {
        Surface(color = AutomotiveColors.Background) {
            Column(
                Modifier.padding(AutomotiveDimensions.Space4),
                verticalArrangement = Arrangement.spacedBy(AutomotiveDimensions.Space3)
            ) {
                AutomotiveButton("Registrar mantenimiento", {}, Modifier.fillMaxWidth())
                AutomotiveButton("Ver historial de servicios", {}, Modifier.fillMaxWidth(), AutomotiveButtonKind.Secondary)
                AutomotiveButton("Acción no disponible", {}, Modifier.fillMaxWidth(), enabled = false)
                AutomotiveButton("Acción secundaria no disponible", {}, Modifier.fillMaxWidth(), AutomotiveButtonKind.Secondary, enabled = false)
                AutomotiveCard(Modifier.fillMaxWidth()) {
                    Text("TÍTULO DE TARJETA", style = AutomotiveText.CardEyebrow, color = AutomotiveColors.TextSecondary)
                    Text("Contenido principal", style = AutomotiveText.CardTitle)
                    Text("Información secundaria", style = AutomotiveText.CardSupporting, color = AutomotiveColors.TextSecondary)
                }
                AutomotiveCard(Modifier.fillMaxWidth(), emphasis = true) {
                    Text("Tarjeta destacada", style = AutomotiveText.CardTitle)
                }
                StatusBadge("Correcto", AutomotiveStatus.Success)
                StatusBadge("Atención", AutomotiveStatus.Warning)
                StatusBadge("Vencido", AutomotiveStatus.Error)
                VehicleCard("Toyota Corolla · 2018", "VEHÍCULO ACTIVO", "Kilometraje actual · 125.400 km", Modifier.fillMaxWidth())
                VehicleTitle("Un vehículo con un nombre deliberadamente largo para comprobar dos líneas")
            }
        }
    }
}
