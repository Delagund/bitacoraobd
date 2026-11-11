package cl.cristian.bitacoraobd.ui.vehicles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.cristian.bitacoraobd.ui.AppSession
import cl.cristian.bitacoraobd.ui.VehicleRecovery
import cl.cristian.bitacoraobd.ui.components.GraphiteCard
import cl.cristian.bitacoraobd.ui.theme.PrototypeBad
import cl.cristian.bitacoraobd.ui.theme.PrototypeBadSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeCardContent
import cl.cristian.bitacoraobd.ui.theme.PrototypeGood
import cl.cristian.bitacoraobd.ui.theme.PrototypeGoodSoft
import cl.cristian.bitacoraobd.ui.theme.PrototypeOutline
import cl.cristian.bitacoraobd.ui.theme.PrototypeSurface

@Composable
fun VehiclesScreen(
    session: AppSession,
    viewModel: VehiclesViewModel
) {
    val state by session.state.collectAsState()
    var deleteId by remember { mutableStateOf<Long?>(null) }
    val draft = viewModel.draft

    LaunchedEffect(state.vehicleActionResult?.id) {
        val result = state.vehicleActionResult ?: return@LaunchedEffect
        if (result.action.name == "Save") viewModel.recordSaveResult(result.error == null && result.recovery == VehicleRecovery.None)
        if (result.error != null) return@LaunchedEffect
        when (result.action.name) {
            "Select", "Delete" -> Unit
            "OpenEditor" -> result.vehicle?.let(viewModel::load)
            "Save" -> if (result.recovery == VehicleRecovery.None) {
                viewModel.discard()
            }
        }
        session.consumeVehicleResult(result.id)
    }

    var confirmDiscard by remember { mutableStateOf(false) }
    if (draft != null) {
        VehicleForm(draft, viewModel, state.operation != null || state.sensitiveOperationRunning, state.vehicleActionResult?.error ?: state.error) {
            if (viewModel.isDirty(draft)) confirmDiscard = true else viewModel.discard()
        }
        if (confirmDiscard) AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Descartar cambios") },
            text = { Text("Se perderán los cambios del vehículo.") },
            confirmButton = {
                Button(onClick = { confirmDiscard = false; viewModel.discard() }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeBad, contentColor = PrototypeSurface)) { Text("Descartar") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Seguir editando") } }
        )
        return
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Vehículos", style = MaterialTheme.typography.headlineMedium)
            viewModel.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            when {
                state.vehicleLoadStatus.name == "Loading" -> Text("Cargando vehículos…")
                state.vehicleLoadStatus.name == "Failed" -> {
                    Text(state.error ?: "No se pudieron cargar los vehículos")
                    OutlinedButton(onClick = { session.refreshVehicleContext() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)) { Text("Reintentar") }
                }
                state.vehicles.isEmpty() -> {
                    Text("Aún no hay vehículos")
                    Button(onClick = viewModel::addVehicle, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Agregar vehículo") }
                }
                else -> {
                    if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.vehicles, key = { it.id }) { vehicle ->
                            GraphiteCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(listOf(vehicle.brand, vehicle.model).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Vehículo" }, style = MaterialTheme.typography.titleLarge)
                                    Text(buildString {
                                        if (vehicle.year > 0) append(vehicle.year)
                                        append(if (isNotEmpty()) " · " else "")
                                        append("${vehicle.currentMileageKm} km")
                                    }, color = PrototypeCardContent.copy(alpha = 0.75f))
                                    if (vehicle.id == state.activeVehicleId) {
                                        Text("Activo", color = PrototypeGoodSoft, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (vehicle.id != state.activeVehicleId) {
                                            OutlinedButton(onClick = {
                                                session.selectVehicle(vehicle.id)
                                            }, colors = ButtonDefaults.outlinedButtonColors(contentColor = PrototypeCardContent), border = BorderStroke(1.dp, PrototypeOutline)) { Text("Usar") }
                                        }
                                        TextButton(onClick = { viewModel.editVehicle(vehicle.id) }, colors = ButtonDefaults.textButtonColors(contentColor = PrototypeCardContent)) { Text("Editar") }
                                        TextButton(onClick = { deleteId = vehicle.id }, colors = ButtonDefaults.textButtonColors(contentColor = PrototypeBadSoft)) { Text("Eliminar") }
                                    }
                                }
                            }
                        }
                    }
                    Button(onClick = viewModel::addVehicle, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Agregar vehículo") }
                }
            }
            state.vehicleActionResult?.let { result ->
                if (result.error != null) {
                    Text(result.error, color = MaterialTheme.colorScheme.error)
                    if (result.recovery == VehicleRecovery.RetrySelection && result.vehicleId != null) {
                        Button(onClick = { session.retryVehicleSelection(result.vehicleId) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Reintentar selección") }
                    } else if (result.recovery == VehicleRecovery.RefreshOnly || result.recovery == VehicleRecovery.Reconcile) {
                        OutlinedButton(onClick = { session.retryVehicleRecovery(result.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)) { Text("Actualizar lista") }
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Eliminar vehículo") },
            text = { Text("Se eliminarán también sus diagnósticos y mantenimientos. ¿Continuar?") },
            confirmButton = {
                Button(onClick = { deleteId = null; session.deleteVehicle(id) }, colors = ButtonDefaults.buttonColors(containerColor = PrototypeBad, contentColor = PrototypeSurface)) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun VehicleForm(draft: VehicleDraft, viewModel: VehiclesViewModel, saving: Boolean, backendError: String?, onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(if (draft.id == 0L) "Agregar vehículo" else "Editar vehículo", style = MaterialTheme.typography.headlineMedium)
        viewModel.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        backendError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        listOf(
            "brand" to ("Marca" to draft.brand),
            "model" to ("Modelo" to draft.model),
            "year" to ("Año (opcional)" to draft.year),
            "fuel" to ("Combustible (opcional)" to draft.fuelType),
            "engine" to ("Motor (opcional)" to draft.engineDescription),
            "mileage" to ("Kilometraje (" + (if (draft.distanceUnit == cl.cristian.bitacoraobd.domain.model.DistanceUnit.MILES) "mi" else "km") + ")" to draft.mileageKm)
        ).forEach { (field, pair) ->
            OutlinedTextField(
                value = pair.second,
                onValueChange = { viewModel.edit(draft, field, it) },
                label = { Text(pair.first) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        draft.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !saving, onClick = viewModel::save, colors = ButtonDefaults.buttonColors(containerColor = PrototypeGood, contentColor = PrototypeSurface)) { Text("Guardar") }
            TextButton(enabled = !saving, onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Cancelar") }
        }
    }
}
