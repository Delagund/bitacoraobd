package cl.cristian.bitacoraobd.ui.history

import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @Test
    fun cleanSessionsAreShownAndChangingVehicleDropsOldEmissions() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val diagnostics = HistoryDiagnostics()
            val viewModel = HistoryViewModel(diagnostics, AnalyzeDiagnosticHistoryUseCase(diagnostics))
            val clean = session(id = 1, vehicle = 1, codes = emptyList())
            diagnostics.rows(1).value = listOf(clean)

            viewModel.observeVehicle(1)
            advanceUntilIdle()
            assertEquals(listOf(clean), (viewModel.sessions.value as HistoryLoad.Ready).value)

            val secondVehicle = session(id = 2, vehicle = 2)
            diagnostics.rows(2).value = listOf(secondVehicle)
            viewModel.observeVehicle(2)
            advanceUntilIdle()
            diagnostics.rows(1).value = listOf(session(id = 3, vehicle = 1))
            advanceUntilIdle()

            assertEquals(listOf(secondVehicle), (viewModel.sessions.value as HistoryLoad.Ready).value)
            assertEquals(listOf(1L, 2L), diagnostics.observedVehicles)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun historyFailureCanBeRetried() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val diagnostics = HistoryDiagnostics()
            diagnostics.failObservations = 1
            val viewModel = HistoryViewModel(diagnostics, AnalyzeDiagnosticHistoryUseCase(diagnostics))

            viewModel.observeVehicle(1)
            advanceUntilIdle()
            assertEquals("offline", (viewModel.sessions.value as HistoryLoad.Failed).message)

            viewModel.retry(1)
            advanceUntilIdle()
            assertEquals(emptyList<DiagnosticSession>(), (viewModel.sessions.value as HistoryLoad.Ready).value)
            assertEquals(2, diagnostics.observationAttempts)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun detailRejectsAnotherVehiclesSessionAndUsesGenericGuidanceForUnknownCode() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val diagnostics = HistoryDiagnostics()
            val foreign = session(id = 4, vehicle = 2)
            diagnostics.byId[foreign.id] = foreign
            val viewModel = HistoryViewModel(diagnostics, AnalyzeDiagnosticHistoryUseCase(diagnostics))

            viewModel.loadDetail(vehicleId = 1, sessionId = foreign.id)
            advanceUntilIdle()
            assertTrue(viewModel.detail.value is HistoryLoad.Failed)
            assertEquals(0, diagnostics.historyQueries)

            val unknown = session(id = 5, vehicle = 1, codes = listOf("P0ABC"))
            diagnostics.byId[unknown.id] = unknown
            diagnostics.previousCodes = setOf("P0ABC")
            viewModel.loadDetail(vehicleId = 1, sessionId = unknown.id)
            advanceUntilIdle()

            val detail = (viewModel.detail.value as HistoryLoad.Ready).value
            assertEquals("P0ABC", detail.codes.single().code)
            assertEquals(false, detail.codes.single().guidance.isKnownInCatalog)
            assertTrue(detail.codes.single().isRecurrent)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private fun session(
    id: Long,
    vehicle: Long,
    codes: List<String> = listOf("P0ABC")
) = DiagnosticSession(id = id, vehicleId = vehicle, timestampMillis = id, mileageAtScanKm = 0, codes = codes)

private class HistoryDiagnostics : DiagnosticRepository {
    private val emissions = mutableMapOf<Long, MutableStateFlow<List<DiagnosticSession>>>()
    val observedVehicles = mutableListOf<Long>()
    val byId = mutableMapOf<Long, DiagnosticSession>()
    var failObservations = 0
    var observationAttempts = 0
    var historyQueries = 0
    var previousCodes = emptySet<String>()

    fun rows(vehicleId: Long) = emissions.getOrPut(vehicleId) { MutableStateFlow(emptyList()) }

    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> = flow {
        observedVehicles += vehicleId
        observationAttempts++
        if (failObservations > 0) {
            failObservations--
            error("offline")
        }
        emitAll(rows(vehicleId))
    }

    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = byId[sessionId]
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long = error("not used")
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long): Set<String> {
        historyQueries++
        return previousCodes
    }
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long): Map<String, Int> = emptyMap()
    override fun getDtcGuidance(code: String) = DtcGuidance(code, "Orientación general", "Desconocido", emptyList(), emptyList(), false)
}
