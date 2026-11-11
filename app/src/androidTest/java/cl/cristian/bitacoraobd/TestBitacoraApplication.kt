package cl.cristian.bitacoraobd

import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdLiveMetrics
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import cl.cristian.bitacoraobd.domain.usecase.DeleteVehicleUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveDiagnosticSessionUseCase
import cl.cristian.bitacoraobd.domain.usecase.ScanDiagnosticCodesUseCase
import cl.cristian.bitacoraobd.ui.AppSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestBitacoraApplication : BitacoraApplication() {
    internal val fixture by lazy { TestSessionFixture() }
    override val appSession: AppSession get() = fixture.session
}

internal class TestSessionFixture {
    val vehicles = TestVehicles()
    val preferences = TestPreferences()
    val diagnostics = TestDiagnostics()
    val gateway = TestGateway()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val session = AppSession(
        vehicles = vehicles,
        preferences = preferences,
        gateway = gateway,
        scanDiagnostic = ScanDiagnosticCodesUseCase(gateway, vehicles),
        saveDiagnostic = SaveDiagnosticSessionUseCase(diagnostics),
        deleteVehicleUseCase = DeleteVehicleUseCase(vehicles, preferences, gateway),
        scope = scope
    )
}

internal class TestVehicles : VehicleRepository {
    var saveStarted = CompletableDeferred<Unit>()
    var saveGate: CompletableDeferred<Unit>? = null
    var failSave = false
    var saveCalls = 0
    private val vehicle = Vehicle(1L, "Test", "Vehicle", 2020, "Gasoline", "2.0", 100)
    override fun observeVehicles(): Flow<List<Vehicle>> = MutableStateFlow(listOf(vehicle))
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> =
        MutableStateFlow(vehicle.takeIf { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = vehicle.takeIf { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long {
        saveCalls++
        saveGate?.let {
            saveStarted.complete(Unit)
            it.await()
        }
        check(!failSave) { "Injected vehicle save failure" }
        return if (vehicle.id == 0L) 2L else vehicle.id
    }
    override suspend fun deleteVehicle(vehicleId: Long) = Unit
}

internal class TestPreferences : AppPreferencesRepository {
    private val active = MutableStateFlow<Long?>(1L)
    override val activeVehicleId: Flow<Long?> = active
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig()).asStateFlow()
    override val settings = MutableStateFlow(AppSettings()).asStateFlow()
    override suspend fun setActiveVehicleId(vehicleId: Long?) { active.value = vehicleId }
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) = Unit
    override suspend fun saveSettings(settings: AppSettings) = Unit
}

internal class TestDiagnostics : DiagnosticRepository {
    val saveStarted = CompletableDeferred<Unit>()
    val allowSave = CompletableDeferred<Unit>()
    var saveCalls = 0
    var failSave = false
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> =
        MutableStateFlow(emptyList())
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long {
        saveCalls++
        saveStarted.complete(Unit)
        allowSave.await()
        check(!failSave) { "Injected save failure" }
        return 1L
    }
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) =
        emptySet<String>()
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = emptyMap<String, Int>()
    override fun getDtcGuidance(code: String) =
        DtcGuidance(code, "", "", emptyList(), emptyList(), false)
}

internal class TestGateway : ObdGateway {
    private val mutableState = MutableStateFlow<ObdConnectionState>(
        ObdConnectionState.Connected(ObdConnectionConfig())
    )
    override val connectionState: StateFlow<ObdConnectionState> = mutableState.asStateFlow()
    override suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> =
        ObdResult.Success(Unit)
    override suspend fun disconnect() { mutableState.value = ObdConnectionState.Disconnected }
    override suspend fun readLiveMetrics(): ObdResult<ObdLiveMetrics> =
        ObdResult.Failure("Not used")
    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> =
        ObdResult.Success(listOf("P0123"))
}
