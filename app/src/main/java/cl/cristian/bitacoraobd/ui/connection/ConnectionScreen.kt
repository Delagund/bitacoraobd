package cl.cristian.bitacoraobd.ui.connection

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.TemperatureUnit
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.ui.AppSession

@Composable
fun ConnectionScreen(
    session: AppSession,
    model: ConnectionViewModel,
    preferences: AppPreferencesRepository,
    requestExit: () -> Unit
) {
    val context = LocalContext.current
    val state by session.state.collectAsState()
    val connection by session.connectionState.collectAsState()
    val settings by preferences.settings.collectAsState(initial = AppSettings())

    LaunchedEffect(connection, state.connectedConfigSavePending, state.operation) {
        val connected = connection as? ObdConnectionState.Connected
        if (connected != null && state.connectedConfigSavePending == null && state.operation == null) {
            model.markSaved(connected.config)
        }
    }

    var showWifiNotice by remember { mutableStateOf(true) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permission = "android.permission.ACCESS_LOCAL_NETWORK"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) model.connect()
    }

    if (showWifiNotice) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Conecta el teléfono al Wi-Fi del adaptador") },
            text = { Text("La conexión con el vehículo se inicia cuando pulses Conectar.") },
            confirmButton = { TextButton(onClick = { showWifiNotice = false }) { Text("Siguiente") } },
            dismissButton = { TextButton(onClick = requestExit) { Text("Cancelar") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Conexión OBD", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Configura el adaptador para consultar tu vehículo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!model.loaded) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(model.error ?: "Cargando configuración…", style = MaterialTheme.typography.bodyLarge)
                    if (model.error != null) Button(onClick = model::reload) { Text("Reintentar") }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Datos del adaptador", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = model.ip,
                        onValueChange = model::editIp,
                        label = { Text("Dirección IP") },
                        enabled = connection !is ObdConnectionState.Connected && connection !is ObdConnectionState.Connecting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = model.port,
                        onValueChange = model::editPort,
                        label = { Text("Puerto") },
                        enabled = connection !is ObdConnectionState.Connected && connection !is ObdConnectionState.Connecting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    model.error?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }
                    state.error?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }
                    when (connection) {
                        is ObdConnectionState.Connected -> ConnectionStatusCard("Conectado", connected = true)
                        is ObdConnectionState.Connecting -> ConnectionStatusCard("Conectando…")
                        is ObdConnectionState.Disconnecting -> ConnectionStatusCard("Desconectando…")
                        is ObdConnectionState.Error -> ConnectionStatusCard("Error de conexión", error = true)
                        else -> ConnectionStatusCard("Desconectado")
                    }

                    if (connection is ObdConnectionState.Connecting) {
                        OutlinedButton(
                            onClick = session::cancelConnection,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cancelar conexión") }
                    } else if (connection is ObdConnectionState.Connected) {
                        Button(
                            onClick = session::readMetrics,
                            enabled = state.operation == null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Actualizar lectura") }
                        state.metrics?.takeIf { it.first == state.activeVehicleId }?.second?.let { metrics ->
                            val vehicle = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
                            Card {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Lectura de ${vehicle?.brand.orEmpty()} ${vehicle?.model.orEmpty()}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        java.text.DateFormat.getDateTimeInstance().format(metrics.timestampMillis),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("RPM: ${metrics.rpm?.toString() ?: "No disponible"}", style = MaterialTheme.typography.bodyLarge)
                                    val temperature = metrics.coolantTempCelsius?.let {
                                        if (settings.temperatureUnit == TemperatureUnit.FAHRENHEIT) it * 9 / 5 + 32 else it
                                    }
                                    val speed = metrics.speedKmh?.let {
                                        if (settings.distanceUnit == DistanceUnit.MILES) (it * 0.621371).toInt() else it
                                    }
                                    Text(
                                        "Temperatura: ${temperature?.toString() ?: "No disponible"} ${if (settings.temperatureUnit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Velocidad: ${speed?.toString() ?: "No disponible"} ${if (settings.distanceUnit == DistanceUnit.MILES) "mph" else "km/h"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = session::disconnect,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Desconectar") }
                    } else if (connection !is ObdConnectionState.Disconnecting) {
                        Button(
                            onClick = {
                                if (!model.validConfig()) return@Button
                                if (Build.VERSION.SDK_INT < 37 ||
                                    context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionDenied = false
                                    model.connect()
                                } else launcher.launch(permission)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Conectar") }
                    }

                    state.connectedConfigSavePending?.let {
                        Button(
                            onClick = session::retryConnectedConfigSave,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reintentar guardado de configuración") }
                    }
                }
            }

            if (permissionDenied) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Permite el acceso a la red local para conectar con el adaptador.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = { launcher.launch(permission) }) { Text("Reintentar permiso") }
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }) { Text("Abrir ajustes de la aplicación") }
                    }
                }
            }
        }

        OutlinedButton(onClick = requestExit, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a Diagnóstico")
        }
    }
}

@Composable
private fun ConnectionStatusCard(label: String, connected: Boolean = false, error: Boolean = false) {
    val background = when {
        error -> MaterialTheme.colorScheme.errorContainer
        connected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val foreground = when {
        error -> MaterialTheme.colorScheme.onErrorContainer
        connected -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            color = foreground
        )
    }
}
