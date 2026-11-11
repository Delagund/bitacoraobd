package cl.cristian.bitacoraobd.ui.maintenance

import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceResult
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MaintenanceViewModelTest {
    @Test
    fun onlyLatestAlertableRecordGetsStatusAndOtherNeverDoes() = runTest {
        withMainDispatcher {
            val older = record(id = 1, type = MaintenanceType.OIL_ONLY)
            val latest = record(id = 2, type = MaintenanceType.OIL_ONLY, dueMileage = 100)
            val other = record(id = 3, type = MaintenanceType.OTHER, dueMileage = 1)
            val maintenance = MaintenanceData(listOf(older, latest, other))
            val viewModel = maintenanceViewModel(maintenance, mileage = 100)

            viewModel.observe(1)
            advanceUntilIdle()

            val rows = (viewModel.rows.value as LoadState.Ready).value
            assertEquals(null, rows.single { it.record.id == older.id }.status)
            assertEquals(MaintenanceStatus.OVERDUE, rows.single { it.record.id == latest.id }.status)
            assertNull(rows.single { it.record.id == other.id }.status)
        }
    }

    @Test
    fun editKeepsRecordIdAndUneditedCanonicalMileageAfterUnitConversion() = runTest {
        withMainDispatcher {
            val original = record(id = 7, mileage = 17, dueMileage = 32)
            val maintenance = MaintenanceData(listOf(original))
            val viewModel = maintenanceViewModel(maintenance, unit = DistanceUnit.MILES)

            viewModel.edit(vehicle = 1, recordId = original.id)
            advanceUntilIdle()
            assertEquals("10.6", viewModel.draft!!.mileage)
            assertEquals("19.9", viewModel.draft!!.nextDueMileage)

            viewModel.update { it.copy(notes = "Filtro instalado") }
            viewModel.save()
            advanceUntilIdle()

            assertEquals(original.copy(notes = "Filtro instalado"), maintenance.saved.single())
            assertEquals(7L, maintenance.saved.single().id)
            assertNull(viewModel.draft)
        }
    }

    @Test
    fun wrongVehicleCannotEditRecord() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData(listOf(record(id = 9, vehicle = 2)))
            val viewModel = maintenanceViewModel(maintenance)

            viewModel.edit(vehicle = 1, recordId = 9)
            advanceUntilIdle()

            assertNull(viewModel.draft)
            assertTrue(viewModel.error!!.contains("otro vehículo"))
            assertTrue(maintenance.saved.isEmpty())
        }
    }

    @Test
    fun lateEditAfterRequestLeaveCannotCreateDraftOrSave() = runTest {
        withMainDispatcher {
            val recordLoad = CompletableDeferred<MaintenanceRecord?>()
            val recordRequested = CompletableDeferred<Unit>()
            val preferenceLoad = CompletableDeferred<AppSettings>()
            val preferencesRequested = CompletableDeferred<Unit>()
            val maintenance = MaintenanceData(listOf(record(id = 11)), recordLoad, recordRequested)
            val preferences = MaintenancePreferences(DistanceUnit.KILOMETERS, preferenceLoad, preferencesRequested)
            val viewModel = maintenanceViewModel(maintenance, preferences = preferences)

            viewModel.edit(vehicle = 1, recordId = 11)
            runCurrent()
            recordRequested.await()
            assertNull(viewModel.draft)

            recordLoad.complete(record(id = 11))
            runCurrent()
            preferencesRequested.await()
            viewModel.requestLeave("history")
            preferenceLoad.complete(AppSettings(distanceUnit = DistanceUnit.KILOMETERS))
            advanceUntilIdle()

            assertNull(viewModel.draft)
            viewModel.save()
            advanceUntilIdle()
            assertTrue(maintenance.saved.isEmpty())
        }
    }

    @Test
    fun lateEditAfterSwitchingVehicleCannotCreateDraftForPreviousVehicle() = runTest {
        withMainDispatcher {
            val recordLoad = CompletableDeferred<MaintenanceRecord?>()
            val recordRequested = CompletableDeferred<Unit>()
            val maintenance = MaintenanceData(listOf(record(id = 12)), recordLoad, recordRequested)
            val viewModel = maintenanceViewModel(maintenance)

            viewModel.edit(vehicle = 1, recordId = 12)
            runCurrent()
            recordRequested.await()
            viewModel.observe(2)
            recordLoad.complete(record(id = 12))
            advanceUntilIdle()

            assertNull(viewModel.draft)
            viewModel.save()
            advanceUntilIdle()
            assertTrue(maintenance.saved.isEmpty())
        }
    }

    @Test
    fun reenteringObservedVehicleRefreshesStatusesWithoutRoomEmission() = runTest {
        withMainDispatcher {
            val vehicles = MaintenanceVehicles(Vehicle(1, "A", "B", 2020, "Gasoline", "1.6", 50))
            val maintenance = MaintenanceData(listOf(record(id = 13, dueMileage = 100)))
            val viewModel = maintenanceViewModel(maintenance, vehicles = vehicles)

            viewModel.observe(1)
            advanceUntilIdle()
            assertEquals(MaintenanceStatus.UPCOMING, (viewModel.rows.value as LoadState.Ready).value.single().status)

            vehicles.vehicle = vehicles.vehicle.copy(currentMileageKm = 100)
            viewModel.observe(1)
            advanceUntilIdle()

            assertEquals(MaintenanceStatus.OVERDUE, (viewModel.rows.value as LoadState.Ready).value.single().status)
        }
    }

    @Test
    fun historicalMileageRequiresConfirmationBeforeAnyWrite() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData()
            val viewModel = maintenanceViewModel(maintenance, mileage = 50_000)
            viewModel.add(1)
            advanceUntilIdle()
            viewModel.update { it.copy(performedDate = "2025-01-10", mileage = "40000", notes = "Histórico") }

            viewModel.save()
            advanceUntilIdle()
            assertEquals(40_000 to 50_000, viewModel.historicalMileage)
            assertTrue(maintenance.saved.isEmpty())
            assertEquals("Histórico", viewModel.draft!!.notes)

            viewModel.confirmHistoricalSave()
            advanceUntilIdle()
            assertEquals(1, maintenance.saved.size)
            assertEquals(40_000, maintenance.saved.single().mileageAtServiceKm)
            assertNull(viewModel.draft)
        }
    }

    @Test
    fun saveFailureRetainsDraftAndRetryCanSucceed() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData().apply { failSaves = 1 }
            val viewModel = maintenanceViewModel(maintenance)
            viewModel.add(1)
            advanceUntilIdle()
            viewModel.update { it.copy(performedDate = "2025-01-10", mileage = "10", notes = "Borrador") }

            viewModel.save()
            advanceUntilIdle()
            assertEquals("fallo de almacenamiento", viewModel.draft!!.error)
            assertEquals("Borrador", viewModel.draft!!.notes)
            assertTrue(maintenance.saved.isEmpty())

            viewModel.save()
            advanceUntilIdle()
            assertEquals(1, maintenance.saved.size)
            assertEquals("Borrador", maintenance.saved.single().notes)
            assertNull(viewModel.draft)
        }
    }

    @Test
    fun invalidDateNegativeAndOverflowMileageDoNotReachRepository() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData()
            val viewModel = maintenanceViewModel(maintenance)
            viewModel.add(1)
            advanceUntilIdle()

            listOf(
                MaintenanceDraft(vehicleId = 1, performedDate = "not-a-date", mileage = "10"),
                MaintenanceDraft(vehicleId = 1, performedDate = "2025-01-10", mileage = "-1"),
                MaintenanceDraft(vehicleId = 1, performedDate = "2025-01-10", mileage = "999999999999")
            ).forEach { invalid ->
                viewModel.update { invalid }
                viewModel.save()
                assertTrue(viewModel.draft!!.error!!.isNotBlank())
            }
            advanceUntilIdle()
            assertTrue(maintenance.saved.isEmpty())
        }
    }

    @Test
    fun allEightMaintenanceTypesCanBeSavedAndCancelNeverPersists() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData()
            val viewModel = maintenanceViewModel(maintenance)
            MaintenanceType.entries.forEach { type ->
                viewModel.add(1)
                advanceUntilIdle()
                viewModel.update {
                    it.copy(type = type, performedDate = "2025-01-10", mileage = "10")
                }
                viewModel.save()
                advanceUntilIdle()
                assertEquals(type, maintenance.saved.last().serviceType)
            }
            viewModel.add(1)
            advanceUntilIdle()
            viewModel.update { it.copy(notes = "Cancelar") }
            viewModel.cancel()
            assertNull(viewModel.draft)
            assertEquals(MaintenanceType.entries.size, maintenance.saved.size)
        }
    }

    @Test
    fun navigationWaitsForSaveAndReportsWhetherDraftWasSaved() = runTest {
        withMainDispatcher {
            val maintenance = MaintenanceData()
            val viewModel = maintenanceViewModel(maintenance)
            viewModel.add(1)
            advanceUntilIdle()
            viewModel.update { it.copy(performedDate = "2025-01-10", mileage = "10") }

            viewModel.save()
            viewModel.requestLeave("settings")
            assertNull(viewModel.consumeQueuedLeave())
            advanceUntilIdle()

            assertEquals("settings" to true, viewModel.consumeQueuedLeave())
            assertNull(viewModel.draft)
        }
    }

}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun kotlinx.coroutines.test.TestScope.withMainDispatcher(block: suspend () -> Unit) {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}

