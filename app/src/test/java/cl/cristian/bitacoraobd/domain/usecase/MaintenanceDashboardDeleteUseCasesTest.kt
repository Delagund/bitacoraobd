package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdLiveMetrics
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MaintenanceDashboardDeleteUseCasesTest {
    @Test
    fun saveMaintenanceHistoricalMileageRequiresConfirmationWithoutWriting() = runBlocking {
        val vehicle = vehicle(1, 50_000)
        val maintenance = TestMaintenance()
        val record = MaintenanceRecord(
            vehicleId = vehicle.id, serviceType = MaintenanceType.OIL_ONLY,
            performedDate = LocalDate.of(2025, 1, 1), mileageAtServiceKm = 40_000
        )

        val result = SaveMaintenanceUseCase(TestVehicles(listOf(vehicle)), maintenance)(record)

        assertTrue(result is SaveMaintenanceResult.RequiresHistoricalMileageConfirmation)
        assertEquals(0, maintenance.savedRecords.size)
    }

    @Test
    fun deleteActiveVehicleSelectsRemainingVehicle() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1), vehicle(2)))
        val preferences = TestPreferences(1L)
        val result = DeleteVehicleUseCase(vehicles, preferences, TestGateway())(1L)

        assertEquals(DeleteVehicleResult.Deleted, result)
        assertEquals(2L, preferences.activeVehicleId.first())
    }

    @Test
    fun deleteVehicleIsBlockedWhileConnected() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1)))
        val preferences = TestPreferences(1L)
        val gateway = TestGateway().apply {
            connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
        }

        assertEquals(DeleteVehicleResult.Connected, DeleteVehicleUseCase(vehicles, preferences, gateway)(1L))
        assertEquals(listOf(1L), vehicles.observeVehicles().first().map { it.id })
        assertEquals(1L, preferences.activeVehicleId.first())
    }

    @Test
    fun dashboardWithNoVehiclesReturnsEmptyState() = runBlocking {
        val vehicles = TestVehicles(emptyList())
        val preferences = TestPreferences(null)
        val gateway = TestGateway()
        val diagnostics = TestDiagnostics()
        val maintenance = TestMaintenance()
        val useCase = dashboard(vehicles, preferences, gateway, diagnostics, maintenance)

        assertEquals(DashboardState.NoVehicles, useCase().first())
    }

    @Test
    fun dashboardFallsBackToExistingVehicleWhenSavedSelectionIsStale() = runBlocking {
        val existing = vehicle(2)
        val useCase = dashboard(
            TestVehicles(listOf(existing)), TestPreferences(999L), TestGateway(),
            TestDiagnostics(), TestMaintenance()
        )

        val state = useCase().first() as DashboardState.Ready
        assertEquals(existing.id, state.summary.activeVehicle.id)
    }
    @Test
    fun dashboardSwitchesObservationWhenActiveVehicleChanges() = runTest {
        val vehicles = TestVehicles(listOf(vehicle(1), vehicle(2)))
        val preferences = TestPreferences(1L)
        val diagnostics = TestDiagnostics()
        val emissions = mutableListOf<DashboardState>()
        val job = launch {
            dashboard(vehicles, preferences, TestGateway(), diagnostics, TestMaintenance())()
                .take(2).toList(emissions)
        }
        runCurrent()
        preferences.activeIdFlow.value = 2L
        runCurrent()
        job.cancel()

        val summaries = emissions.map { (it as DashboardState.Ready).summary }
        assertEquals(listOf(1L, 2L), summaries.map { it.activeVehicle.id })
    }

    @Test
    fun dashboardDeduplicatesCodesAndClearsRecurrentAlertAfterCleanScan() = runTest {
        val diagnostics = TestDiagnostics().apply {
            previous = setOf("P0123")
            counts = mapOf("P0123" to 2)
            sessions[1L] = MutableStateFlow(
                listOf(cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                    id = 1, vehicleId = 1, timestampMillis = 1, mileageAtScanKm = 40_000,
                    codes = listOf("P0123", "P0123")
                ))
            )
        }
        val emissions = mutableListOf<DashboardState>()
        val job = launch {
            dashboard(
                TestVehicles(listOf(vehicle(1))), TestPreferences(1L), TestGateway(),
                diagnostics, TestMaintenance()
            )().take(2).toList(emissions)
        }
        runCurrent()
        diagnostics.sessions.getValue(1L).value = listOf(
            cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                id = 2, vehicleId = 1, timestampMillis = 2, mileageAtScanKm = 40_000,
                codes = emptyList()
            )
        )
        runCurrent()
        job.cancel()

        val summaries = emissions.map { (it as DashboardState.Ready).summary }
        assertEquals(1, summaries.first().alerts.size)
        assertTrue(summaries.first().alerts.single() is cl.cristian.bitacoraobd.domain.model.VehicleAlert.RecurrentDtcAlert)
        assertTrue(summaries.last().alerts.isEmpty())
    }
}

