package cl.cristian.bitacoraobd.ui.diagnostic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.DiagnosticScanResult
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.ui.AppSession
import cl.cristian.bitacoraobd.ui.ExitAction
import cl.cristian.bitacoraobd.ui.VehicleLoadStatus
import cl.cristian.bitacoraobd.ui.components.GraphiteCard
import cl.cristian.bitacoraobd.ui.navigation.AppRoute
import cl.cristian.bitacoraobd.ui.theme.PrototypeBadSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeCardContent
import cl.cristian.bitacoraobd.ui.theme.PrototypeGood
import cl.cristian.bitacoraobd.ui.theme.PrototypeSurface

@Composable
fun DiagnosticScreen(session: AppSession, diagnostics: DiagnosticRepository, onHistory: (Long) -> Unit = {}) {
    val state by session.state.collectAsState()
    val connection by session.connectionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Diagnóstico", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Revisa el estado y los códigos de tu vehículo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (state.vehicleLoadStatus) {
            VehicleLoadStatus.Loading -> {
                GraphiteCard(Modifier.fillMaxWidth()) {
                    Text(
                        "Cargando vehículo…",
                        Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                return@Column
            }
            VehicleLoadStatus.Failed -> {
                GraphiteCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("No se pudo cargar el vehículo", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = session::refreshVehicleContext, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Reintentar") }
                    }
                }
                return@Column
            }
            VehicleLoadStatus.Ready -> Unit
        }

        val vehicle = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
        if (vehicle == null) {
            GraphiteCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Agrega un vehículo para comenzar", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.VEHICLES)) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) {
                        Text("Agregar vehículo")
                    }
                }
            }
            return@Column
        }

        GraphiteCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Vehículo seleccionado", style = MaterialTheme.typography.labelLarge, color = PrototypeCardContent)
                Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(
                    onClick = { onHistory(vehicle.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent),
                    border = BorderStroke(1.dp, PrototypeCardContent)
                ) { Text("Ver historial") }
            }
        }

        if (connection !is ObdConnectionState.Connected) {
            GraphiteCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Adaptador desconectado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Conecta el adaptador OBD para iniciar un escaneo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.CONNECTION)) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) {
                        Text("Abrir conexión")
                    }
                }
            }
        }

        if (connection is ObdConnectionState.Connected && state.pendingDiagnostic == null) {
            Button(
                onClick = session::scanDiagnostic,
                enabled = state.operation == null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)
            ) {
                Text(if (state.savedDiagnostic != null) "Nuevo escaneo" else "Escanear códigos")
            }
        }

        if (state.operation == "scan") {
            GraphiteCard(Modifier.fillMaxWidth()) {
                Text(
                    "Escaneando…",
                    Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        if (state.scanCancelled) {
            GraphiteCard(Modifier.fillMaxWidth()) {
                Text(
                    "Se canceló el último escaneo; no se guardó una captura nueva.",
                    Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        state.error?.let { message ->
            GraphiteCard(Modifier.fillMaxWidth()) {
                Text(
                    message,
                    Modifier.padding(20.dp),
                    color = PrototypeCardContent
                )
            }
        }

        val result: DiagnosticScanResult.Success? = state.pendingDiagnostic ?: state.savedDiagnostic
        result?.takeIf { it.vehicleId == vehicle.id }?.let { capture ->
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        if (state.savedDiagnostic?.captureId == capture.captureId && state.savedDiagnosticId != null) {
                            if (state.scanCancelled || state.scanFailed) "Diagnóstico anterior guardado" else "Diagnóstico guardado"
                        } else {
                            "Resultado pendiente"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (state.pendingDiagnostic != null) {
                        Text(
                            "Si cierras la app antes de guardar, perderás esta captura.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrototypeCardContent
                        )
                    }
                    Text(
                        "Capturado ${java.text.DateFormat.getDateTimeInstance().format(capture.timestampMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrototypeCardContent
                    )
                    if (capture.codes.isEmpty()) {
                        GraphiteCard {
                            Text(
                                "Sin códigos almacenados",
                                Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    capture.codes.forEach { code ->
                        val guidance = diagnostics.getDtcGuidance(code)
                        GraphiteCard {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(guidance.code, style = MaterialTheme.typography.titleMedium)
                                Text(guidance.description, style = MaterialTheme.typography.bodyLarge)
                                guidance.possibleCauses.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                guidance.orientativeActions.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                    if (state.pendingDiagnostic != null) {
                        if (state.operation == "save") Text("Guardando diagnóstico…")
                        if (state.operation != "save" && state.error != null) {
                            Text("No se pudo guardar el diagnóstico. Puedes reintentar.")
                        }
                        Button(
                            onClick = session::savePendingDiagnostic,
                            enabled = state.operation != "save",
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)
                        ) {
                            Text(if (state.error != null) "Reintentar guardado" else "Guardar diagnóstico")
                        }
                        OutlinedButton(
                            onClick = {
                                session.discardPendingDiagnostic()
                                session.requestExit(ExitAction.ToRoute(AppRoute.HOME))
                            },
                            enabled = state.operation != "save",
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeBadSoft),
                            border = BorderStroke(1.dp, PrototypeBadSoft)
                        ) {
                            Text("Descartar y volver a Inicio")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.HOME)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver a Inicio")
                        }
                    }
                }
            }
        }
    }
}
