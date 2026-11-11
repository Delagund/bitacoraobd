package cl.cristian.bitacoraobd.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.TemperatureUnit
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.ui.AppSession
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.theme.PrototypeBad
import cl.cristian.bitacoraobd.ui.theme.PrototypeBadSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeWarn
import cl.cristian.bitacoraobd.ui.theme.PrototypeWarnSoft
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    session: AppSession,
    settings: AppSettings,
    onVehicles: () -> Unit,
    onMaintenance: () -> Unit,
    onDiagnostic: () -> Unit,
    onDetail: (Long, Long) -> Unit,
    onHistory: (Long) -> Unit,
    onSettings: () -> Unit,
    onConnection: () -> Unit
) {
    val home by viewModel.state.collectAsState()
    val app by session.state.collectAsState()
    val connection by session.connectionState.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            settings.displayName?.takeIf(String::isNotBlank) ?: "Inicio",
            style = MaterialTheme.typography.headlineMedium
        )

        if (app.vehicleLoadStatus.name == "Ready" && app.vehicles.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Aún no hay vehículos", style = MaterialTheme.typography.titleLarge)
                    Text("Agrega un vehículo para ver su estado y sus próximos mantenimientos.")
                    Button(onClick = onVehicles) { Text("Agregar vehículo") }
                }
            }
        } else if (home.loading && home.dashboard == null) {
            CircularProgressIndicator()
        } else if (home.error != null && home.dashboard == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("No se pudo cargar Inicio", style = MaterialTheme.typography.titleLarge)
                    Text(home.error ?: "Inténtalo nuevamente.")
                    Button(onClick = viewModel::retry) { Text("Reintentar") }
                }
            }
        } else {
            home.error?.let {
                Text("No se pudo actualizar. Los datos pueden estar desactualizados. $it",
                    color = MaterialTheme.colorScheme.error)
            }
            if (home.loading) Text("Actualizando…", style = MaterialTheme.typography.labelLarge)

            when (val dashboard = home.dashboard) {
                is DashboardState.Ready -> {
                    val summary = dashboard.summary
                    if (summary.activeVehicle.id == app.activeVehicleId) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Vehículo activo", style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    listOf(summary.activeVehicle.brand, summary.activeVehicle.model)
                                        .filter(String::isNotBlank).joinToString(" ").ifBlank { "Vehículo" },
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Conexión: ${connectionLabel(summary.connectionState)}")
                                Button(onClick = onConnection) { Text("Abrir conexión") }
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Mantenimiento", style = MaterialTheme.typography.titleLarge)
                                if (summary.activeMaintenanceSchedules.isEmpty()) {
                                    Text("Aún no hay mantenimientos programados.")
                                } else {
                                    summary.activeMaintenanceSchedules.forEach { item ->
                                        val status = when (item.status) {
                                            MaintenanceStatus.OVERDUE -> "Vencido"
                                            MaintenanceStatus.UPCOMING -> "Próximo"
                                            MaintenanceStatus.UNSCHEDULED -> "Sin programación"
                                        }
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.record.serviceType.name, modifier = Modifier.weight(1f))
                                            StatusPill(
                                                status,
                                                when (item.status) {
                                                    MaintenanceStatus.OVERDUE -> PrototypeBadSoft
                                                    MaintenanceStatus.UPCOMING -> PrototypeWarnSoft
                                                    MaintenanceStatus.UNSCHEDULED -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                when (item.status) {
                                                    MaintenanceStatus.OVERDUE -> PrototypeBad
                                                    MaintenanceStatus.UPCOMING -> PrototypeWarn
                                                    MaintenanceStatus.UNSCHEDULED -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                                OutlinedButton(onClick = onMaintenance) { Text("Ver mantenimiento") }
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Último diagnóstico", style = MaterialTheme.typography.titleLarge)
                                val sessionRecord = summary.lastDiagnosticSession
                                if (sessionRecord == null) {
                                    Text("Aún no hay diagnósticos guardados.")
                                } else {
                                    Text(
                                        if (sessionRecord.codes.isEmpty()) "Sin códigos detectados"
                                        else "${sessionRecord.codes.size} código(s) detectado(s)",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(DateFormat.getDateTimeInstance().format(Date(sessionRecord.timestampMillis)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    summary.codesInLastSession.forEach { code ->
                                        Text("${code.code}: ${code.guidance.description}${if (code.isRecurrent) " · Recurrente" else ""}")
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onDetail(summary.activeVehicle.id, sessionRecord.id) }) {
                                            Text("Ver detalle")
                                        }
                                        OutlinedButton(onClick = { onHistory(summary.activeVehicle.id) }) {
                                            Text("Historial")
                                        }
                                    }
                                }
                            }
                        }

                        if (settings.showRpm || settings.showCoolant || settings.showSpeed) {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Métricas OBD", style = MaterialTheme.typography.titleLarge)
                                        Text(
                                            "Lectura puntual",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    val metrics = app.metrics?.takeIf { it.first == summary.activeVehicle.id }?.second
                                    val tempUnit = if (settings.temperatureUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"
                                    if (metrics == null) {
                                        Text("Sin lectura disponible.")
                                    } else {
                                        Text(
                                            "Lectura: ${DateFormat.getDateTimeInstance().format(Date(metrics.timestampMillis))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val values = buildList {
                                            if (settings.showRpm) add("RPM" to (metrics.rpm?.toString() ?: "No disponible"))
                                            if (settings.showCoolant) {
                                                val temp = metrics.coolantTempCelsius?.let {
                                                    if (settings.temperatureUnit == TemperatureUnit.FAHRENHEIT) it * 9 / 5 + 32 else it
                                                }
                                                add("Refrigerante" to "${temp?.toString() ?: "No disponible"} $tempUnit")
                                            }
                                            if (settings.showSpeed) {
                                                val speed = metrics.speedKmh?.let {
                                                    if (settings.distanceUnit == DistanceUnit.MILES) (it * 0.621371).toInt() else it
                                                }
                                                add("Velocidad" to "${speed?.toString() ?: "No disponible"} ${if (settings.distanceUnit == DistanceUnit.MILES) "mph" else "km/h"}")
                                            }
                                        }
                                        values.chunked(2).forEach { rowValues ->
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowValues.forEach { (label, value) ->
                                                    MetricCard(label, value, Modifier.weight(1f))
                                                }
                                                if (rowValues.size == 1) {
                                                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                        if (connection !is ObdConnectionState.Connected) {
                                            Text("Desconectado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Button(onClick = onDiagnostic) { Text("Abrir diagnóstico") }
                                }
                            }
                        }
                    }
                }
                DashboardState.NoVehicles -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Aún no hay vehículos", style = MaterialTheme.typography.titleLarge)
                            Button(onClick = onVehicles) { Text("Agregar vehículo") }
                        }
                    }
                }
                null -> if (app.vehicleLoadStatus.name == "Failed") {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(app.error ?: "No se pudieron cargar los vehículos")
                            Button(onClick = session::refreshVehicleContext) { Text("Reintentar") }
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = onSettings) { Text("Ajustes") }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatusPill(label: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.small) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium)
    }
}

private fun connectionLabel(state: ObdConnectionState): String = when (state) {
    ObdConnectionState.Disconnected -> "Desconectado"
    ObdConnectionState.Connecting -> "Conectando…"
    is ObdConnectionState.Connected -> "Conectado"
    ObdConnectionState.Disconnecting -> "Desconectando…"
    is ObdConnectionState.Error -> "Error de conexión"
}
