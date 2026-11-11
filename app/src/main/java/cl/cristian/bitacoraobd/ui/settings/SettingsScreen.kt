package cl.cristian.bitacoraobd.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import cl.cristian.bitacoraobd.ui.components.GraphiteCard
import cl.cristian.bitacoraobd.ui.theme.PrototypeBad
import cl.cristian.bitacoraobd.ui.theme.PrototypeBadSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeCardContent
import cl.cristian.bitacoraobd.ui.theme.PrototypeGood
import cl.cristian.bitacoraobd.ui.theme.PrototypeOutline
import cl.cristian.bitacoraobd.ui.theme.PrototypeSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.TemperatureUnit

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onLeave: (String) -> Unit) {
    val draft = viewModel.draft
    BackHandler(enabled = !viewModel.loading && !viewModel.saving) {
        if (viewModel.dirty) viewModel.requestLeave("back") else onLeave("back")
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
            Text("Personaliza la información que muestra Bitácora OBD", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (viewModel.loading && draft == null) {
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator()
                    Text("Cargando ajustes…")
                }
            }
        } else if (draft == null) {
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(viewModel.error ?: "No se pudieron cargar los ajustes", color = PrototypeBadSoft)
                    Button(
                        onClick = viewModel::reload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrototypeGood,
                            contentColor = PrototypeSurface
                        )
                    ) { Text("Reintentar") }
                }
            }
        } else {
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nombre", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = draft.displayName.orEmpty(),
                        onValueChange = { value -> viewModel.update { it.copy(displayName = value.ifBlank { null }) } },
                        label = { Text("Nombre (opcional)") },
                        singleLine = true,
                        enabled = !viewModel.saving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrototypeCardContent,
                            unfocusedTextColor = PrototypeCardContent,
                            disabledTextColor = PrototypeCardContent,
                            focusedLabelColor = PrototypeCardContent,
                            unfocusedLabelColor = PrototypeCardContent,
                            disabledLabelColor = PrototypeCardContent,
                            cursorColor = PrototypeCardContent,
                            focusedBorderColor = PrototypeCardContent,
                            unfocusedBorderColor = PrototypeCardContent,
                            disabledBorderColor = PrototypeCardContent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Unidades", style = MaterialTheme.typography.titleLarge)
                    Text("Distancia y velocidad", style = MaterialTheme.typography.titleSmall, color = PrototypeCardContent)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceButton("Kilómetros", draft.distanceUnit == DistanceUnit.KILOMETERS, !viewModel.saving, Modifier.weight(1f)) {
                            viewModel.update { it.copy(distanceUnit = DistanceUnit.KILOMETERS) }
                        }
                        ChoiceButton("Millas", draft.distanceUnit == DistanceUnit.MILES, !viewModel.saving, Modifier.weight(1f)) {
                            viewModel.update { it.copy(distanceUnit = DistanceUnit.MILES) }
                        }
                    }
                    Text("Temperatura", style = MaterialTheme.typography.titleSmall, color = PrototypeCardContent)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceButton("°C", draft.temperatureUnit == TemperatureUnit.CELSIUS, !viewModel.saving, Modifier.weight(1f)) {
                            viewModel.update { it.copy(temperatureUnit = TemperatureUnit.CELSIUS) }
                        }
                        ChoiceButton("°F", draft.temperatureUnit == TemperatureUnit.FAHRENHEIT, !viewModel.saving, Modifier.weight(1f)) {
                            viewModel.update { it.copy(temperatureUnit = TemperatureUnit.FAHRENHEIT) }
                        }
                    }
                }
            }
            GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Métricas en Inicio", style = MaterialTheme.typography.titleLarge)
                    SettingSwitch("RPM", draft.showRpm, !viewModel.saving) {
                        viewModel.update { settings -> settings.copy(showRpm = it) }
                    }
                    SettingSwitch("Refrigerante", draft.showCoolant, !viewModel.saving) {
                        viewModel.update { settings -> settings.copy(showCoolant = it) }
                    }
                    SettingSwitch("Velocidad", draft.showSpeed, !viewModel.saving) {
                        viewModel.update { settings -> settings.copy(showSpeed = it) }
                    }
                }
            }
            viewModel.error?.let {
                GraphiteCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(it, color = PrototypeBadSoft)
                        OutlinedButton(
                            onClick = viewModel::save,
                            enabled = !viewModel.saving,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent),
                            border = BorderStroke(1.dp, PrototypeOutline)
                        ) { Text("Reintentar") }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::save,
                    enabled = viewModel.dirty && !viewModel.saving,
                    colors = ButtonDefaults.buttonColors(
                            containerColor = PrototypeGood,
                            contentColor = PrototypeSurface
                        )
                ) {
                    Text(if (viewModel.saving) "Guardando…" else "Guardar")
                }
                OutlinedButton(onClick = {
                    if (viewModel.dirty) viewModel.requestLeave("back") else onLeave("back")
                }, enabled = !viewModel.saving) { Text("Volver") }
            }
        }
    }
    if (viewModel.pendingLeave) AlertDialog(
        onDismissRequest = viewModel::keepEditing,
        title = { Text("Descartar cambios") },
        text = { Text("Se perderán los cambios de los ajustes.") },
        confirmButton = {
            Button(
                onClick = { viewModel.discard()?.let(onLeave) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrototypeBad,
                    contentColor = PrototypeSurface
                )
            ) { Text("Descartar") }
        },
        dismissButton = { TextButton(onClick = viewModel::keepEditing) { Text("Seguir editando") } }
    )
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrototypeGood,
                contentColor = PrototypeSurface
            )
        ) { Text(label) }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent),
            border = BorderStroke(1.dp, PrototypeOutline)
        ) { Text(label) }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}
