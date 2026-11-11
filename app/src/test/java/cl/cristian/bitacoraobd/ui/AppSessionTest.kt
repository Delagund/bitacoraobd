package cl.cristian.bitacoraobd.ui

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
import cl.cristian.bitacoraobd.ui.connection.ConnectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppSessionTest {
    @Test
    fun successfulConnectKeepsGatewayConnectedWithoutDisconnecting() = runTest {
        val gateway = FakeGateway()
        val result = CompletableDeferred<ObdResult<Unit>>()
        gateway.connectResult = result
        val fixture = Fixture(gateway, backgroundScope)

        runCurrent()
        fixture.session.connect(ObdConnectionConfig(), localNetworkAllowed = true)
        runCurrent()
        assertEquals(ObdConnectionState.Connecting, gateway.connectionState.value)

        result.complete(ObdResult.Success(Unit))
        runCurrent()

        assertTrue(gateway.connectionState.value is ObdConnectionState.Connected)
        assertEquals(0, gateway.disconnectCalls)
    }

    @Test
    fun connectionFormRejectsInvalidAddressAndPortWithoutOpeningSocket() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val gateway = FakeGateway()
            val fixture = Fixture(gateway, backgroundScope)
            val model = ConnectionViewModel(fixture.session, fixture.preferences)
            runCurrent()
            assertEquals(false, model.dirty)
            model.editIp("not an IP")
            model.editPort("0")
            assertEquals(true, model.dirty)
            model.connect()
            runCurrent()
            assertEquals(0, gateway.connectCalls)
            assertTrue(model.error != null)
            model.discard()
            assertEquals(false, model.dirty)
            assertEquals("192.168.0.10", model.ip)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedConfigWriteKeepsConnectionAndRetriesSameEndpoint() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)
        val config = ObdConnectionConfig("192.168.0.10", 35000)
        fixture.preferences.failNextConfigSave = true
        gateway.connectResult.complete(ObdResult.Success(Unit))
        runCurrent()

        fixture.session.connect(config, localNetworkAllowed = true)
        runCurrent()
        assertTrue(gateway.connectionState.value is ObdConnectionState.Connected)
        assertEquals(config, fixture.session.state.value.connectedConfigSavePending)
        assertEquals(1, gateway.connectCalls)
        assertEquals(1, fixture.preferences.configSaveCalls)

        fixture.session.retryConnectedConfigSave()
        runCurrent()
        assertEquals(2, fixture.preferences.configSaveCalls)
        assertEquals(config, fixture.preferences.lastSavedConfig)
        assertNull(fixture.session.state.value.connectedConfigSavePending)
        assertEquals(1, gateway.connectCalls)
    }

    @Test
    fun savedCaptureRetainsIdentityWhenNextScanFails() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        val first = fixture.session.state.value.pendingDiagnostic!!
        fixture.session.savePendingDiagnostic()
        runCurrent()
        assertEquals(first, fixture.session.state.value.savedDiagnostic)
        assertEquals(1L, fixture.session.state.value.savedDiagnosticId)

        gateway.scanResult = ObdResult.Failure("scan failed")
        fixture.session.scanDiagnostic()
        runCurrent()
        assertEquals(first, fixture.session.state.value.savedDiagnostic)
        assertEquals(1L, fixture.session.state.value.savedDiagnosticId)
        assertEquals("scan failed", fixture.session.state.value.error)
        assertTrue(fixture.session.state.value.scanFailed)
        assertNull(fixture.session.state.value.pendingDiagnostic)
    }

    @Test
    fun newSuccessfulScanReplacesSavedCaptureWithoutInheritedIdentity() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        val first = fixture.session.state.value.pendingDiagnostic!!
        fixture.session.savePendingDiagnostic()
        runCurrent()
        gateway.scanResult = ObdResult.Success(listOf("P0300"))

        fixture.session.scanDiagnostic()
        runCurrent()
        val next = fixture.session.state.value.pendingDiagnostic!!
        assertTrue(first.captureId != next.captureId)
        assertEquals(listOf("P0300"), next.codes)
        assertNull(fixture.session.state.value.savedDiagnostic)
        assertNull(fixture.session.state.value.savedDiagnosticId)
    }

    @Test
    fun pendingEndpointSurvivesDisconnectAndBlocksAnotherHandshake() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)
        val original = ObdConnectionConfig("192.168.0.10", 35000)
        val replacement = ObdConnectionConfig("192.168.0.11", 35000)
        fixture.preferences.failNextConfigSave = true
        gateway.connectResult.complete(ObdResult.Success(Unit))
        runCurrent()
        fixture.session.connect(original, localNetworkAllowed = true)
        runCurrent()
        fixture.session.disconnect()
        runCurrent()
        fixture.session.connect(replacement, localNetworkAllowed = true)
        runCurrent()
        assertEquals(original, fixture.session.state.value.connectedConfigSavePending)
        assertEquals(1, gateway.connectCalls)
    }

    @Test
    fun switchingFromAtoBAndBackDoesNotResurfaceOldCaptureOrMetrics() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        fixture.session.savePendingDiagnostic()
        runCurrent()
        fixture.session.readMetrics()
        runCurrent()
        assertTrue(fixture.session.state.value.savedDiagnostic != null)
        assertTrue(fixture.session.state.value.metrics != null)

        fixture.session.disconnect()
        runCurrent()
        fixture.session.selectVehicle(2L)
        runCurrent()
        assertNull(fixture.session.state.value.savedDiagnostic)
        assertNull(fixture.session.state.value.metrics)
        fixture.session.state.value.authorizedExit?.let { fixture.session.completeExit(it.id) }
        fixture.session.selectVehicle(1L)
        runCurrent()
        assertNull(fixture.session.state.value.savedDiagnostic)
        assertNull(fixture.session.state.value.savedDiagnosticId)
        assertNull(fixture.session.state.value.metrics)
    }

    @Test
    fun cancellingSecondScanKeepsFirstSavedAndMarksCancellationSeparately() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        fixture.session.savePendingDiagnostic()
        runCurrent()
        val first = fixture.session.state.value.savedDiagnostic
        gateway.scanGate = CompletableDeferred()
        fixture.session.scanDiagnostic()
        runCurrent()
        assertEquals("scan", fixture.session.state.value.operation)
        fixture.session.disconnect()
        runCurrent()
        assertEquals(first, fixture.session.state.value.savedDiagnostic)
        assertEquals(1L, fixture.session.state.value.savedDiagnosticId)
        assertTrue(fixture.session.state.value.scanCancelled)
        assertNull(fixture.session.state.value.pendingDiagnostic)
    }

    @Test
    fun exitDuringSaveWaitsForWriteWithoutAnotherPendingDecision() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        fixture.diagnostics.saveGate = CompletableDeferred()
        fixture.session.savePendingDiagnostic()
        runCurrent()
        fixture.session.requestExit(ExitAction.ToRoute("home"))
        runCurrent()
        assertNull(fixture.session.state.value.pendingDecision)
        assertNull(fixture.session.state.value.authorizedExit)
        fixture.diagnostics.saveGate!!.complete(Unit)
        runCurrent()
        assertNull(fixture.session.state.value.pendingDecision)
        assertNull(fixture.session.state.value.pendingDiagnostic)
        assertEquals(ExitAction.ToRoute("home"), fixture.session.state.value.authorizedExit?.action)
        assertEquals(1, fixture.diagnostics.saveCalls)
    }

    @Test
    fun connectedStateBlocksSelectionAndAnyVehicleDeleteWithoutDisconnecting() = runTest {
        val gateway = FakeGateway().apply {
            setState(ObdConnectionState.Connected(ObdConnectionConfig()))
        }
        val fixture = Fixture(gateway, backgroundScope)

        runCurrent()
        fixture.session.selectVehicle(2L)
        fixture.session.deleteVehicle(1L)
        runCurrent()

        assertEquals(1L, fixture.preferences.active.value)
        assertEquals(emptyList<Long>(), fixture.vehicles.deleted)
        assertEquals(0, gateway.disconnectCalls)
        assertNull(fixture.session.state.value.authorizedExit)
    }

    @Test
    fun connectedActiveVehicleCannotBeOpenedOrSavedForEditing() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        val original = fixture.vehicles.getVehicleById(1L)!!

        fixture.session.openVehicleEditor(1L)
        runCurrent()
        assertEquals(VehicleAction.OpenEditor, fixture.session.state.value.vehicleActionResult?.action)
        assertTrue(fixture.session.state.value.vehicleActionResult?.error != null)
        fixture.session.consumeVehicleResult(fixture.session.state.value.vehicleActionResult!!.id)

        fixture.session.saveVehicle(original.copy(brand = "Changed"), original)
        runCurrent()

        assertEquals(VehicleAction.Save, fixture.session.state.value.vehicleActionResult?.action)
        assertTrue(fixture.session.state.value.vehicleActionResult?.error != null)
        assertEquals(0, fixture.vehicles.saveCalls)
        assertEquals(0, gateway.disconnectCalls)
    }

    @Test
    fun connectedNonActiveVehicleCanBeEditedWithoutDisconnecting() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        val original = fixture.vehicles.getVehicleById(2L)!!

        fixture.session.openVehicleEditor(2L)
        runCurrent()
        assertEquals(null, fixture.session.state.value.vehicleActionResult?.error)
        fixture.session.consumeVehicleResult(fixture.session.state.value.vehicleActionResult!!.id)

        fixture.session.saveVehicle(original.copy(brand = "Changed"), original)
        runCurrent()

        assertEquals(1, fixture.vehicles.saveCalls)
        assertEquals(0, gateway.disconnectCalls)
    }

    @Test
    fun invalidRouteIsRejectedAndValidRouteCanFollow() = runTest {
        val fixture = Fixture(FakeGateway(), backgroundScope)
        runCurrent()

        fixture.session.requestExit(ExitAction.ToRoute("history/0"))
        runCurrent()

        assertNull(fixture.session.state.value.authorizedExit)
        assertEquals("Destino de navegación no válido", fixture.session.state.value.error)

        fixture.session.requestExit(ExitAction.ToRoute("settings"))
        runCurrent()

        assertEquals(ExitAction.ToRoute("settings"), fixture.session.state.value.authorizedExit?.action)
    }

    @Test
    fun failedNavigationReleasesAuthorizationForRetry() = runTest {
        val fixture = Fixture(FakeGateway(), backgroundScope)
        runCurrent()

        fixture.session.requestExit(ExitAction.ToRoute("settings"))
        runCurrent()
        val failed = fixture.session.state.value.authorizedExit!!
        fixture.session.failExit(failed.id, "navigation failed")

        assertNull(fixture.session.state.value.authorizedExit)
        assertEquals("navigation failed", fixture.session.state.value.error)

        fixture.session.requestExit(ExitAction.ToRoute("history/1"))
        runCurrent()
        val retry = fixture.session.state.value.authorizedExit!!
        assertTrue(retry.id != failed.id)
        assertEquals(ExitAction.ToRoute("history/1"), retry.action)
        fixture.session.completeExit(retry.id)
        assertNull(fixture.session.state.value.error)
    }

    @Test
    fun completingRootBackAllowsAnotherSessionExitRequest() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()

        fixture.session.requestExit(ExitAction.Back)
        runCurrent()
        val first = fixture.session.state.value.authorizedExit
        assertEquals(ExitAction.Back, first?.action)
        fixture.session.completeExit(first!!.id)

        fixture.session.requestExit(ExitAction.ToRoute("settings"))
        runCurrent()

        assertEquals(ExitAction.ToRoute("settings"), fixture.session.state.value.authorizedExit?.action)
    }

    @Test
    fun cancelledFirstInsertRetainsCommittedVehicleIdForRecovery() = runTest {
        val gateway = FakeGateway()
        val gate = CompletableDeferred<Unit>()
        val vehicles = GatedVehicleRepository(gate)
        val preferences = FakePreferences().apply { active.value = null }
        val diagnostics = FakeDiagnostics()
        val sessionJob = Job()
        val session = AppSession(
            vehicles = vehicles,
            preferences = preferences,
            gateway = gateway,
            scanDiagnostic = ScanDiagnosticCodesUseCase(gateway, vehicles),
            saveDiagnostic = SaveDiagnosticSessionUseCase(diagnostics),
            deleteVehicleUseCase = DeleteVehicleUseCase(vehicles, preferences, gateway),
            scope = CoroutineScope(coroutineContext + sessionJob)
        )
        runCurrent()

        session.saveVehicle(Vehicle(brand = "A", model = "B", year = 0, fuelType = "", engineDescription = "", currentMileageKm = 0))
        runCurrent()
        assertEquals(1, vehicles.saveCalls)

        sessionJob.cancel()
        gate.complete(Unit)
        runCurrent()

        assertEquals(1, vehicles.rows.value.size)
        assertEquals(41L, session.state.value.vehicleActionResult?.vehicleId)
        assertNull(session.state.value.authorizedExit)
    }

    @Test
    fun selectingVehicleAuthorizesHomeAfterPersistenceAndOtherOperationsDoNotNavigate() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)

        runCurrent()
        fixture.session.selectVehicle(2L)
        runCurrent()
        val selection = fixture.session.state.value.authorizedExit
        assertEquals(ExitAction.ToRoute("home"), selection?.action)
        fixture.session.completeExit(selection!!.id)

        fixture.session.deleteVehicle(1L)
        runCurrent()
        fixture.session.disconnect()
        runCurrent()

        assertNull(fixture.session.state.value.authorizedExit)
    }

    @Test
    fun vehicleRecoveryReconcilesByReadingWithoutRepeatingSelectionWrite() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.preferences.failNextSet = true

        fixture.session.selectVehicle(2L)
        runCurrent()
        val failed = fixture.session.state.value.vehicleActionResult
        assertEquals(VehicleRecovery.Reconcile, failed?.recovery)
        assertEquals(1, fixture.preferences.setCalls)

        fixture.session.retryVehicleRecovery(failed!!.id)
        runCurrent()

        assertEquals(1L, fixture.preferences.active.value)
        assertEquals(1, fixture.preferences.setCalls)
        assertNull(fixture.session.state.value.vehicleActionResult)
    }

    @Test
    fun roomDeleteFailureRefreshesTheActualSelectionBeforeRetry() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.vehicles.failDelete = true

        fixture.session.deleteVehicle(1L)
        runCurrent()

        assertEquals(2L, fixture.preferences.active.value)
        assertEquals(2L, fixture.session.state.value.activeVehicleId)
        assertTrue(fixture.session.state.value.error != null)
    }

    @Test
    fun pendingCaptureStayKeepsCaptureAndDoesNotAuthorizeExit() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        val capture = fixture.session.state.value.pendingDiagnostic
        assertTrue(capture != null)
        fixture.session.requestExit(ExitAction.ToRoute("history/1"))
        assertTrue(fixture.session.state.value.sensitiveOperationRunning)
        runCurrent()
        fixture.session.resolvePending(PendingChoice.Stay)
        runCurrent()
        assertEquals(capture, fixture.session.state.value.pendingDiagnostic)
        assertNull(fixture.session.state.value.authorizedExit)
        assertEquals(false, fixture.session.state.value.sensitiveOperationRunning)
    }

    @Test
    fun pendingCaptureSaveAuthorizesExitOnlyAfterPersisting() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        assertTrue(fixture.session.state.value.pendingDiagnostic != null)
        fixture.session.requestExit(ExitAction.ToRoute("history/1"))
        runCurrent()
        fixture.session.resolvePending(PendingChoice.Save)
        runCurrent()
        assertEquals(1, fixture.diagnostics.saveCalls)
        assertNull(fixture.session.state.value.pendingDiagnostic)
        assertEquals(ExitAction.ToRoute("history/1"), fixture.session.state.value.authorizedExit?.action)
        assertNull(fixture.session.state.value.pendingDecision)
    }

    @Test
    fun pendingCaptureSaveFailureRetainsCaptureAndDoesNotAuthorizeExit() = runTest {
        val gateway = FakeGateway().apply { setState(ObdConnectionState.Connected(ObdConnectionConfig())) }
        val fixture = Fixture(gateway, backgroundScope)
        runCurrent()
        fixture.session.scanDiagnostic()
        runCurrent()
        val capture = fixture.session.state.value.pendingDiagnostic
        fixture.diagnostics.failSave = true
        fixture.session.requestExit(ExitAction.ToRoute("history/1"))
        runCurrent()
        fixture.session.resolvePending(PendingChoice.Save)
        runCurrent()
        assertEquals(capture, fixture.session.state.value.pendingDiagnostic)
        assertNull(fixture.session.state.value.authorizedExit)
        assertTrue(fixture.session.state.value.error != null)
        assertEquals(false, fixture.session.state.value.scanFailed)
        assertEquals(1, fixture.diagnostics.saveCalls)
    }

    @Test
    fun selectingDuringConnectCancelsAndWaitsForConfirmedClose() = runTest {
        val gateway = FakeGateway()
        val result = CompletableDeferred<ObdResult<Unit>>()
        gateway.connectResult = result
        val fixture = Fixture(gateway, backgroundScope)

        runCurrent()
        fixture.session.connect(ObdConnectionConfig(), localNetworkAllowed = true)
        runCurrent()
        fixture.session.selectVehicle(2L)
        runCurrent()

        assertEquals(ObdConnectionState.Disconnected, gateway.connectionState.value)
        assertEquals(2L, fixture.preferences.active.value)
        assertTrue(gateway.disconnectCalls >= 1)
    }
}