private fun maintenanceViewModel(
    maintenance: MaintenanceData,
    mileage: Int = 0,
    unit: DistanceUnit = DistanceUnit.KILOMETERS,
    vehicles: MaintenanceVehicles = MaintenanceVehicles(Vehicle(1, "A", "B", 2020, "Gasoline", "1.6", mileage)),
    preferences: MaintenancePreferences = MaintenancePreferences(unit)
): MaintenanceViewModel {
    val status = GetMaintenanceStatusUseCase(
        vehicles,
        maintenance,
        Clock.fixed(Instant.parse("2025-01-20T00:00:00Z"), ZoneOffset.UTC)
    )
    return MaintenanceViewModel(
        maintenance, status, SaveMaintenanceUseCase(vehicles, maintenance,
            Clock.fixed(Instant.parse("2025-01-20T00:00:00Z"), ZoneOffset.UTC)),
        preferences
    )
}

private fun record(
    id: Long,
    vehicle: Long = 1,
    type: MaintenanceType = MaintenanceType.OIL_ONLY,
    mileage: Int = 0,
    dueMileage: Int? = null
) = MaintenanceRecord(
    id = id, vehicleId = vehicle, serviceType = type,
    performedDate = LocalDate.parse("2025-01-01"), mileageAtServiceKm = mileage,
    nextDueDate = null, nextDueMileageKm = dueMileage
)

