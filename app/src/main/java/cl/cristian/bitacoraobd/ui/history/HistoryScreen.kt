package cl.cristian.bitacoraobd.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.ui.formatDistance
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    vehicleId: Long,
    viewModel: HistoryViewModel,
    distanceUnit: DistanceUnit,
    onOpenSession: (Long) -> Unit
) {
    val state by viewModel.sessions.collectAsState()
    LaunchedEffect(vehicleId) { viewModel.observeVehicle(vehicleId) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Historial de diagnósticos", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Revisa las lecturas guardadas de este vehículo",
            Modifier.padding(top = 4.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when (val value = state) {
            HistoryLoad.Loading -> Card(Modifier.fillMaxWidth()) {
                Text("Cargando historial…", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge)
            }
            is HistoryLoad.Failed -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(value.message, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { viewModel.retry(vehicleId) }) { Text("Reintentar") }
                }
            }
            is HistoryLoad.Ready -> if (value.value.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Aún no hay diagnósticos guardados", style = MaterialTheme.typography.titleMedium)
                        Text("Las sesiones que guardes aparecerán aquí.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(value.value, key = { it.id }) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenSession(session.id) }
                        ) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    DateFormat.getDateTimeInstance().format(Date(session.timestampMillis)),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(formatDistance(session.mileageAtScanKm, distanceUnit), style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (session.codes.isEmpty()) "Sin códigos" else "${session.codes.size} códigos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticDetailScreen(
    vehicleId: Long,
    sessionId: Long,
    viewModel: HistoryViewModel,
    distanceUnit: DistanceUnit
) {
    val state by viewModel.detail.collectAsState()
    LaunchedEffect(vehicleId, sessionId) { viewModel.loadDetail(vehicleId, sessionId) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Detalle del diagnóstico", style = MaterialTheme.typography.headlineMedium)
        when (val value = state) {
            null, HistoryLoad.Loading -> Card(Modifier.fillMaxWidth()) {
                Text("Cargando diagnóstico…", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge)
            }
            is HistoryLoad.Failed -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(value.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.retryDetail(vehicleId, sessionId) }) { Text("Reintentar") }
                }
            }
            is HistoryLoad.Ready -> {
                val session: DiagnosticSession = value.value.session
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(DateFormat.getDateTimeInstance().format(Date(session.timestampMillis)), style = MaterialTheme.typography.titleLarge)
                        Text("Kilometraje: ${formatDistance(session.mileageAtScanKm, distanceUnit)}", style = MaterialTheme.typography.bodyLarge)
                        if (value.value.codes.isEmpty()) Text("Sin códigos almacenados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else Text("${value.value.codes.size} códigos encontrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                value.value.codes.forEach { code ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(code.code, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            Text(if (code.isRecurrent) "Código recurrente" else "Primera aparición", style = MaterialTheme.typography.labelLarge)
                            Text(code.guidance.description, style = MaterialTheme.typography.bodyLarge)
                            if (code.guidance.isKnownInCatalog) {
                                Text("Sistema: ${code.guidance.relatedSystem}", style = MaterialTheme.typography.titleSmall)
                                code.guidance.possibleCauses.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                code.guidance.orientativeActions.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                }
            }
        }
    }
}
