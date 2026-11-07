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
        val gateway = FakeGateway().apply {
            connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
            codes = ObdResult.Success(emptyList())
        }
        val result = ScanDiagnosticCodesUseCase(gateway, FakeVehicles(vehicle), fixedClock())(vehicle.id)

        assertEquals(
            DiagnosticScanResult.Success(vehicle.id, 1234L, 42_000, emptyList()),
            result
        )
    }

    @Test
    fun scan_missingVehicleDoesNotReadCodes() = runBlocking {
        val gateway = FakeGateway().apply {
            connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
        }
        val result = ScanDiagnosticCodesUseCase(
            gateway, FakeVehicles(testVehicle().copy(id = 2L)), fixedClock()
        )(1L)

        assertEquals(DiagnosticScanResult.Failure("Vehicle does not exist"), result)
        assertEquals(0, gateway.diagnosticReads)
    }

    @Test
    fun scan_invalidConnectionStatesDoNotReadCodes() = runBlocking {
        val states = listOf(
            ObdConnectionState.Connecting,
            ObdConnectionState.Disconnecting,
            ObdConnectionState.Error("connection failed")
        )

        states.forEach { state ->
            val gateway = FakeGateway().apply { connectionState.value = state }
            val result = ScanDiagnosticCodesUseCase(
                gateway, FakeVehicles(testVehicle()), fixedClock()
            )(1L)

            assertEquals("state=$state", DiagnosticScanResult.Failure("OBD adapter is not connected"), result)
            assertEquals("state=$state", 0, gateway.diagnosticReads)
        }
    }

    @Test
    fun scan_disconnectedDoesNotReadCodes() = runBlocking {
        val gateway = FakeGateway()
        val result = ScanDiagnosticCodesUseCase(gateway, FakeVehicles(testVehicle()), fixedClock())(1L)

        assertEquals(DiagnosticScanResult.Failure("OBD adapter is not connected"), result)
        assertEquals(0, gateway.diagnosticReads)
    }

    @Test
    fun scan_duplicateCodesAreDeduplicatedBeforeSaving() = runBlocking {
        val gateway = FakeGateway().apply {
            connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
            codes = ObdResult.Success(listOf("P0123", "P0456", "P0123"))
        }
        val result = ScanDiagnosticCodesUseCase(
            gateway, FakeVehicles(testVehicle()), fixedClock()
        )(1L)
        val diagnostics = FakeDiagnostics()

        SaveDiagnosticSessionUseCase(diagnostics)(result)

        assertEquals(listOf("P0123", "P0456"), diagnostics.saved?.codes)
        assertEquals(1, diagnostics.writeCount)
    }

    @Test
    fun scan_failureDoesNotWriteDiagnosticSession() = runBlocking {
        val gateway = FakeGateway().apply {
            connectionState.value = ObdConnectionState.Connected(ObdConnectionConfig())
            codes = ObdResult.Failure("offline")
        }
        val result = ScanDiagnosticCodesUseCase(gateway, FakeVehicles(testVehicle()), fixedClock())(1L)
        val diagnostics = FakeDiagnostics()

        assertEquals(DiagnosticScanResult.Failure("offline"), result)
        assertNull(SaveDiagnosticSessionUseCase(diagnostics)(result))
        assertNull(diagnostics.saved)
        assertEquals(0, diagnostics.writeCount)
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
    fun maintenanceDateBoundaryIsUpcomingBeforeAndOverdueOnOrAfterDueDate() = runBlocking {
        val vehicle = testVehicle()
        val records = listOf(
            maintenanceRecord(1, nextDueDate = LocalDate.of(1970, 1, 2)),
            maintenanceRecord(2, nextDueDate = LocalDate.of(1970, 1, 1)),
            maintenanceRecord(3, nextDueDate = LocalDate.of(1969, 12, 31))
        )

        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), FakeMaintenance(records), fixedClock()
        )(vehicle.id)

        assertEquals(
            listOf(
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UPCOMING,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE
            ),
            statuses.map { it.status }
        )
    }

    @Test
    fun maintenanceMileageBoundaryIsUpcomingBelowAndOverdueAtOrAboveDueMileage() = runBlocking {
        val vehicle = testVehicle()
        val records = listOf(
            maintenanceRecord(1, nextDueMileageKm = 42_001),
            maintenanceRecord(2, nextDueMileageKm = 42_000),
            maintenanceRecord(3, nextDueMileageKm = 41_999)
        )

        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), FakeMaintenance(records), fixedClock()
        )(vehicle.id)

        assertEquals(
            listOf(
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UPCOMING,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE
            ),
            statuses.map { it.status }
        )
    }

    @Test
    fun maintenanceIsOverdueWhenEitherDueLimitIsReached() = runBlocking {
        val vehicle = testVehicle()
        val records = listOf(
            maintenanceRecord(1, nextDueDate = LocalDate.of(1970, 1, 2), nextDueMileageKm = 42_000),
            maintenanceRecord(2, nextDueDate = LocalDate.of(1970, 1, 1), nextDueMileageKm = 42_001)
        )

        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), FakeMaintenance(records), fixedClock()
        )(vehicle.id)

        assertEquals(
            List(2) { cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE },
            statuses.map { it.status }
        )
    }

    @Test
    fun maintenanceAlertTypesAreEvaluatedIndependently() = runBlocking {
        val vehicle = testVehicle()
        val records = listOf(
            maintenanceRecord(1, MaintenanceType.OIL_AND_FILTER, nextDueMileageKm = 42_001),
            maintenanceRecord(2, MaintenanceType.OIL_ONLY, nextDueMileageKm = 42_000),
            maintenanceRecord(3, MaintenanceType.FILTER_ONLY, nextDueDate = LocalDate.of(1970, 1, 2))
        )

        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), FakeMaintenance(records), fixedClock()
        )(vehicle.id)

        assertEquals(records.map { it.serviceType }, statuses.map { it.record.serviceType })
        assertEquals(
            listOf(
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UPCOMING,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.OVERDUE,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UPCOMING
            ),
            statuses.map { it.status }
        )
    }

    @Test
    fun maintenanceStatusDistinguishesUpcomingAndUnscheduled() = runBlocking {
        val vehicle = testVehicle()
        val records = listOf(
            MaintenanceRecord(
                id = 1, vehicleId = vehicle.id, serviceType = MaintenanceType.OIL_ONLY,
                performedDate = LocalDate.of(2025, 1, 1), mileageAtServiceKm = 40_000,
                nextDueDate = LocalDate.of(2026, 11, 8)
            ),
            MaintenanceRecord(
                id = 2, vehicleId = vehicle.id, serviceType = MaintenanceType.FILTER_ONLY,
                performedDate = LocalDate.of(2025, 1, 1), mileageAtServiceKm = 40_000
            )
        )

        val statuses = GetMaintenanceStatusUseCase(
            FakeVehicles(vehicle), FakeMaintenance(records), fixedClock()
        )(vehicle.id)

        assertEquals(
            listOf(
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UPCOMING,
                cl.cristian.bitacoraobd.domain.model.MaintenanceStatus.UNSCHEDULED
            ),
            statuses.map { it.status }
        )
    }

    @Test
    fun historyFirstAppearanceIsNotRecurrent() = runBlocking {
        val results = AnalyzeDiagnosticHistoryUseCase(FakeDiagnostics())(
            DiagnosticSession(id = 1, vehicleId = 1, timestampMillis = 10, mileageAtScanKm = 42_000, codes = listOf("P0123"))
        )

        assertEquals(false, results.single().isRecurrent)
    }

    @Test
    fun historyDoesNotCountLaterSessionsAsPrevious() = runBlocking {
        val diagnostics = FakeDiagnostics()
        val session = DiagnosticSession(
            id = 2, vehicleId = 1, timestampMillis = 20, mileageAtScanKm = 42_000,
            codes = listOf("P0123")
        )

        val result = AnalyzeDiagnosticHistoryUseCase(diagnostics)(session)

        assertEquals(false, result.single().isRecurrent)
        assertEquals(1L, diagnostics.previousQueryVehicleId)
        assertEquals(20L, diagnostics.previousQueryTimestamp)
        assertEquals(2L, diagnostics.previousQuerySessionId)
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

private fun maintenanceRecord(
    id: Long,
    serviceType: MaintenanceType = MaintenanceType.OIL_ONLY,
    nextDueDate: LocalDate? = null,
    nextDueMileageKm: Int? = null
) = MaintenanceRecord(
    id = id,
    vehicleId = 1L,
    serviceType = serviceType,
    performedDate = LocalDate.of(1969, 1, 1),
    mileageAtServiceKm = 40_000,
    nextDueDate = nextDueDate,
    nextDueMileageKm = nextDueMileageKm
)

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
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long) = records.filter { it.serviceType.supportsAutoAlerts }
}

private class FakeDiagnostics : DiagnosticRepository {
    var saved: DiagnosticSession? = null
    var writeCount = 0
    var previousCodes: Set<String> = emptySet()
    var previousQueryVehicleId: Long? = null
    var previousQueryTimestamp: Long? = null
    var previousQuerySessionId: Long? = null
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> = flowOf(emptyList())
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long {
        writeCount++
        saved = session
        return 1L
    }
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long): Set<String> {
        previousQueryVehicleId = vehicleId
        previousQueryTimestamp = timestampMillis
        previousQuerySessionId = sessionId
        return previousCodes
    }
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = emptyMap<String, Int>()
    override fun getDtcGuidance(code: String) = DtcGuidance(code, code, "Engine", emptyList(), emptyList(), false)
}

private class FakeGateway : ObdGateway {
    override val connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    var codes: ObdResult<List<String>> = ObdResult.Success(emptyList())
    var diagnosticReads = 0
    override suspend fun connect(config: ObdConnectionConfig) = ObdResult.Success(Unit)
    override suspend fun disconnect() = Unit
    override suspend fun readLiveMetrics() = ObdResult.Success(ObdLiveMetrics(null, null, null, 0L))
    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> {
        diagnosticReads++
        return codes
    }
}