private class GatedVehicleRepository(private val gate: CompletableDeferred<Unit>) : VehicleRepository {
    val rows = MutableStateFlow<List<Vehicle>>(emptyList())
    var saveCalls = 0

    override fun observeVehicles(): Flow<List<Vehicle>> = rows
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> =
        MutableStateFlow(rows.value.firstOrNull { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = rows.value.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long {
        saveCalls++
        gate.await()
        val saved = vehicle.copy(id = 41L)
        rows.value = rows.value + saved
        return saved.id
    }
    override suspend fun deleteVehicle(vehicleId: Long) {
        rows.value = rows.value.filterNot { it.id == vehicleId }
    }
}

private class Fixture(gateway: FakeGateway, scope: kotlinx.coroutines.CoroutineScope) {
    val vehicles = FakeVehicles()
    val preferences = FakePreferences()
    val diagnostics = FakeDiagnostics()
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

private class FakeVehicles : VehicleRepository {
    private val rows = listOf(
        Vehicle(1L, "A", "B", 2020, "Gasoline", "2.0", 10),
        Vehicle(2L, "C", "D", 2021, "Diesel", "1.6", 20)
    )
    val deleted = mutableListOf<Long>()
    var failDelete = false
    var saveCalls = 0
    override fun observeVehicles(): Flow<List<Vehicle>> = MutableStateFlow(rows)
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> =
        MutableStateFlow(rows.firstOrNull { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = rows.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long { saveCalls++; return vehicle.id }
    override suspend fun deleteVehicle(vehicleId: Long) {
        if (failDelete) error("Room write failed")
        deleted += vehicleId
    }
}

private class FakePreferences : AppPreferencesRepository {
    val active = MutableStateFlow<Long?>(1L)
    var failNextSet = false
    var setCalls = 0
    var failNextConfigSave = false
    var configSaveCalls = 0
    var lastSavedConfig: ObdConnectionConfig? = null
    override val activeVehicleId: Flow<Long?> = active
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig()).asStateFlow()
    override val settings = MutableStateFlow(AppSettings()).asStateFlow()
    override suspend fun setActiveVehicleId(vehicleId: Long?) {
        setCalls++
        if (failNextSet) { failNextSet = false; error("DataStore failed") }
        active.value = vehicleId
    }
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) {
        configSaveCalls++
        if (failNextConfigSave) { failNextConfigSave = false; error("DataStore failed") }
        lastSavedConfig = config
    }
    override suspend fun saveSettings(settings: AppSettings) = Unit
}

private class FakeGateway : ObdGateway {
    private val mutableState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    override val connectionState: StateFlow<ObdConnectionState> = mutableState.asStateFlow()
    var connectResult = CompletableDeferred<ObdResult<Unit>>()
    var disconnectCalls = 0
    var connectCalls = 0
    var scanResult: ObdResult<List<String>> = ObdResult.Success(emptyList())
    var scanGate: CompletableDeferred<Unit>? = null

    override suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> {
        connectCalls++
        mutableState.value = ObdConnectionState.Connecting
        return try {
            connectResult.await().also {
                if (it is ObdResult.Success) mutableState.value = ObdConnectionState.Connected(config)
                if (it is ObdResult.Failure) mutableState.value = ObdConnectionState.Error(it.message)
            }
        } catch (cancelled: CancellationException) {
            mutableState.value = ObdConnectionState.Disconnected
            throw cancelled
        }
    }

    override suspend fun disconnect() {
        disconnectCalls++
        mutableState.value = ObdConnectionState.Disconnected
    }
    override suspend fun readLiveMetrics(): ObdResult<ObdLiveMetrics> =
        ObdResult.Success(ObdLiveMetrics(0, null, 0, 1L))
    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> {
        scanGate?.await()
        return scanResult
    }
    fun setState(state: ObdConnectionState) { mutableState.value = state }
}

private class FakeDiagnostics : DiagnosticRepository {
    var failSave = false
    var saveCalls = 0
    var saveGate: CompletableDeferred<Unit>? = null
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> =
        MutableStateFlow(emptyList())
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long {
        saveCalls++
        saveGate?.await()
        if (failSave) error("DB failure")
        return 1L
    }
    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) = emptySet<String>()
    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) = emptyMap<String, Int>()
    override fun getDtcGuidance(code: String) = DtcGuidance(
        code = code, description = "", relatedSystem = "", possibleCauses = emptyList(),
        orientativeActions = emptyList(), isKnownInCatalog = false
    )
}
