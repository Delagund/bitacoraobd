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
import kotlinx.coroutines.CompletableDeferred
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
    fun ordinaryVehicleAndDisconnectOperationsDoNotAuthorizeNavigation() = runTest {
        val gateway = FakeGateway()
        val fixture = Fixture(gateway, backgroundScope)

        runCurrent()
        fixture.session.selectVehicle(2L)
        runCurrent()
        fixture.session.deleteVehicle(1L)
        runCurrent()
        fixture.session.disconnect()
        runCurrent()

        assertNull(fixture.session.state.value.authorizedExit)
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
        runCurrent()
        fixture.session.resolvePending(PendingChoice.Stay)
        runCurrent()
        assertEquals(capture, fixture.session.state.value.pendingDiagnostic)
        assertNull(fixture.session.state.value.authorizedExit)
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
    override fun observeVehicles(): Flow<List<Vehicle>> = MutableStateFlow(rows)
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> =
        MutableStateFlow(rows.firstOrNull { it.id == vehicleId })
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = rows.firstOrNull { it.id == vehicleId }
    override suspend fun saveVehicle(vehicle: Vehicle): Long = vehicle.id
    override suspend fun deleteVehicle(vehicleId: Long) {
        if (failDelete) error("Room write failed")
        deleted += vehicleId
    }
}

private class FakePreferences : AppPreferencesRepository {
    val active = MutableStateFlow<Long?>(1L)
    override val activeVehicleId: Flow<Long?> = active
    override val obdConnectionConfig = MutableStateFlow(ObdConnectionConfig()).asStateFlow()
    override val settings = MutableStateFlow(AppSettings()).asStateFlow()
    override suspend fun setActiveVehicleId(vehicleId: Long?) { active.value = vehicleId }
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) = Unit
    override suspend fun saveSettings(settings: AppSettings) = Unit
}

private class FakeGateway : ObdGateway {
    private val mutableState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    override val connectionState: StateFlow<ObdConnectionState> = mutableState.asStateFlow()
    var connectResult = CompletableDeferred<ObdResult<Unit>>()
    var disconnectCalls = 0

    override suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> {
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
        ObdResult.Failure("not used")
    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> =
        ObdResult.Success(emptyList())
    fun setState(state: ObdConnectionState) { mutableState.value = state }
}

private class FakeDiagnostics : DiagnosticRepository {
    var failSave = false
    var saveCalls = 0
    override fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>> =
        MutableStateFlow(emptyList())
    override suspend fun getLatestSession(vehicleId: Long): DiagnosticSession? = null
    override suspend fun getSessionById(sessionId: Long): DiagnosticSession? = null
    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long {
        saveCalls++
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
