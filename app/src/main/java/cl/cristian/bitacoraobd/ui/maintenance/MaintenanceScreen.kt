package cl.cristian.bitacoraobd.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.ui.formatDistance
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.ui.components.GraphiteCard
import cl.cristian.bitacoraobd.ui.theme.PrototypeBad
import cl.cristian.bitacoraobd.ui.theme.PrototypeBadSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeCardContent
import cl.cristian.bitacoraobd.ui.theme.PrototypeGood
import cl.cristian.bitacoraobd.ui.theme.PrototypeOutline
import cl.cristian.bitacoraobd.ui.theme.PrototypeWarnSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeSurface

@Composable
fun MaintenanceScreen(vehicleId: Long, viewModel: MaintenanceViewModel, distanceUnit: DistanceUnit) {
    val rows by viewModel.rows.collectAsState()
    val draft = viewModel.draft
    LaunchedEffect(vehicleId) { viewModel.observe(vehicleId) }
    DisposableEffect(vehicleId) {
        onDispose { viewModel.onScreenClosed(vehicleId) }
    }
    if (draft != null) {
        MaintenanceForm(draft, viewModel)
        viewModel.historicalMileage?.let { (entered, current) ->
            AlertDialog(
                onDismissRequest = viewModel::keepHistoricalDraft,
                title = { Text("Kilometraje histórico") },
                text = { Text("El vehículo registra ${formatDistance(current, draft.unit)} y este servicio indica ${formatDistance(entered, draft.unit)}. ¿Guardar el registro histórico?") },
                confirmButton = { Button(onClick = viewModel::confirmHistoricalSave, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Guardar igualmente") } },
                dismissButton = { TextButton(onClick = viewModel::keepHistoricalDraft) { Text("Seguir editando") } }
            )
        }
        return
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Mantenimiento", style = MaterialTheme.typography.headlineMedium)
            Text("Servicios y próximos vencimientos", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when (val state = rows) {
            LoadState.Loading -> GraphiteCard(Modifier.fillMaxWidth()) {
                Text("Cargando mantenimientos…", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge)
            }
            is LoadState.Failed -> GraphiteCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.message, color = PrototypeBadSoft)
                    OutlinedButton(onClick = { viewModel.retry(vehicleId) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent), border = BorderStroke(1.dp, PrototypeOutline)) { Text("Reintentar") }
                }
            }
            is LoadState.Ready -> if (state.value.isEmpty()) {
                GraphiteCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Aún no hay mantenimientos", style = MaterialTheme.typography.titleMedium)
                        Text("Registra un servicio para llevar el control de sus próximos vencimientos.", color = PrototypeCardContent.copy(alpha = 0.75f))
                        Button(onClick = { viewModel.add(vehicleId) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Registrar mantenimiento") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.value, key = { it.record.id }) { row ->
                        GraphiteCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(maintenanceTypeLabel(row.record.serviceType), style = MaterialTheme.typography.titleLarge)
                                    val status = when (row.status) {
                                        MaintenanceStatus.OVERDUE -> "Vencido"
                                        MaintenanceStatus.UPCOMING -> "Próximo"
                                        MaintenanceStatus.UNSCHEDULED, null -> "Sin programar"
                                    }
                                    Text(
                                        status,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (row.status) {
                                            MaintenanceStatus.OVERDUE -> PrototypeBadSoft
                                            MaintenanceStatus.UPCOMING -> PrototypeWarnSoft
                                            MaintenanceStatus.UNSCHEDULED, null -> PrototypeCardContent.copy(alpha = 0.75f)
                                        }
                                    )
                                }
                                Text("Realizado el ${row.record.performedDate}", style = MaterialTheme.typography.bodyMedium, color = PrototypeCardContent.copy(alpha = 0.75f))
                                Text("Kilometraje: ${formatDistance(row.record.mileageAtServiceKm, distanceUnit)}", style = MaterialTheme.typography.bodyMedium)
                                row.record.nextDueDate?.let { Text("Próximo servicio: $it", style = MaterialTheme.typography.bodyMedium) }
                                row.record.nextDueMileageKm?.let { Text("Próximo kilometraje: ${formatDistance(it, distanceUnit)}", style = MaterialTheme.typography.bodyMedium) }
                                OutlinedButton(onClick = { viewModel.edit(vehicleId, row.record.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent), border = BorderStroke(1.dp, PrototypeOutline)) { Text("Editar") }
                            }
                        }
                    }
                    item {
                        Button(onClick = { viewModel.add(vehicleId) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Registrar mantenimiento") }
                    }
                }
            }
        }
        viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun MaintenanceForm(draft: MaintenanceDraft, viewModel: MaintenanceViewModel) {
    var confirmCancel by remember { mutableStateOf(false) }
    val graphiteFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = PrototypeCardContent,
        unfocusedTextColor = PrototypeCardContent,
        disabledTextColor = PrototypeCardContent,
        focusedLabelColor = PrototypeCardContent,
        unfocusedLabelColor = PrototypeCardContent,
        disabledLabelColor = PrototypeCardContent,
        focusedPlaceholderColor = PrototypeCardContent,
        unfocusedPlaceholderColor = PrototypeCardContent,
        disabledPlaceholderColor = PrototypeCardContent,
        focusedLeadingIconColor = PrototypeCardContent,
        unfocusedLeadingIconColor = PrototypeCardContent,
        disabledLeadingIconColor = PrototypeCardContent,
        focusedTrailingIconColor = PrototypeCardContent,
        unfocusedTrailingIconColor = PrototypeCardContent,
        disabledTrailingIconColor = PrototypeCardContent,
        focusedBorderColor = PrototypeCardContent,
        unfocusedBorderColor = PrototypeOutline,
        disabledBorderColor = PrototypeOutline,
        cursorColor = PrototypeCardContent
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (draft.id == 0L) "Registrar mantenimiento" else "Editar mantenimiento", style = MaterialTheme.typography.headlineMedium)
        GraphiteCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MaintenanceTypeSelector(draft.type, graphiteFieldColors, enabled = !viewModel.saving) { type -> viewModel.update { it.copy(type = type) } }
                OutlinedTextField(
                    draft.performedDate, { value -> if (!viewModel.saving) viewModel.update { it.copy(performedDate = value) } },
                    label = { Text("Fecha (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !viewModel.saving, colors = graphiteFieldColors
                )
                OutlinedTextField(
                    draft.mileage, { value -> if (!viewModel.saving) viewModel.update { it.copy(mileage = value, mileageEdited = true) } },
                    label = { Text("Kilometraje (${if (draft.unit == DistanceUnit.MILES) "mi" else "km"})") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !viewModel.saving, colors = graphiteFieldColors
                )
                OutlinedTextField(
                    draft.nextDueDate, { value -> if (!viewModel.saving) viewModel.update { it.copy(nextDueDate = value) } },
                    label = { Text("Próxima fecha (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !viewModel.saving, colors = graphiteFieldColors
                )
                OutlinedTextField(
                    draft.nextDueMileage, { value -> if (!viewModel.saving) viewModel.update { it.copy(nextDueMileage = value, nextDueMileageEdited = true) } },
                    label = { Text("Próximo kilometraje (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !viewModel.saving, colors = graphiteFieldColors
                )
                OutlinedTextField(
                    draft.notes, { value -> if (!viewModel.saving) viewModel.update { it.copy(notes = value) } },
                    label = { Text("Notas (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !viewModel.saving, colors = graphiteFieldColors
                )
                draft.error?.let { Text(it, color = PrototypeBadSoft) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::save, enabled = !viewModel.saving, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Guardar") }
            OutlinedButton(onClick = {
                if (viewModel.isDirty(draft)) confirmCancel = true else viewModel.cancel()
            }, enabled = !viewModel.saving, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)) { Text("Cancelar") }
        }
    }
    if (confirmCancel) AlertDialog(
        onDismissRequest = { confirmCancel = false },
        title = { Text("Descartar cambios") },
        text = { Text("Se perderán los cambios del mantenimiento.") },
        confirmButton = { Button(onClick = { confirmCancel = false; viewModel.cancel() }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeBad, contentColor = PrototypeSurface)) { Text("Descartar") } },
        dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Seguir editando") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceTypeSelector(selected: MaintenanceType, colors: TextFieldColors, enabled: Boolean = true, onSelected: (MaintenanceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = maintenanceTypeLabel(selected), onValueChange = {}, readOnly = true, enabled = enabled,
            label = { Text("Tipo de servicio") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = colors
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MaintenanceType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(maintenanceTypeLabel(type)) },
                    onClick = { onSelected(type); expanded = false }, enabled = enabled
                )
            }
        }
    }
}

private fun maintenanceTypeLabel(type: MaintenanceType): String = when (type) {
    MaintenanceType.OIL_AND_FILTER -> "Aceite y filtro"
    MaintenanceType.OIL_ONLY -> "Solo aceite"
    MaintenanceType.FILTER_ONLY -> "Solo filtro"
    MaintenanceType.BRAKES -> "Frenos"
    MaintenanceType.TIRES -> "Neumáticos"
    MaintenanceType.BATTERY -> "Batería"
    MaintenanceType.COOLANT -> "Refrigerante"
    MaintenanceType.OTHER -> "Otro"
}
