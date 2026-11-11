package cl.cristian.bitacoraobd.ui.maintenance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceResult
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceUseCase
import cl.cristian.bitacoraobd.ui.vehicles.mileageForSave
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class MaintenanceRow(val record: MaintenanceRecord, val status: MaintenanceStatus?)

data class MaintenanceDraft(
    val id: Long = 0L,
    val vehicleId: Long,
    val type: MaintenanceType = MaintenanceType.OIL_AND_FILTER,
    val performedDate: String = LocalDate.now().toString(),
    val mileage: String = "",
    val mileageOriginalKm: Int? = null,
    val mileageEdited: Boolean = true,
    val notes: String = "",
    val nextDueDate: String = "",
    val nextDueMileage: String = "",
    val nextDueMileageOriginalKm: Int? = null,
    val nextDueMileageEdited: Boolean = true,
    val unit: DistanceUnit = DistanceUnit.KILOMETERS,
    val original: MaintenanceRecord? = null,
    val error: String? = null
)

class MaintenanceViewModel(
    private val maintenance: MaintenanceRepository,
    private val getStatus: GetMaintenanceStatusUseCase,
    private val saveRecord: SaveMaintenanceUseCase,
    private val preferences: AppPreferencesRepository
) : ViewModel() {
    private val mutableRows = MutableStateFlow<LoadState<List<MaintenanceRow>>>(LoadState.Loading)
    val rows: StateFlow<LoadState<List<MaintenanceRow>>> = mutableRows.asStateFlow()
    var draft: MaintenanceDraft? by mutableStateOf(null)
        private set
    var saving by mutableStateOf(false)
        private set
    var historicalMileage by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var pendingLeave by mutableStateOf<String?>(null)
        private set
    var queuedLeave by mutableStateOf<String?>(null)
        private set
    var queuedLeaveSucceeded by mutableStateOf<Boolean?>(null)
        private set
    private var vehicleId: Long? = null
    private var observeJob: Job? = null
    private var latestRecords: List<MaintenanceRecord>? = null
    private var editorLoadRequest = 0L
    private var statusRequest = 0L
    private var saveRequest = 0L

    fun observe(vehicle: Long) {
        if (vehicleId == vehicle && observeJob?.isActive == true) {
            refreshStatuses(vehicle)
            return
        }
        invalidateEditorLoad()
        statusRequest++
        vehicleId = vehicle
        latestRecords = null
        observeJob?.cancel()
        draft = null
        mutableRows.value = LoadState.Loading
        observeJob = viewModelScope.launch {
            try {
                maintenance.observeMaintenanceByVehicle(vehicle).collect { records ->
                    if (vehicleId != vehicle) return@collect
                    latestRecords = records
                    val request = ++statusRequest
                    val current = getStatus(vehicle).associateBy { it.record.id }
                    if (vehicleId == vehicle && request == statusRequest) {
                        mutableRows.value = LoadState.Ready(records.map { MaintenanceRow(it, current[it.id]?.status) })
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (vehicleId == vehicle) mutableRows.value =
                    LoadState.Failed(failure.message ?: "No se pudo cargar el mantenimiento")
            }
        }
    }

    private fun refreshStatuses(vehicle: Long) {
        val request = ++statusRequest
        viewModelScope.launch {
            try {
                val current = getStatus(vehicle).associateBy { it.record.id }
                if (vehicleId == vehicle && request == statusRequest) {
                    val records = (mutableRows.value as? LoadState.Ready)?.value
                        ?: latestRecords?.map { MaintenanceRow(it, current[it.id]?.status) }
                        ?: return@launch
                    mutableRows.value = LoadState.Ready(records.map { MaintenanceRow(it.record, current[it.record.id]?.status) })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (vehicleId == vehicle && request == statusRequest) {
                    mutableRows.value = LoadState.Failed(failure.message ?: "No se pudo cargar el mantenimiento")
                }
            }
        }
    }

    private fun invalidateEditorLoad() {
        editorLoadRequest++
    }

    fun onScreenClosed(vehicle: Long) {
        if (vehicleId == null || vehicleId == vehicle) cancel()
    }

    fun retry(vehicle: Long) { vehicleId = null; observe(vehicle) }

    fun add(vehicle: Long) {
        val request = ++editorLoadRequest
        viewModelScope.launch {
            try {
                val unit = preferences.settings.first().distanceUnit
                if (isCurrentEditorLoad(request, vehicle)) {
                    draft = MaintenanceDraft(vehicleId = vehicle, unit = unit)
                    error = null
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (isCurrentEditorLoad(request, vehicle)) {
                    error = failure.message ?: "No se pudieron cargar las unidades"
                }
            }
        }
    }

    fun edit(vehicle: Long, recordId: Long) {
        val request = ++editorLoadRequest
        viewModelScope.launch {
            try {
                val record = maintenance.getMaintenanceById(recordId)
                    ?: error("No existe el registro de mantenimiento")
                require(record.vehicleId == vehicle) { "El registro pertenece a otro vehículo" }
                val unit = preferences.settings.first().distanceUnit
                if (isCurrentEditorLoad(request, vehicle)) {
                    draft = MaintenanceDraft(
                        id = record.id, vehicleId = record.vehicleId, type = record.serviceType,
                        performedDate = record.performedDate.toString(),
                        mileage = formatMileage(record.mileageAtServiceKm, unit),
                        mileageOriginalKm = record.mileageAtServiceKm, mileageEdited = false,
                        notes = record.notes, nextDueDate = record.nextDueDate?.toString().orEmpty(),
                        nextDueMileage = record.nextDueMileageKm?.let { formatMileage(it, unit) }.orEmpty(),
                        nextDueMileageOriginalKm = record.nextDueMileageKm, nextDueMileageEdited = false,
                        unit = unit, original = record
                    )
                    error = null
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (isCurrentEditorLoad(request, vehicle)) {
                    error = failure.message ?: "No se pudo abrir el registro"
                }
            }
        }
    }

    private fun isCurrentEditorLoad(request: Long, vehicle: Long): Boolean =
        request == editorLoadRequest && (vehicleId == null || vehicleId == vehicle)

    fun update(transform: (MaintenanceDraft) -> MaintenanceDraft) {
        draft = draft?.let { transform(it).copy(error = null) }
        historicalMileage = null
        error = null
    }

    fun isDirty(value: MaintenanceDraft): Boolean {
        val original = value.original ?: return true
        return value.type != original.serviceType || value.performedDate != original.performedDate.toString() ||
            value.notes != original.notes || value.nextDueDate != original.nextDueDate?.toString().orEmpty() ||
            (value.mileageEdited && canonicalMileage(value.mileage, value.unit, value.mileageOriginalKm, true) != original.mileageAtServiceKm) ||
            (value.nextDueMileageEdited && (value.nextDueMileage.takeIf(String::isNotBlank)?.let {
                canonicalMileage(it, value.unit, null, true)
            }) != original.nextDueMileageKm)
    }

    fun cancel() {
        invalidateEditorLoad()
        draft = null
        historicalMileage = null
        error = null
    }

    fun requestLeave(target: String) {
        if (saving) {
            queuedLeave = target
            queuedLeaveSucceeded = null
            return
        }
        val current = draft
        if (current != null && isDirty(current)) pendingLeave = target
        else { cancel(); queuedLeave = target; queuedLeaveSucceeded = true }
    }

    fun consumeQueuedLeave(): Pair<String, Boolean>? {
        val target = queuedLeave ?: return null
        val succeeded = queuedLeaveSucceeded ?: return null
        queuedLeave = null
        queuedLeaveSucceeded = null
        return target to succeeded
    }

    fun confirmLeave() { cancel(); pendingLeave = null }
    fun keepEditing() { pendingLeave = null }

    fun save(confirmHistorical: Boolean = false) {
        val value = draft ?: return
        if (saving) return
        val performedDate = runCatching { LocalDate.parse(value.performedDate) }.getOrNull()
        val dueDate = value.nextDueDate.takeIf(String::isNotBlank)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val mileage = canonicalMileage(value.mileage, value.unit, value.mileageOriginalKm, value.mileageEdited)
        val dueMileage = value.nextDueMileage.takeIf(String::isNotBlank)?.let {
            canonicalMileage(it, value.unit, value.nextDueMileageOriginalKm, value.nextDueMileageEdited)
        }
        if (performedDate == null || (value.nextDueDate.isNotBlank() && dueDate == null) ||
            mileage == null || (value.nextDueMileage.isNotBlank() && dueMileage == null)
        ) {
            draft = value.copy(error = "Revisa la fecha y los kilometrajes")
            return
        }
        val record = runCatching {
            MaintenanceRecord(value.id, value.vehicleId, value.type, performedDate, mileage, value.notes, dueDate, dueMileage)
        }.getOrElse {
            draft = value.copy(error = it.message ?: "Revisa los datos")
            return
        }
        val request = ++saveRequest
        saving = true
        error = null
        viewModelScope.launch {
            var saved = false
            try {
                when (val result = saveRecord(record, confirmHistorical)) {
                    is SaveMaintenanceResult.Saved -> {
                        saved = true
                        if (request == saveRequest) { draft = null; historicalMileage = null }
                    }
                    is SaveMaintenanceResult.RequiresHistoricalMileageConfirmation -> {
                        if (request == saveRequest) historicalMileage = result.enteredMileageKm to result.currentMileageKm
                    }
                    is SaveMaintenanceResult.Failed -> if (request == saveRequest) {
                        draft = value.copy(error = result.message)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (request == saveRequest) draft = value.copy(error = failure.message ?: "No se pudo guardar")
            } finally {
                if (request == saveRequest) {
                    saving = false
                    if (queuedLeave != null) queuedLeaveSucceeded = saved
                }
            }
        }
    }

    fun confirmHistoricalSave() { historicalMileage = null; save(confirmHistorical = true) }
    fun keepHistoricalDraft() { historicalMileage = null }


    class Factory(
        private val maintenance: MaintenanceRepository,
        private val getStatus: GetMaintenanceStatusUseCase,
        private val saveRecord: SaveMaintenanceUseCase,
        private val preferences: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MaintenanceViewModel::class.java))
            return MaintenanceViewModel(maintenance, getStatus, saveRecord, preferences) as T
        }
    }
}

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Failed(val message: String) : LoadState<Nothing>
}

private fun canonicalMileage(input: String, unit: DistanceUnit, originalKm: Int?, edited: Boolean): Int? =
    mileageForSave(input, unit, originalKm, edited)

private fun formatMileage(km: Int, unit: DistanceUnit): String =
    if (unit == DistanceUnit.KILOMETERS) km.toString()
    else BigDecimal(km).divide(BigDecimal("1.609344"), 1, RoundingMode.HALF_UP).toPlainString()