private class MaintenanceData(
    initial: List<MaintenanceRecord> = emptyList(),
    private val recordLoad: CompletableDeferred<MaintenanceRecord?>? = null,
    private val recordRequested: CompletableDeferred<Unit>? = null
) : MaintenanceRepository {
    private val observed = mutableMapOf(1L to MutableStateFlow(initial))
    val saved = mutableListOf<MaintenanceRecord>()
    var failSaves = 0
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        observed.getOrPut(vehicleId) { MutableStateFlow(emptyList()) }
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? {
        recordRequested?.complete(Unit)
        return recordLoad?.await()
            ?: observed.values.asSequence().flatMap { it.value.asSequence() }.firstOrNull { it.id == recordId }
    }
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long {
        if (failSaves > 0) {
            failSaves--
            error("fallo de almacenamiento")
        }
        saved += record
        return record.id.takeIf { it > 0 } ?: saved.size.toLong()
    }
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> =
        observed[vehicleId]?.value.orEmpty().filter { it.serviceType.supportsAutoAlerts }
            .groupBy { it.serviceType }
            .values.mapNotNull { records -> records.maxWithOrNull(compareBy<MaintenanceRecord> { it.performedDate }.thenBy { it.id }) }
}

private class MaintenanceVehicles(var vehicle: Vehicle) : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = MutableStateFlow(listOf(vehicle))
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> =
        MutableStateFlow(vehicle.takeIf { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = vehicle.takeIf { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) = Unit
}

private class MaintenancePreferences(
    unit: DistanceUnit,
    private val settingsLoad: CompletableDeferred<AppSettings>? = null,
    private val settingsRequested: CompletableDeferred<Unit>? = null
) : AppPreferencesRepository {
    override val activeVehicleId = MutableStateFlow<Long?>(1)
    override val obdConnectionConfig = MutableStateFlow(cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig())
    override val settings: Flow<AppSettings> = if (settingsLoad == null) {
        MutableStateFlow(AppSettings(distanceUnit = unit)).asStateFlow()
    } else flow {
        settingsRequested?.complete(Unit)
        emit(settingsLoad.await())
    }
    override suspend fun setActiveVehicleId(vehicleId: Long?) { activeVehicleId.value = vehicleId }
    override suspend fun saveObdConnectionConfig(config: cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig) {
        obdConnectionConfig.value = config
    }
    override suspend fun saveSettings(settings: AppSettings) = Unit
}