private fun dashboard(
    vehicles: VehicleRepository,
    preferences: AppPreferencesRepository,
    gateway: ObdGateway,
    diagnostics: DiagnosticRepository,
    maintenance: MaintenanceRepository
) = GetDashboardDataUseCase(
    vehicles, preferences, diagnostics, maintenance, gateway,
    GetMaintenanceStatusUseCase(vehicles, maintenance, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
    AnalyzeDiagnosticHistoryUseCase(diagnostics)
)

private fun vehicle(id: Long, mileage: Int = 40_000) = Vehicle(
    id = id, brand = "A", model = "B", year = 2020, fuelType = "Gasoline",
    engineDescription = "1.6", currentMileageKm = mileage
)

private class TestVehicles(initial: List<Vehicle>) : VehicleRepository {
    val values = MutableStateFlow(initial)
    override fun observeVehicles(): Flow<List<Vehicle>> = values
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> = values.let { flow ->
        flowOf(flow.value.firstOrNull { it.id == vehicleId })
    }
    override suspend fun getVehicleById(vehicleId: Long) = values.value.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) {
        values.value = values.value.filterNot { it.id == vehicleId }
    }
}

private class TestPreferences(activeId: Long?) : AppPreferencesRepository {
    val activeIdFlow = MutableStateFlow(activeId)
    override val activeVehicleId: MutableStateFlow<Long?> = activeIdFlow
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig())
    override suspend fun setActiveVehicleId(vehicleId: Long?) { activeVehicleId.value = vehicleId }
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) { obdConnectionConfig.value = config }
}

private class TestMaintenance : MaintenanceRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> = flowOf(emptyList())
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = null
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long {
        savedRecords += record
        return savedRecords.size.toLong()
    }
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> = emptyList()
}

private class TestDiagnostics : DiagnosticRepository {
    val sessions = mutableMapOf<Long, MutableStateFlow<List<cl.cristian.bitacoraobd.domain.model.DiagnosticSession>>>()
    var previous = emptySet<String>()
    var counts = emptyMap<String, Int>()
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<cl.cristian.bitacoraobd.domain.model.DiagnosticSession>> =
        sessions.getOrPut(vehicleId) { MutableStateFlow(emptyList()) }
    override suspend fun getLatestSession(vehicleId: Long) = null
    override suspend fun getSessionById(sessionId: Long) = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: cl.cristian.bitacoraobd.domain.model.DiagnosticSession) = 1L
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) = previous
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = counts
    override fun getDtcGuidance(code: String) = cl.cristian.bitacoraobd.domain.model.DtcGuidance(code, code, "", emptyList(), emptyList(), false)
}

private class TestGateway : ObdGateway {
    override val connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    override suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> = ObdResult.Success(Unit)
    override suspend fun disconnect() = Unit
    override suspend fun readLiveMetrics(): ObdResult<ObdLiveMetrics> = ObdResult.Success(ObdLiveMetrics(null, null, null, 0))
    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> = ObdResult.Success(emptyList())
}
