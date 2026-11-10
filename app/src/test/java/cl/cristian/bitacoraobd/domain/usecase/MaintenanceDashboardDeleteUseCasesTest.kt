package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.model.AppSettings
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
    fun saveMaintenanceConfirmedHistoricalMileageSavesRecord() = runBlocking {
        val vehicle = vehicle(1, 50_000)
        val maintenance = TestMaintenance()
        val record = MaintenanceRecord(
            vehicleId = vehicle.id, serviceType = MaintenanceType.OIL_ONLY,
            performedDate = LocalDate.of(2025, 1, 1), mileageAtServiceKm = 40_000
        )

        val result = SaveMaintenanceUseCase(TestVehicles(listOf(vehicle)), maintenance)(
            record, confirmHistoricalMileage = true
        )

        assertEquals(SaveMaintenanceResult.Saved(1L), result)
        assertEquals(listOf(record), maintenance.savedRecords)
    }

    @Test
    fun saveMaintenanceHigherMileageSavesWithoutConfirmation() = runBlocking {
        val vehicle = vehicle(1, 50_000)
        val maintenance = TestMaintenance()
        val record = MaintenanceRecord(
            vehicleId = vehicle.id, serviceType = MaintenanceType.FILTER_ONLY,
            performedDate = LocalDate.of(2025, 1, 1), mileageAtServiceKm = 60_000
        )

        val result = SaveMaintenanceUseCase(TestVehicles(listOf(vehicle)), maintenance)(record)

        assertEquals(SaveMaintenanceResult.Saved(1L), result)
        assertEquals(listOf(record), maintenance.savedRecords)
    }

    @Test
    fun maintenanceRejectsInvalidDatesAndMileageWithoutWriting() = runBlocking {
        val maintenance = TestMaintenance()
        val clock = Clock.fixed(Instant.parse("2025-01-10T00:00:00Z"), ZoneOffset.UTC)
        val useCase = SaveMaintenanceUseCase(TestVehicles(listOf(vehicle(1))), maintenance, clock)
        val record = MaintenanceRecord(vehicleId = 1, serviceType = MaintenanceType.BRAKES,
            performedDate = LocalDate.of(2025, 1, 10), mileageAtServiceKm = 40_000)
        listOf(record.copy(performedDate = LocalDate.of(2025, 1, 11)),
            record.copy(nextDueDate = LocalDate.of(2025, 1, 9)),
            record.copy(nextDueMileageKm = 39_999)).forEach {
            assertTrue(useCase(it) is SaveMaintenanceResult.Failed)
        }
        assertTrue(maintenance.savedRecords.isEmpty())
        assertTrue(useCase(record.copy(nextDueDate = record.performedDate, nextDueMileageKm = 40_000)) is SaveMaintenanceResult.Saved)
    }

    @Test
    fun deleteFailureAfterSelectionPreservesVehicleAndExposesFailure() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1), vehicle(2))).apply { failDelete = true }
        val preferences = TestPreferences(1L)
        assertEquals(DeleteVehicleResult.Failed, DeleteVehicleUseCase(vehicles, preferences, TestGateway())(1L))
        assertEquals(2L, preferences.activeVehicleId.first())
        assertEquals(listOf(1L, 2L), vehicles.values.value.map { it.id })
    }

    @Test
    fun deletePropagatesCancellation() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1))).apply { cancelDelete = true }
        val cancellation = runCatching { DeleteVehicleUseCase(vehicles, TestPreferences(1L), TestGateway())(1L) }.exceptionOrNull()
        assertTrue(cancellation is kotlinx.coroutines.CancellationException)
        assertEquals(listOf(1L), vehicles.values.value.map { it.id })
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
    fun deleteNonActiveKeepsSelectionAndLastActiveClearsIt() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1), vehicle(2)))
        val preferences = TestPreferences(1L)
        val useCase = DeleteVehicleUseCase(vehicles, preferences, TestGateway())

        assertEquals(DeleteVehicleResult.Deleted, useCase(2L))
        assertEquals(1L, preferences.activeVehicleId.first())
        assertEquals(DeleteVehicleResult.Deleted, useCase(1L))
        assertEquals(emptyList<Long>(), vehicles.values.value.map { it.id })
        assertEquals(null, preferences.activeVehicleId.first())
    }

    @Test
    fun deleteMissingVehicleDoesNotChangeSelectionOrRows() = runBlocking {
        val vehicles = TestVehicles(listOf(vehicle(1)))
        val preferences = TestPreferences(1L)

        assertEquals(DeleteVehicleResult.NotFound, DeleteVehicleUseCase(vehicles, preferences, TestGateway())(2L))
        assertEquals(listOf(1L), vehicles.values.value.map { it.id })
        assertEquals(1L, preferences.activeVehicleId.first())
    }

    @Test
    fun dashboardReactsToVehicleConnectionAndMaintenanceUpdates() = runTest {
        val vehicles = TestVehicles(listOf(vehicle(1, 40_000)))
        val preferences = TestPreferences(1L)
        val gateway = TestGateway()
        val maintenance = TestMaintenance()
        val emissions = mutableListOf<DashboardState>()
        val job = launch {
            dashboard(vehicles, preferences, gateway, TestDiagnostics(), maintenance)()
                .take(4).toList(emissions)
        }
        runCurrent()
        vehicles.values.value = listOf(vehicle(1, 45_000))
        runCurrent()
        gateway.connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
        runCurrent()
        maintenance.records[1L]!!.value = listOf(
            MaintenanceRecord(
                vehicleId = 1L, serviceType = MaintenanceType.OIL_ONLY,
                performedDate = LocalDate.of(1969, 1, 1), mileageAtServiceKm = 40_000,
                nextDueDate = LocalDate.of(1969, 2, 1)
            )
        )
        runCurrent()
        job.cancel()

        val summaries = emissions.map { (it as DashboardState.Ready).summary }
        assertEquals(4, summaries.size)
        assertEquals(40_000, summaries[0].activeVehicle.currentMileageKm)
        assertEquals(45_000, summaries[1].activeVehicle.currentMileageKm)
        assertEquals(ObdConnectionState.Connected(ObdConnectionConfig()), summaries[2].connectionState)
        assertEquals(1, summaries[3].activeMaintenanceSchedules.size)
    }

    @Test
    fun dashboardSwitchingVehiclesDoesNotCarryPreviousVehicleData() = runTest {
        val vehicles = TestVehicles(listOf(vehicle(1), vehicle(2)))
        val preferences = TestPreferences(1L)
        val maintenance = TestMaintenance().apply {
            records.getValue(1L).value = listOf(
                MaintenanceRecord(
                    vehicleId = 1L, serviceType = MaintenanceType.OIL_ONLY,
                    performedDate = LocalDate.of(1969, 1, 1), mileageAtServiceKm = 40_000,
                    nextDueDate = LocalDate.of(1969, 2, 1)
                )
            )
        }
        val emissions = mutableListOf<DashboardState>()
        val job = launch {
            dashboard(vehicles, preferences, TestGateway(), TestDiagnostics(), maintenance)()
                .take(2).toList(emissions)
        }
        runCurrent()
        preferences.activeIdFlow.value = 2L
        runCurrent()
        job.cancel()

        val summaries = emissions.map { (it as DashboardState.Ready).summary }
        assertEquals(1, summaries.first().activeMaintenanceSchedules.size)
        assertTrue(summaries.last().activeMaintenanceSchedules.isEmpty())
    }

    @Test
    fun deleteActiveVehicleKeepsVehicleWhenPreferenceUpdateFails() = runTest {
        val vehicle = vehicle(1)
        val vehicles = TestVehicles(listOf(vehicle))
        val preferences = TestPreferences(vehicle.id).apply { failNextSelectionUpdate = true }
        val useCase = DeleteVehicleUseCase(vehicles, preferences, TestGateway())

        val deletionResult = useCase(vehicle.id)

        assertEquals(DeleteVehicleResult.Failed, deletionResult)
        assertEquals(listOf(vehicle), vehicles.values.value)
        assertEquals(vehicle.id, preferences.activeVehicleId.first())
        val state = dashboard(
            vehicles, preferences, TestGateway(), TestDiagnostics(), TestMaintenance()
        )().first() as DashboardState.Ready
        assertEquals(vehicle.id, state.summary.activeVehicle.id)
    }

    @Test
    fun dashboardDeduplicatesCodesAndClearsRecurrentAlertAfterCleanScan() = runTest {
        val diagnostics = TestDiagnostics().apply {
            previous = setOf("P0123")
            counts = mapOf("P0123" to 2)
            sessions[1L] = MutableStateFlow(
                listOf(cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                    id = 1, vehicleId = 1, timestampMillis = 1, mileageAtScanKm = 40_000,
                    codes = listOf("P0123", "P0123", "P0456")
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
        assertEquals(listOf("P0123", "P0456"), summaries.first().codesInLastSession.map { it.code })
        assertEquals(listOf(true, false), summaries.first().codesInLastSession.map { it.isRecurrent })
        assertTrue(summaries.first().alerts.isEmpty())
        assertTrue(summaries.last().codesInLastSession.isEmpty())
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
    var failDelete = false
    var cancelDelete = false
    override fun observeVehicles(): Flow<List<Vehicle>> = values
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> = values.let { flow ->
        flowOf(flow.value.firstOrNull { it.id == vehicleId })
    }
    override suspend fun getVehicleById(vehicleId: Long) = values.value.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) {
        if (cancelDelete) throw kotlinx.coroutines.CancellationException("cancelled")
        if (failDelete) error("Room delete failed")
        values.value = values.value.filterNot { it.id == vehicleId }
    }
}

private class TestPreferences(activeId: Long?) : AppPreferencesRepository {
    val activeIdFlow = MutableStateFlow(activeId)
    var failNextSelectionUpdate = false
    override val activeVehicleId: MutableStateFlow<Long?> = activeIdFlow
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig())
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setActiveVehicleId(vehicleId: Long?) {
        if (failNextSelectionUpdate) {
            failNextSelectionUpdate = false
            error("DataStore write failed")
        }
        activeVehicleId.value = vehicleId
    }
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) { obdConnectionConfig.value = config }
    override suspend fun saveSettings(settings: AppSettings) { this.settings.value = settings }
}

private class TestMaintenance : MaintenanceRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    val records = mutableMapOf<Long, MutableStateFlow<List<MaintenanceRecord>>>(
        1L to MutableStateFlow(emptyList()),
        2L to MutableStateFlow(emptyList())
    )
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        records.getOrPut(vehicleId) { MutableStateFlow(emptyList()) }
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = null
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long {
        savedRecords += record
        return savedRecords.size.toLong()
    }
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> = records[vehicleId]?.value.orEmpty().filter { it.serviceType.supportsAutoAlerts }
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
