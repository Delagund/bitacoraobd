package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DiagnosticScanResult
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdLiveMetrics
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiagnosticAndMaintenanceUseCasesTest {
    @Test
    fun scan_successWithNoCodes_isSuccessfulAndUsesVehicleMileage() = runBlocking {
        val vehicle = testVehicle()
        val gateway = FakeGateway().apply { codes = ObdResult.Success(emptyList()) }
        val result = ScanDiagnosticCodesUseCase(gateway, FakeVehicles(vehicle), fixedClock())(vehicle.id)

        assertEquals(
            DiagnosticScanResult.Success(vehicle.id, 1234L, 42_000, emptyList()),
            result
        )
    }

    @Test
    fun scan_failureDoesNotProducePersistableSuccess() = runBlocking {
        val gateway = FakeGateway().apply { codes = ObdResult.Failure("offline") }
        val result = ScanDiagnosticCodesUseCase(gateway, FakeVehicles(testVehicle()), fixedClock())(1L)

        assertEquals(DiagnosticScanResult.Failure("offline"), result)
    }

    @Test
    fun saveDiagnostic_failureDoesNotWriteSession() = runBlocking {
        val repo = FakeDiagnostics()
        assertNull(SaveDiagnosticSessionUseCase(repo)(DiagnosticScanResult.Failure("offline")))
        assertNull(repo.saved)
    }

    @Test
    fun saveDiagnostic_successUsesVehicleFromOriginalScan() = runBlocking {
        val repo = FakeDiagnostics()
        SaveDiagnosticSessionUseCase(repo)(
            DiagnosticScanResult.Success(7L, 55L, 100, listOf("P0123"))
        )

        assertEquals(7L, repo.saved?.vehicleId)
        assertEquals(listOf("P0123"), repo.saved?.codes)
    }

    @Test
    fun maintenanceStatus_marksReachedDateOrMileageOverdue() = runBlocking {
        val vehicle = testVehicle()
        val dueDate = MaintenanceRecord(
            vehicleId = vehicle.id,
            serviceType = MaintenanceType.OIL_ONLY,
            performedDate = LocalDate.of(2025, 1, 1),
            mileageAtServiceKm = 40_000,
            nextDueDate = LocalDate.of(1970, 1, 1)
        )
        val repo = FakeMaintenance(listOf(dueDate))
        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), repo, fixedClock()
        )(vehicle.id)

        assertEquals(1, statuses.size)
        assertEquals(cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE, statuses.single().status)
    }

    @Test
    fun historyMarksCodeRecurrentOnlyWhenInPreviousSessions() = runBlocking {
        val repo = FakeDiagnostics().apply { previousCodes = setOf("P0123") }
        val results = AnalyzeDiagnosticHistoryUseCase(repo)(
            DiagnosticSession(id = 3, vehicleId = 1, timestampMillis = 30, mileageAtScanKm = 42_000, codes = listOf("P0123", "P0456"))
        )

        assertEquals(listOf(true, false), results.map { it.isRecurrent })
    }
}

private fun fixedClock(): Clock = Clock.fixed(Instant.ofEpochMilli(1234L), ZoneOffset.UTC)

private fun testVehicle() = Vehicle(
    id = 1, brand = "A", model = "B", year = 2020, fuelType = "Gasoline",
    engineDescription = "1.6", currentMileageKm = 42_000
)

private class FakeVehicles(private val vehicle: Vehicle) : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = flowOf(listOf(vehicle))
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> = flowOf(vehicle.takeIf { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = vehicle.takeIf { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle) = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) = Unit
}

private class FakeMaintenance(private val records: List<MaintenanceRecord>) : MaintenanceRepository {
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> = flowOf(records)
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = records.firstOrNull { it.id == recordId }
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) = 1L
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long) = records
}

private class FakeDiagnostics : DiagnosticRepository {
    var saved: DiagnosticSession? = null
    var previousCodes: Set<String> = emptySet()
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> = flowOf(emptyList())
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long {
        saved = session
        return 1L
    }
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) = previousCodes
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = emptyMap<String, Int>()
    override fun getDtcGuidance(code: String) = DtcGuidance(code, code, "Engine", emptyList(), emptyList(), false)
}

private class FakeGateway : ObdGateway {
    override val connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    var codes: ObdResult<List<String>> = ObdResult.Success(emptyList())
    override suspend fun connect(config: ObdConnectionConfig) = ObdResult.Success(Unit)
    override suspend fun disconnect() = Unit
    override suspend fun readLiveMetrics() = ObdResult.Success(ObdLiveMetrics(null, null, null, 0L))
    override suspend fun readDiagnosticCodes() = codes
}
