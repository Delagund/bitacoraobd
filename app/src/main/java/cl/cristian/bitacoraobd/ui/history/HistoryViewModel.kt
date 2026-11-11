package cl.cristian.bitacoraobd.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cristian.bitacoraobd.domain.model.DiagnosedDtc
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoryLoad<out T> {
    data object Loading : HistoryLoad<Nothing>
    data class Ready<T>(val value: T) : HistoryLoad<T>
    data class Failed(val message: String) : HistoryLoad<Nothing>
}

data class DiagnosticDetail(val session: DiagnosticSession, val codes: List<DiagnosedDtc>)

class HistoryViewModel(
    private val diagnostics: DiagnosticRepository,
    private val analyzeHistory: AnalyzeDiagnosticHistoryUseCase
) : ViewModel() {
    private val mutableSessions = MutableStateFlow<HistoryLoad<List<DiagnosticSession>>>(HistoryLoad.Loading)
    val sessions: StateFlow<HistoryLoad<List<DiagnosticSession>>> = mutableSessions.asStateFlow()
    private val mutableDetail = MutableStateFlow<HistoryLoad<DiagnosticDetail>?>(null)
    val detail: StateFlow<HistoryLoad<DiagnosticDetail>?> = mutableDetail.asStateFlow()
    private var observeJob: Job? = null
    private var contextVehicleId: Long? = null
    private var detailRequest = 0

    fun observeVehicle(vehicleId: Long) {
        if (contextVehicleId == vehicleId && observeJob?.isActive == true) return
        contextVehicleId = vehicleId
        observeJob?.cancel()
        mutableSessions.value = HistoryLoad.Loading
        mutableDetail.value = null
        observeJob = viewModelScope.launch {
            try {
                diagnostics.observeSessionsByVehicle(vehicleId).collect { rows ->
                    if (contextVehicleId == vehicleId) mutableSessions.value = HistoryLoad.Ready(rows)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (contextVehicleId == vehicleId) mutableSessions.value =
                    HistoryLoad.Failed(error.message ?: "No se pudo cargar el historial")
            }
        }
    }

    fun retry(vehicleId: Long) {
        contextVehicleId = null
        observeVehicle(vehicleId)
    }

    fun loadDetail(vehicleId: Long, sessionId: Long) {
        if (contextVehicleId != vehicleId) {
            observeJob?.cancel()
            observeJob = null
            contextVehicleId = vehicleId
            mutableSessions.value = HistoryLoad.Loading
        }
        val request = ++detailRequest
        mutableDetail.value = HistoryLoad.Loading
        viewModelScope.launch {
            try {
                val session = diagnostics.getSessionById(sessionId)
                    ?: error("No se encontró el diagnóstico solicitado")
                require(session.vehicleId == vehicleId) { "Esta sesión no pertenece al vehículo seleccionado" }
                val detail = DiagnosticDetail(session, analyzeHistory(session))
                if (request == detailRequest && contextVehicleId == vehicleId) mutableDetail.value = HistoryLoad.Ready(detail)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request == detailRequest && contextVehicleId == vehicleId) mutableDetail.value =
                    HistoryLoad.Failed(error.message ?: "No se pudo cargar el diagnóstico")
            }
        }
    }

    fun retryDetail(vehicleId: Long, sessionId: Long) = loadDetail(vehicleId, sessionId)

    class Factory(
        private val diagnostics: DiagnosticRepository,
        private val analyzeHistory: AnalyzeDiagnosticHistoryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HistoryViewModel::class.java))
            return HistoryViewModel(diagnostics, analyzeHistory) as T
        }
    }
}
