package cl.cristian.bitacoraobd.ui

import cl.cristian.bitacoraobd.domain.model.DiagnosticScanResult
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdLiveMetrics
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import cl.cristian.bitacoraobd.domain.usecase.DeleteVehicleResult
import cl.cristian.bitacoraobd.domain.usecase.DeleteVehicleUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveDiagnosticSessionUseCase
import cl.cristian.bitacoraobd.domain.usecase.ScanDiagnosticCodesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface PendingChoice {
    data object Save : PendingChoice
    data object Discard : PendingChoice
    data object Stay : PendingChoice
}

sealed interface ExitAction {
    data object Back : ExitAction
    data class ToRoute(val route: String) : ExitAction
}

data class AuthorizedExit(val id: Long, val action: ExitAction)

data class PendingDecision(val id: Long, val action: ExitAction?)

enum class VehicleLoadStatus { Loading, Ready, Failed }
enum class VehicleAction { OpenEditor, Save, Select, Delete }
enum class VehicleRecovery { None, RetrySave, RetrySelection, RefreshOnly, Reconcile }
data class VehicleActionResult(
    val id: Long,
    val action: VehicleAction,
    val vehicleId: Long?,
    val vehicle: Vehicle? = null,
    val error: String? = null,
    val recovery: VehicleRecovery = VehicleRecovery.None
)

data class AppSessionState(
    val vehicles: List<Vehicle> = emptyList(),
    val vehicleLoadStatus: VehicleLoadStatus = VehicleLoadStatus.Loading,
    val vehicleActionResult: VehicleActionResult? = null,
    val activeVehicleId: Long? = null,
    val pendingDiagnostic: DiagnosticScanResult.Success? = null,
    val savedDiagnosticId: Long? = null,
    val metrics: Pair<Long, ObdLiveMetrics>? = null,
    val operation: String? = null,
    val sensitiveOperationRunning: Boolean = false,
    val error: String? = null,
    val pendingDecision: PendingDecision? = null,
    val authorizedExit: AuthorizedExit? = null
)

