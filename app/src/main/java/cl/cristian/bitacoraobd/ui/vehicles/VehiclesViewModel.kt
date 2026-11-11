package cl.cristian.bitacoraobd.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import java.math.BigDecimal
import java.math.RoundingMode
import cl.cristian.bitacoraobd.ui.AppSession
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal fun mileageForSave(input: String, unit: DistanceUnit, originalKm: Int?, edited: Boolean): Int? =
    if (!edited && originalKm != null) originalKm else mileageToKilometers(input, unit)

internal fun mileageToKilometers(input: String, unit: DistanceUnit): Int? {
    val value = input.trim()
    if (value.isEmpty() || (value.contains(',') && value.contains('.'))) return null
    val normalized = value.replace(',', '.')
    val pattern = if (unit == DistanceUnit.MILES) Regex("\\d+(\\.\\d)?") else Regex("\\d+")
    if (!pattern.matches(normalized)) return null
    return try {
        val kilometers = BigDecimal(normalized).let {
            if (unit == DistanceUnit.MILES) it.multiply(BigDecimal("1.609344")) else it
        }.setScale(0, RoundingMode.HALF_UP).intValueExact()
        kilometers.takeIf { it >= 0 }
    } catch (_: ArithmeticException) {
        null
    }
}

data class VehicleDraft(
    val id: Long = 0,
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val fuelType: String = "",
    val engineDescription: String = "",
    val mileageKm: String = "0",
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val mileageEdited: Boolean = false,
    val original: Vehicle? = null,
    val error: String? = null
)

class VehiclesViewModel(private val session: AppSession, private val preferences: AppPreferencesRepository) : ViewModel() {
    var draft: VehicleDraft? by mutableStateOf(null)
        private set

    var formError by mutableStateOf<String?>(null)
        private set
    var pendingLeaveRoute by mutableStateOf<String?>(null)
        private set
    var queuedExitRoute by mutableStateOf<String?>(null)
        private set
    var queuedExitSaveSucceeded by mutableStateOf<Boolean?>(null)
        private set

    fun addVehicle() {
        formError = null
        viewModelScope.launch {
            try {
                draft = VehicleDraft(distanceUnit = preferences.settings.first().distanceUnit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                formError = error.message ?: "No se pudieron cargar las unidades"
            }
        }
    }

    fun editVehicle(vehicleId: Long) { session.openVehicleEditor(vehicleId) }

    fun edit(draft: VehicleDraft, field: String, value: String) {
        this.draft = when (field) {
            "brand" -> draft.copy(brand = value, error = null)
            "model" -> draft.copy(model = value, error = null)
            "year" -> draft.copy(year = value, error = null)
            "fuel" -> draft.copy(fuelType = value, error = null)
            "engine" -> draft.copy(engineDescription = value, error = null)
            else -> draft.copy(mileageKm = value, mileageEdited = value != draft.mileageKm, error = null)
        }
    }

    fun load(vehicle: Vehicle) {
        formError = null
        viewModelScope.launch {
            try {
                val unit = preferences.settings.first().distanceUnit
                val displayMileage = if (unit == DistanceUnit.MILES) {
                    BigDecimal(vehicle.currentMileageKm).divide(BigDecimal("1.609344"), 1, RoundingMode.HALF_UP).toPlainString()
                } else vehicle.currentMileageKm.toString()
                draft = VehicleDraft(
                    id = vehicle.id, brand = vehicle.brand, model = vehicle.model,
                    year = vehicle.year.takeIf { it > 0 }?.toString().orEmpty(),
                    fuelType = vehicle.fuelType, engineDescription = vehicle.engineDescription,
                    mileageKm = displayMileage, distanceUnit = unit, original = vehicle
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                formError = error.message ?: "No se pudieron cargar las unidades"
            }
        }
    }

    fun save() {
        val current = draft ?: return
        val year = current.year.toIntOrNull() ?: if (current.year.isBlank()) 0 else null
        val mileage = mileageForSave(current.mileageKm, current.distanceUnit, current.original?.currentMileageKm, current.mileageEdited)
        if (year == null || mileage == null || mileage < 0) {
            draft = current.copy(error = "Revisa el año y el kilometraje")
            return
        }
        session.saveVehicle(
            Vehicle(
                id = current.id, brand = current.brand, model = current.model, year = year,
                fuelType = current.fuelType, engineDescription = current.engineDescription,
                currentMileageKm = mileage
            ),
            current.original
        )
    }

    fun isDirty(draft: VehicleDraft): Boolean {
        val original = draft.original ?: return draft.brand.isNotBlank() || draft.model.isNotBlank() ||
            draft.year.isNotBlank() || draft.fuelType.isNotBlank() || draft.engineDescription.isNotBlank() ||
            draft.mileageKm != "0"
        val year = if (draft.year.isBlank()) 0 else draft.year.toIntOrNull() ?: return true
        val mileage = mileageForSave(draft.mileageKm, draft.distanceUnit, original.currentMileageKm, draft.mileageEdited) ?: return true
        return draft.brand != original.brand || draft.model != original.model ||
            year != original.year || draft.fuelType != original.fuelType ||
            draft.engineDescription != original.engineDescription ||
            (draft.mileageEdited && mileageToKilometers(draft.mileageKm, draft.distanceUnit) != original.currentMileageKm)
    }

    private fun isBusy() = session.state.value.sensitiveOperationRunning || session.state.value.operation == "vehicle-save"

    fun requestBackFromForm() {
        if (isBusy()) { queuedExitRoute = "vehicles"; queuedExitSaveSucceeded = null; return }
        val current = draft ?: return
        if (isDirty(current)) pendingLeaveRoute = "vehicles" else draft = null
    }

    fun requestLeave(route: String, requestExit: (String) -> Unit) {
        if (isBusy()) { queuedExitRoute = route; queuedExitSaveSucceeded = null; return }
        val current = draft
        if (current != null && isDirty(current)) pendingLeaveRoute = route else {
            draft = null
            requestExit(route)
        }
    }

    fun confirmLeave(requestExit: (String) -> Unit) {
        if (isBusy()) return
        val route = pendingLeaveRoute ?: return
        pendingLeaveRoute = null
        draft = null
        if (route != "vehicles") requestExit(route)
    }

    fun recordSaveResult(succeeded: Boolean) {
        if (queuedExitRoute != null) queuedExitSaveSucceeded = succeeded
    }

    fun settleQueuedExit() {
        val route = queuedExitRoute ?: return
        val succeeded = queuedExitSaveSucceeded ?: return
        queuedExitRoute = null
        queuedExitSaveSucceeded = null
        if (!succeeded) {
            pendingLeaveRoute = route
            return
        }
        draft = null
        if (route != "vehicles") session.requestExit(cl.cristian.bitacoraobd.ui.ExitAction.ToRoute(route))
    }

    fun keepEditing() { pendingLeaveRoute = null }

    fun discard() { draft = null }

    class Factory(private val session: AppSession, private val preferences: AppPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(VehiclesViewModel::class.java))
            return VehiclesViewModel(session, preferences) as T
        }
    }
}
