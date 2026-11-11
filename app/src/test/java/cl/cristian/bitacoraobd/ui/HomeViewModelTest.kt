package cl.cristian.bitacoraobd.ui

import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetDashboardDataUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @Test
    fun changingActiveVehicleUpdatesDashboardToTheNewVehicleContext() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val first = Vehicle(1L, "First", "Car", 2020, "Gasoline", "2.0", 10)
            val second = Vehicle(2L, "Second", "Car", 2021, "Diesel", "1.6", 20)
            val vehicles = HomeVehicles(first, listOf(second))
            val preferences = HomePreferences()
            val diagnostics = HomeDiagnostics()
            val maintenance = HomeMaintenance(emptyList())
            val gateway = HomeGateway()
            val dashboard = GetDashboardDataUseCase(
                vehicles, preferences, diagnostics, maintenance, gateway,
                GetMaintenanceStatusUseCase(vehicles, maintenance, MutableClock(Instant.EPOCH)),
                AnalyzeDiagnosticHistoryUseCase(diagnostics)
            )
            val viewModel = HomeViewModel(dashboard)

            viewModel.setActive(true)
            advanceUntilIdle()
            assertEquals(1L, (viewModel.state.value.dashboard as DashboardState.Ready).summary.activeVehicle.id)

            preferences.active.value = 2L
            advanceUntilIdle()

            assertEquals(2L, (viewModel.state.value.dashboard as DashboardState.Ready).summary.activeVehicle.id)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun reactivationRecalculatesMaintenanceAgainstCurrentDateWithoutTimer() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val clock = MutableClock(Instant.parse("2026-01-01T12:00:00Z"))
            val vehicle = Vehicle(1L, "A", "B", 2020, "Gasoline", "2.0", 10)
            val records = listOf(
                MaintenanceRecord(
                    id = 1L,
                    vehicleId = 1L,
                    serviceType = MaintenanceType.OIL_ONLY,
                    performedDate = LocalDate.parse("2025-12-01"),
                    mileageAtServiceKm = 0,
                    nextDueDate = LocalDate.parse("2026-01-02")
                )
            )
            val vehicles = HomeVehicles(vehicle)
            val preferences = HomePreferences()
            val diagnostics = HomeDiagnostics()
            val maintenance = HomeMaintenance(records)
            val gateway = HomeGateway()
            val dashboard = GetDashboardDataUseCase(
                vehicles, preferences, diagnostics, maintenance, gateway,
                GetMaintenanceStatusUseCase(vehicles, maintenance, clock),
                AnalyzeDiagnosticHistoryUseCase(diagnostics)
            )
            val viewModel = HomeViewModel(dashboard)

            viewModel.setActive(true)
            advanceUntilIdle()
            assertEquals(
                MaintenanceStatus.UPCOMING,
                (viewModel.state.value.dashboard as DashboardState.Ready)
                    .summary.activeMaintenanceSchedules.single().status
            )

            clock.current = Instant.parse("2026-01-02T12:00:00Z")
            advanceUntilIdle()
            assertEquals(
                MaintenanceStatus.UPCOMING,
                (viewModel.state.value.dashboard as DashboardState.Ready)
                    .summary.activeMaintenanceSchedules.single().status
            )

            viewModel.setActive(false)
            advanceUntilIdle()
            viewModel.setActive(true)
            advanceUntilIdle()
            assertEquals(
                MaintenanceStatus.OVERDUE,
                (viewModel.state.value.dashboard as DashboardState.Ready)
                    .summary.activeMaintenanceSchedules.single().status
            )

            val currentDashboard = viewModel.state.value.dashboard
            diagnostics.failAfterEmission = true
            viewModel.setActive(false)
            advanceUntilIdle()
            viewModel.setActive(true)
            advanceUntilIdle()
            assertEquals(currentDashboard, viewModel.state.value.dashboard)
            assertEquals("diagnostic refresh failed", viewModel.state.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class MutableClock(var current: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneId.of("UTC")
    override fun withZone(zone: ZoneId): Clock = this
    override fun instant(): Instant = current
}

private class HomeVehicles(vehicle: Vehicle, additional: List<Vehicle> = emptyList()) : VehicleRepository {
    private val all = listOf(vehicle) + additional
    override fun observeVehicles(): Flow<List<Vehicle>> = MutableStateFlow(all)
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> = MutableStateFlow(all.firstOrNull { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = all.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) = Unit
}

private class HomePreferences(initialVehicleId: Long? = 1L) : AppPreferencesRepository {
    val active = MutableStateFlow(initialVehicleId)
    override val activeVehicleId: Flow<Long?> = active
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig()).asStateFlow()
    override val settings = MutableStateFlow(AppSettings()).asStateFlow()
    override suspend fun setActiveVehicleId(vehicleId: Long?) = Unit
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) = Unit
    override suspend fun saveSettings(settings: AppSettings) = Unit
}

private class HomeDiagnostics : DiagnosticRepository {
    var failAfterEmission = false
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> =
        kotlinx.coroutines.flow.flow {
            emit(emptyList())
            if (failAfterEmission) throw IllegalStateException("diagnostic refresh failed")
        }
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long = 1L
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) = emptySet<String>()
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = emptyMap<String, Int>()
    override fun getDtcGuidance(code: String) = DtcGuidance(
        code, "", "", emptyList(), emptyList(), false
    )
}

private class HomeMaintenance(private val records: List<MaintenanceRecord>) : MaintenanceRepository {
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> = MutableStateFlow(records)
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = records.firstOrNull { it.id == recordId }
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long = record.id
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> = records
}

private class HomeGateway : ObdGateway {
    override val connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected).asStateFlow()
    override suspend fun connect(config: ObdConnectionConfig) = ObdResult.Success(Unit)
    override suspend fun disconnect() = Unit
    override suspend fun readLiveMetrics() = ObdResult.Failure("not used")
    override suspend fun readDiagnosticCodes() = ObdResult.Success(emptyList<String>())
}