class AppSession(
    private val vehicles: VehicleRepository,
    private val preferences: AppPreferencesRepository,
    private val gateway: ObdGateway,
    private val scanDiagnostic: ScanDiagnosticCodesUseCase,
    private val saveDiagnostic: SaveDiagnosticSessionUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(AppSessionState())
    val state: StateFlow<AppSessionState> = mutableState.asStateFlow()
    val connectionState get() = gateway.connectionState

    private var connectJob: Job? = null
    private var closeJob: Job? = null
    private var saveJob: Job? = null
    private var readJob: Job? = null
    private var decision: CompletableDeferred<PendingChoice>? = null
    private var pendingExit: ExitAction? = null
    private var sensitiveOperationRunning = false
    private var exitId = 0L
    private var contextRefreshFailed = false
    private var vehicleResultId = 0L

    init { refreshVehicleContext() }

    fun refreshVehicleContext() {
        scope.launch {
            try {
                mutex.withLock { refreshVehicleContextLocked() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                contextRefreshFailed = true
                mutableState.value = mutableState.value.copy(vehicleLoadStatus = VehicleLoadStatus.Failed, error = error.message ?: "Could not load vehicles")
            }
        }
    }

    private suspend fun refreshVehicleContextLocked(clearError: Boolean = true) {
        val all = vehicles.observeVehicles().first()
        val stored = preferences.activeVehicleId.first()
        val active = all.firstOrNull { it.id == stored } ?: all.firstOrNull()
        if (stored != active?.id) preferences.setActiveVehicleId(active?.id)
        contextRefreshFailed = false
        mutableState.value = mutableState.value.copy(
            vehicles = all,
            vehicleLoadStatus = VehicleLoadStatus.Ready,
            activeVehicleId = active?.id,
            error = if (clearError) null else mutableState.value.error
        )
    }

    fun consumeVehicleResult(resultId: Long) {
        if (mutableState.value.vehicleActionResult?.id == resultId) {
            mutableState.value = mutableState.value.copy(vehicleActionResult = null)
        }
    }

    fun retryVehicleRecovery(resultId: Long) {
        val result = mutableState.value.vehicleActionResult?.takeIf { it.id == resultId } ?: return
        when (result.recovery) {
            VehicleRecovery.RetrySelection -> result.vehicleId?.let(::retryVehicleSelection)
            VehicleRecovery.RefreshOnly, VehicleRecovery.Reconcile -> scope.launch {
                try {
                    mutex.withLock {
                        if (mutableState.value.vehicleActionResult?.id != resultId) return@withLock
                        refreshVehicleContextLocked()
                        if (result.action == VehicleAction.Select && result.vehicleId != null &&
                            mutableState.value.activeVehicleId == result.vehicleId
                        ) {
                            publishVehicleResult(VehicleAction.Select, result.vehicleId, vehicle = vehicles.getVehicleById(result.vehicleId))
                            authorizeExit(ExitAction.ToRoute("home"))
                        } else {
                            consumeVehicleResult(resultId)
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    contextRefreshFailed = true
                    mutableState.value = mutableState.value.copy(
                        vehicleLoadStatus = VehicleLoadStatus.Failed,
                        error = error.message ?: "Could not refresh vehicle state"
                    )
                }
            }
            VehicleRecovery.None, VehicleRecovery.RetrySave -> Unit
        }
    }

    fun openVehicleEditor(vehicleId: Long) {
        runSensitive(null) {
            cancelConnectingAndAwait()
            mutex.withLock {
                if (contextRefreshFailed || mutableState.value.pendingDiagnostic != null) return@withLock
                val vehicle = vehicles.getVehicleById(vehicleId)
                if (vehicle == null) {
                    publishVehicleResult(VehicleAction.OpenEditor, vehicleId, error = "Vehicle no longer exists")
                } else if (vehicleId == mutableState.value.activeVehicleId &&
                    gateway.connectionState.value is ObdConnectionState.Connected
                ) {
                    publishVehicleResult(VehicleAction.OpenEditor, vehicleId, error = "Disconnect OBD before editing this vehicle")
                } else {
                    publishVehicleResult(VehicleAction.OpenEditor, vehicleId, vehicle = vehicle)
                }
            }
        }
    }

    fun saveVehicle(vehicle: Vehicle, original: Vehicle? = null) {
        runSensitive(null) {
            cancelConnectingAndAwait()
            if (gateway.connectionState.value !is ObdConnectionState.Disconnected) {
                mutex.withLock {
                    if (vehicle.id == mutableState.value.activeVehicleId) {
                        publishVehicleResult(VehicleAction.Save, vehicle.id.takeIf { it > 0 }, error = "Disconnect OBD before editing this vehicle")
                    } else {
                        saveVehicleLocked(vehicle, original)
                    }
                }
            } else mutex.withLock { saveVehicleLocked(vehicle, original) }
        }
    }

    private suspend fun saveVehicleLocked(vehicle: Vehicle, original: Vehicle?) {
        if (contextRefreshFailed || mutableState.value.pendingDiagnostic != null) return
        val existing = vehicle.id.takeIf { it > 0 }?.let { vehicles.getVehicleById(it) }
        if (original != null && (existing == null || existing != original)) {
            publishVehicleResult(VehicleAction.Save, vehicle.id, error = "Vehicle changed. Reload before saving", recovery = VehicleRecovery.Reconcile)
            return
        }
        mutableState.value = mutableState.value.copy(operation = "vehicle-save", error = null)
        var persistedId: Long? = null
        val isNew = vehicle.id == 0L
        var firstVehicle = false
        try {
            refreshVehicleContextLocked()
            firstVehicle = isNew && mutableState.value.vehicles.isEmpty()
            if (isNew) {
                withContext(NonCancellable) {
                    persistedId = vehicles.saveVehicle(vehicle)
                    publishVehicleResult(
                        VehicleAction.Save,
                        persistedId,
                        vehicle = vehicle.copy(id = requireNotNull(persistedId)),
                        error = "Vehicle saved; selection is not confirmed",
                        recovery = if (firstVehicle) VehicleRecovery.RetrySelection else VehicleRecovery.RefreshOnly
                    )
                }
                if (firstVehicle) preferences.setActiveVehicleId(requireNotNull(persistedId))
            } else {
                persistedId = vehicles.saveVehicle(vehicle)
            }
            val savedId = requireNotNull(persistedId)
            refreshVehicleContextLocked(clearError = false)
            publishVehicleResult(VehicleAction.Save, savedId, vehicle = vehicle.copy(id = savedId))
            if (firstVehicle && preferences.activeVehicleId.first() == savedId) authorizeExit(ExitAction.ToRoute("home"))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val recovery = when {
                isNew && persistedId != null -> VehicleRecovery.RetrySelection
                persistedId != null -> VehicleRecovery.RefreshOnly
                else -> VehicleRecovery.Reconcile
            }
            publishVehicleResult(VehicleAction.Save, persistedId ?: vehicle.id.takeIf { it > 0 },
                vehicle = persistedId?.let { vehicle.copy(id = it) },
                error = error.message ?: "Could not save vehicle", recovery = recovery)
        } finally {
            mutableState.value = mutableState.value.copy(operation = null)
        }
    }

    fun retryVehicleSelection(vehicleId: Long) {
        runSensitive(null) {
            mutex.withLock {
                if (vehicleId <= 0 || contextRefreshFailed || gateway.connectionState.value !is ObdConnectionState.Disconnected) return@withLock
                mutableState.value = mutableState.value.copy(operation = "vehicle-select", error = null)
                try {
                    if (vehicles.getVehicleById(vehicleId) == null) {
                        publishVehicleResult(VehicleAction.Select, vehicleId, error = "Vehicle no longer exists", recovery = VehicleRecovery.RefreshOnly)
                    } else {
                        if (preferences.activeVehicleId.first() != vehicleId) preferences.setActiveVehicleId(vehicleId)
                        refreshVehicleContextLocked(clearError = false)
                        publishVehicleResult(VehicleAction.Select, vehicleId, vehicle = vehicles.getVehicleById(vehicleId))
                        authorizeExit(ExitAction.ToRoute("home"))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    publishVehicleResult(VehicleAction.Select, vehicleId, error = error.message ?: "Could not select vehicle",
                        recovery = VehicleRecovery.RetrySelection)
                } finally { mutableState.value = mutableState.value.copy(operation = null) }
            }
        }
    }

    private fun publishVehicleResult(
        action: VehicleAction,
        vehicleId: Long?,
        vehicle: Vehicle? = null,
        error: String? = null,
        recovery: VehicleRecovery = VehicleRecovery.None
    ) {
        mutableState.value = mutableState.value.copy(
            vehicleActionResult = VehicleActionResult(++vehicleResultId, action, vehicleId, vehicle, error, recovery),
            error = error
        )
    }

    fun connect(config: ObdConnectionConfig, localNetworkAllowed: Boolean) {
        if (!localNetworkAllowed || connectJob?.isActive == true || closeJob?.isActive == true) return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                if (gateway.connectionState.value is ObdConnectionState.Error) gateway.disconnect()
                mutex.withLock {
                    if (contextRefreshFailed) return@withLock
                    refreshVehicleContextLocked()
                    val activeId = preferences.activeVehicleId.first()
                    if (activeId == null || vehicles.getVehicleById(activeId) == null ||
                        sensitiveOperationRunning || pendingExit != null || mutableState.value.pendingDiagnostic != null ||
                        gateway.connectionState.value !is ObdConnectionState.Disconnected
                    ) return@withLock
                    mutableState.value = mutableState.value.copy(operation = "connect", error = null)
                    try {
                        when (val result = gateway.connect(config)) {
                            is ObdResult.Success -> Unit
                            is ObdResult.Failure -> mutableState.value = mutableState.value.copy(error = result.message)
                        }
                    } finally {
                        mutableState.value = mutableState.value.copy(operation = null)
                    }
                }
            } finally {
                if (connectJob === currentCoroutineContext()[Job]) connectJob = null
            }
        }
        connectJob = job
        job.start()
    }

    fun cancelConnection() {
        scope.launch { cancelConnectingAndAwait() }
    }

    fun selectVehicle(vehicleId: Long) {
        runSensitive(null) {
            cancelConnectingAndAwait()
            mutex.withLock {
                if (contextRefreshFailed || mutableState.value.pendingDiagnostic != null) return@withLock
                if (gateway.connectionState.value !is ObdConnectionState.Disconnected) {
                    publishVehicleResult(VehicleAction.Select, vehicleId, error = "Disconnect before changing vehicles")
                    return@withLock
                }
                if (vehicles.getVehicleById(vehicleId) == null) {
                    publishVehicleResult(VehicleAction.Select, vehicleId, error = "Vehicle no longer exists", recovery = VehicleRecovery.RefreshOnly)
                    return@withLock
                }
                try {
                    preferences.setActiveVehicleId(vehicleId)
                    refreshVehicleContextLocked()
                    publishVehicleResult(VehicleAction.Select, vehicleId, vehicle = vehicles.getVehicleById(vehicleId))
                    authorizeExit(ExitAction.ToRoute("home"))
                } catch (cancelled: CancellationException) {
                    contextRefreshFailed = true
                    throw cancelled
                } catch (error: Exception) {
                    contextRefreshFailed = true
                    publishVehicleResult(VehicleAction.Select, vehicleId, error = error.message ?: "Could not select vehicle",
                        recovery = VehicleRecovery.Reconcile)
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: Long) {
        runSensitive(null) {
            cancelConnectingAndAwait()
            mutex.withLock {
                if (contextRefreshFailed || mutableState.value.pendingDiagnostic != null || gateway.connectionState.value !is ObdConnectionState.Disconnected) return@withLock
                mutableState.value = mutableState.value.copy(operation = "delete", error = null)
                var refreshRequired = false
                try {
                    when (deleteVehicleUseCase(vehicleId)) {
                        DeleteVehicleResult.Deleted -> {
                            refreshVehicleContextLocked()
                            publishVehicleResult(VehicleAction.Delete, vehicleId)
                            if (mutableState.value.vehicles.isEmpty()) authorizeExit(ExitAction.ToRoute("home"))
                        }
                        DeleteVehicleResult.Connected -> publishVehicleResult(
                            VehicleAction.Delete, vehicleId, error = "Disconnect before changing vehicles"
                        )
                        DeleteVehicleResult.NotFound -> publishVehicleResult(
                            VehicleAction.Delete, vehicleId, error = "Vehicle no longer exists", recovery = VehicleRecovery.RefreshOnly
                        )
                        DeleteVehicleResult.Failed -> {
                            refreshRequired = true
                            publishVehicleResult(VehicleAction.Delete, vehicleId, error = "Could not delete vehicle",
                                recovery = VehicleRecovery.Reconcile)
                        }
                    }
                } catch (cancelled: CancellationException) {
                    refreshRequired = true
                    throw cancelled
                } catch (error: Exception) {
                    refreshRequired = true
                    publishVehicleResult(VehicleAction.Delete, vehicleId,
                        error = error.message ?: "Could not delete vehicle", recovery = VehicleRecovery.Reconcile)
                } finally {
                    mutableState.value = mutableState.value.copy(operation = null)
                    if (refreshRequired) runCatching { refreshVehicleContextLocked(clearError = false) }
                        .onFailure { contextRefreshFailed = true }
                }
                if (refreshRequired) publishVehicleResult(VehicleAction.Delete, vehicleId,
                    error = mutableState.value.error ?: "Could not delete vehicle", recovery = VehicleRecovery.Reconcile)
            }
        }
    }

    fun scanDiagnostic() {
        if (sensitiveOperationRunning || pendingExit != null || mutableState.value.pendingDiagnostic != null || mutableState.value.operation != null) return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            mutex.withLock {
                if (sensitiveOperationRunning || pendingExit != null || mutableState.value.pendingDiagnostic != null || mutableState.value.operation != null ||
                    gateway.connectionState.value !is ObdConnectionState.Connected
                ) return@withLock
                val active = preferences.activeVehicleId.first() ?: return@withLock
                mutableState.value = mutableState.value.copy(operation = "scan", error = null)
                try {
                    when (val result = scanDiagnostic(active)) {
                        is DiagnosticScanResult.Success -> mutableState.value =
                            mutableState.value.copy(pendingDiagnostic = result)
                        is DiagnosticScanResult.Failure -> mutableState.value =
                            mutableState.value.copy(error = result.message)
                    }
                } finally {
                    mutableState.value = mutableState.value.copy(operation = null)
                }
            }
        }
        readJob = job
        job.start()
    }

    fun readMetrics() {
        if (sensitiveOperationRunning || pendingExit != null || mutableState.value.operation != null) return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            mutex.withLock {
                if (sensitiveOperationRunning || pendingExit != null || mutableState.value.operation != null ||
                    gateway.connectionState.value !is ObdConnectionState.Connected
                ) return@withLock
                val active = preferences.activeVehicleId.first() ?: return@withLock
                mutableState.value = mutableState.value.copy(operation = "metrics", error = null)
                try {
                    when (val result = gateway.readLiveMetrics()) {
                        is ObdResult.Success -> mutableState.value = mutableState.value.copy(metrics = active to result.value)
                        is ObdResult.Failure -> mutableState.value = mutableState.value.copy(error = result.message)
                    }
                } finally {
                    mutableState.value = mutableState.value.copy(operation = null)
                }
            }
        }
        readJob = job
        job.start()
    }

    fun savePendingDiagnostic() {
        if (saveJob?.isActive == true) return
        val pending = mutableState.value.pendingDiagnostic ?: return
        val job = scope.launch(start = CoroutineStart.LAZY) {
            mutableState.value = mutableState.value.copy(operation = "save", error = null)
            try {
                val id = saveDiagnostic(pending) ?: error("Diagnostic capture was not saved")
                if (mutableState.value.pendingDiagnostic?.captureId == pending.captureId) {
                    mutableState.value = mutableState.value.copy(pendingDiagnostic = null, savedDiagnosticId = id, error = null)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(error = error.message ?: "Could not save diagnostic")
            } finally {
                mutableState.value = mutableState.value.copy(operation = null)
                saveJob = null
            }
        }
        saveJob = job
        job.start()
    }

    fun resolvePending(choice: PendingChoice) {
        decision?.complete(choice)
    }

    fun disconnect() {
        runSensitive(null) { cancelConnectingAndAwait(); gateway.disconnect() }
    }

    fun requestExit(action: ExitAction) {
        if (action is ExitAction.ToRoute && !cl.cristian.bitacoraobd.ui.navigation.AppRoute.isAllowedTarget(action.route)) {
            mutableState.value = mutableState.value.copy(error = "Destino de navegación no válido")
            return
        }
        runSensitive(action) {
        if (gateway.connectionState.value is ObdConnectionState.Connecting) cancelConnectingAndAwait()
        val reading = readJob
        if (reading?.isActive == true) {
            reading.cancel()
            if (mutableState.value.operation == "scan") runCatching { gateway.disconnect() }
            reading.join()
            readJob = null
        }
        authorizeExit(action)
        }
    }

    fun completeExit(authorizationId: Long) {
        if (mutableState.value.authorizedExit?.id == authorizationId) {
            mutableState.value = mutableState.value.copy(authorizedExit = null, error = null)
            pendingExit = null
        }
    }

    fun failExit(authorizationId: Long, message: String) {
        if (mutableState.value.authorizedExit?.id == authorizationId) {
            mutableState.value = mutableState.value.copy(authorizedExit = null, error = message)
            pendingExit = null
        }
    }

    private fun runSensitive(action: ExitAction?, operation: suspend () -> Unit) {
        if (decision?.isActive == true || sensitiveOperationRunning || pendingExit != null) return
        sensitiveOperationRunning = true
        mutableState.value = mutableState.value.copy(sensitiveOperationRunning = true)
        pendingExit = action
        scope.launch {
            try {
                while (true) {
                    while (true) {
                        val capture = mutableState.value.pendingDiagnostic ?: break
                        val deferred = CompletableDeferred<PendingChoice>()
                        decision = deferred
                        mutableState.value = mutableState.value.copy(pendingDecision = PendingDecision(++exitId, action))
                        val choice = deferred.await()
                        mutableState.value = mutableState.value.copy(pendingDecision = null)
                        decision = null
                        when (choice) {
                            PendingChoice.Stay -> return@launch
                            PendingChoice.Discard -> {
                                if (mutableState.value.pendingDiagnostic?.captureId == capture.captureId) {
                                    mutableState.value = mutableState.value.copy(pendingDiagnostic = null)
                                }
                            }
                            PendingChoice.Save -> {
                                savePendingDiagnostic()
                                saveJob?.join()
                                if (mutableState.value.error != null) return@launch
                            }
                        }
                        if (mutableState.value.pendingDiagnostic != null) continue
                    }
                    if (saveJob?.isActive == true) {
                        saveJob?.join()
                        if (mutableState.value.error != null) return@launch
                    }
                    cancelConnectingAndAwait()
                    if (mutableState.value.pendingDiagnostic != null) continue
                    operation()
                    if (mutableState.value.pendingDiagnostic == null) break
                }
            } finally {
                if (decision?.isActive == true) decision?.cancel()
                decision = null
                mutableState.value = mutableState.value.copy(pendingDecision = null)
                sensitiveOperationRunning = false
                mutableState.value = mutableState.value.copy(sensitiveOperationRunning = false)
                if (mutableState.value.authorizedExit == null) pendingExit = null
            }
        }
    }

    private fun authorizeExit(action: ExitAction) {
        if (mutableState.value.pendingDiagnostic != null || saveJob?.isActive == true) return
        mutableState.value = mutableState.value.copy(authorizedExit = AuthorizedExit(++exitId, action))
    }

    private suspend fun cancelConnectingAndAwait() {
        if (gateway.connectionState.value is ObdConnectionState.Connected) return
        val attempt = connectJob
        if (attempt?.isActive != true &&
            gateway.connectionState.value !is ObdConnectionState.Connecting &&
            gateway.connectionState.value !is ObdConnectionState.Disconnecting &&
            gateway.connectionState.value !is ObdConnectionState.Error
        ) return

        val close = closeJob ?: scope.launch(start = CoroutineStart.LAZY) {
            attempt?.cancel()
            runCatching { gateway.disconnect() }
            attempt?.join()
            gateway.disconnect()
        }.also { closeJob = it; it.start() }
        close.join()
        closeJob = null
    }
}
