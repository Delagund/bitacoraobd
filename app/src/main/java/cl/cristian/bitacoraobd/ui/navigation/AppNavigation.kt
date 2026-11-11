package cl.cristian.bitacoraobd.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import cl.cristian.bitacoraobd.ui.AppSession
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesScreen
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesViewModel
import cl.cristian.bitacoraobd.ui.ExitAction
import cl.cristian.bitacoraobd.ui.PendingChoice
import cl.cristian.bitacoraobd.ui.connection.ConnectionViewModel
import cl.cristian.bitacoraobd.ui.connection.ConnectionScreen
import cl.cristian.bitacoraobd.ui.diagnostic.DiagnosticScreen
import cl.cristian.bitacoraobd.ui.history.HistoryScreen
import cl.cristian.bitacoraobd.ui.history.DiagnosticDetailScreen
import cl.cristian.bitacoraobd.ui.history.HistoryViewModel
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceScreen
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceViewModel
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.settings.SettingsViewModel
import cl.cristian.bitacoraobd.ui.home.HomeScreen
import cl.cristian.bitacoraobd.ui.settings.SettingsScreen
import cl.cristian.bitacoraobd.ui.theme.PrototypeOutline
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CancellationException

object AppRoute {
    const val HOME = "home"
    const val VEHICLES = "vehicles"
    const val DIAGNOSTIC = "diagnostic"
    const val MAINTENANCE = "maintenance"
    const val SETTINGS = "settings"
    const val HISTORY = "history/{vehicleId}"
    const val DIAGNOSTIC_DETAIL = "diagnostic-detail/{vehicleId}/{sessionId}"
    const val CONNECTION_GRAPH = "connection-flow"
    const val CONNECTION = "connection"

    val primaryRoutes = setOf(HOME, VEHICLES, DIAGNOSTIC, MAINTENANCE)

    fun history(vehicleId: Long) = "history/$vehicleId"
    fun diagnosticDetail(vehicleId: Long, sessionId: Long) = "diagnostic-detail/$vehicleId/$sessionId"

    fun isAllowedTarget(route: String): Boolean =
        route in primaryRoutes || route == SETTINGS || route == CONNECTION ||
            route.toLongRouteId("history/") || route.matches(Regex("diagnostic-detail/[1-9]\\d*/[1-9]\\d*"))

    private fun String.toLongRouteId(prefix: String): Boolean =
        startsWith(prefix) && removePrefix(prefix).toLongOrNull()?.let { it > 0L } == true
}

@androidx.compose.runtime.Composable
private fun EmptyVehicleDestination(title: String, session: AppSession) {
    val state by session.state.collectAsState()
    if (state.vehicleLoadStatus.name == "Ready" && state.vehicles.isEmpty()) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text("Agrega un vehículo para continuar")
            androidx.compose.material3.Button(
                onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.VEHICLES)) }
            ) { Text("Agregar vehículo") }
        }
    } else Text(title)
}

@androidx.compose.runtime.Composable
fun AppNavigation(navController: NavHostController, session: AppSession, vehiclesViewModel: VehiclesViewModel,
                  connectionViewModel: ConnectionViewModel, historyViewModel: HistoryViewModel,
                  maintenanceViewModel: MaintenanceViewModel, homeViewModel: HomeViewModel,
                  settingsViewModel: SettingsViewModel, diagnostics: DiagnosticRepository,
                  preferences: AppPreferencesRepository, onBackAtRoot: () -> Unit) {
    LaunchedEffect(navController, session) {
        session.state.collect { state ->
            val exit = state.authorizedExit ?: return@collect
            val fromConnection = navController.currentDestination?.route == AppRoute.CONNECTION
            val consumed = when (val action = exit.action) {
                ExitAction.Back -> if (navController.popBackStack()) true else {
                    onBackAtRoot()
                    true
                }
                is ExitAction.ToRoute -> {
                    val route = if (fromConnection) AppRoute.DIAGNOSTIC else action.route
                    if (!AppRoute.isAllowedTarget(route)) {
                        false
                    } else {
                        try {
                            if (route in AppRoute.primaryRoutes) {
                                navController.navigate(route) {
                                    popUpTo(AppRoute.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(route) { launchSingleTop = true }
                            }
                            true
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            false
                        }
                    }
                }
            }
            if (consumed) {
                session.completeExit(exit.id)
            } else {
                session.failExit(exit.id, "No se pudo abrir el destino solicitado")
            }
        }
    }

    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
    val lifecycleOwner = LocalLifecycleOwner.current
    val resumed = androidx.compose.runtime.remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumed.value = true
            if (event == Lifecycle.Event.ON_PAUSE) resumed.value = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(currentRoute?.destination?.route, resumed.value) {
        homeViewModel.setActive(currentRoute?.destination?.route == AppRoute.HOME && resumed.value)
    }
    val sessionState by session.state.collectAsState()
    var connectionLeavePending by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ExitAction?>(null) }
    var maintenanceLeavePending by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ExitAction?>(null) }
    LaunchedEffect(maintenanceViewModel.queuedLeave, maintenanceViewModel.queuedLeaveSucceeded) {
        val (target, succeeded) = maintenanceViewModel.consumeQueuedLeave() ?: return@LaunchedEffect
        val action = if (target == "back") ExitAction.Back else ExitAction.ToRoute(target)
        if (succeeded) session.requestExit(action)
        else maintenanceLeavePending = action
    }
    LaunchedEffect(maintenanceViewModel.pendingLeave) {
        val target = maintenanceViewModel.pendingLeave ?: return@LaunchedEffect
        maintenanceLeavePending = if (target == "back") ExitAction.Back else ExitAction.ToRoute(target)
    }
    LaunchedEffect(settingsViewModel.saving, settingsViewModel.error, settingsViewModel.leaveTarget) {
        val target = settingsViewModel.takeSuccessfulLeaveTarget() ?: return@LaunchedEffect
        if (target == "back") navController.popBackStack()
        else session.requestExit(ExitAction.ToRoute(target))
    }
    LaunchedEffect(sessionState.operation, sessionState.sensitiveOperationRunning, vehiclesViewModel.queuedExitRoute, vehiclesViewModel.queuedExitSaveSucceeded) {
        if (sessionState.operation == null && !sessionState.sensitiveOperationRunning) vehiclesViewModel.settleQueuedExit()
    }
    BackHandler {
        if (currentRoute?.destination?.route == AppRoute.MAINTENANCE) {
            maintenanceViewModel.requestLeave("back")
        } else if (currentRoute?.destination?.route == AppRoute.VEHICLES) {
            if (vehiclesViewModel.draft != null) vehiclesViewModel.requestBackFromForm()
            else session.requestExit(ExitAction.Back)
        } else if (currentRoute?.destination?.route == AppRoute.CONNECTION) {
            if (connectionViewModel.dirty) connectionLeavePending = ExitAction.Back
            else session.requestExit(ExitAction.ToRoute(AppRoute.DIAGNOSTIC))
        } else if (currentRoute?.destination?.route == AppRoute.SETTINGS) {
            if (settingsViewModel.dirty) settingsViewModel.requestLeave("back")
            else navController.popBackStack()
        } else session.requestExit(ExitAction.Back)
    }
    sessionState.pendingDecision?.let {
        AlertDialog(
            onDismissRequest = { session.resolvePending(PendingChoice.Stay) },
            title = { Text("Diagnóstico sin guardar") },
            text = { Text("¿Guardar esta captura antes de salir?") },
            confirmButton = { TextButton(onClick = { session.resolvePending(PendingChoice.Save) }) { Text("Guardar") } },
            dismissButton = {
                TextButton(onClick = { session.resolvePending(PendingChoice.Discard) }) { Text("Descartar") }
                TextButton(onClick = { session.resolvePending(PendingChoice.Stay) }) { Text("Seguir aquí") }
            }
        )
    }
    connectionLeavePending?.let { action ->
        AlertDialog(
            onDismissRequest = { connectionLeavePending = null },
            title = { Text("Descartar cambios") },
            text = { Text("Se perderán los cambios de conexión.") },
            confirmButton = { TextButton(onClick = {
                connectionViewModel.discard()
                connectionLeavePending = null
                session.requestExit(action)
            }) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { connectionLeavePending = null }) { Text("Seguir editando") } }
        )
    }
    maintenanceLeavePending?.let { action ->
        AlertDialog(
            onDismissRequest = { maintenanceLeavePending = null },
            title = { Text("Descartar cambios") },
            text = { Text("Se perderán los cambios del mantenimiento.") },
            confirmButton = { TextButton(onClick = {
                maintenanceViewModel.confirmLeave()
                maintenanceLeavePending = null
                session.requestExit(action)
            }) { Text("Descartar y salir") } },
            dismissButton = { TextButton(onClick = {
                maintenanceViewModel.keepEditing()
                maintenanceLeavePending = null
            }) { Text("Seguir editando") } }
        )
    }
    vehiclesViewModel.pendingLeaveRoute?.let {
        AlertDialog(
            onDismissRequest = vehiclesViewModel::keepEditing,
            title = { Text("Descartar cambios") },
            text = { Text("Se perderán los cambios del vehículo.") },
            confirmButton = {
                TextButton(onClick = { vehiclesViewModel.confirmLeave { session.requestExit(ExitAction.ToRoute(it)) } }) {
                    Text("Descartar y salir")
                }
            },
            dismissButton = { TextButton(onClick = vehiclesViewModel::keepEditing) { Text("Seguir editando") } }
        )
    }
    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.border(1.dp, PrototypeOutline)) {
                listOf(
                    AppRoute.HOME to "Inicio",
                    AppRoute.VEHICLES to "Vehículos",
                    AppRoute.DIAGNOSTIC to "Diagnóstico",
                    AppRoute.MAINTENANCE to "Mantenimiento"
                ).forEach { (route, label) ->
                    val destinationRoute = currentRoute?.destination?.route
                    val selected = if (route == AppRoute.DIAGNOSTIC) {
                        destinationRoute == AppRoute.DIAGNOSTIC || destinationRoute == AppRoute.CONNECTION
                    } else destinationRoute == route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute?.destination?.route == AppRoute.SETTINGS) {
                                settingsViewModel.requestLeave(route)
                                if (!settingsViewModel.dirty) session.requestExit(ExitAction.ToRoute(route))
                            } else if (currentRoute?.destination?.route == AppRoute.MAINTENANCE) {
                                maintenanceViewModel.requestLeave(route)
                            } else if (currentRoute?.destination?.route == AppRoute.CONNECTION) {
                                if (connectionViewModel.dirty) connectionLeavePending = ExitAction.ToRoute(AppRoute.DIAGNOSTIC)
                                else session.requestExit(ExitAction.ToRoute(AppRoute.DIAGNOSTIC))
                            } else vehiclesViewModel.requestLeave(route) { session.requestExit(ExitAction.ToRoute(it)) }
                        },
                        label = { Text(label) },
                        icon = {
                            Icon(
                                imageVector = when (route) {
                                    AppRoute.HOME -> PrototypeIcons.Home
                                    AppRoute.VEHICLES -> PrototypeIcons.Vehicle
                                    AppRoute.DIAGNOSTIC -> PrototypeIcons.Diagnostic
                                    else -> PrototypeIcons.Maintenance
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoute.HOME) {
                val settings by preferences.settings.collectAsState(initial = cl.cristian.bitacoraobd.domain.model.AppSettings())
                HomeScreen(
                    homeViewModel, session, settings,
                    onVehicles = { session.requestExit(ExitAction.ToRoute(AppRoute.VEHICLES)) },
                    onMaintenance = { session.requestExit(ExitAction.ToRoute(AppRoute.MAINTENANCE)) },
                    onDiagnostic = { session.requestExit(ExitAction.ToRoute(AppRoute.DIAGNOSTIC)) },
                    onDetail = { vehicleId, sessionId ->
                        session.requestExit(ExitAction.ToRoute(AppRoute.diagnosticDetail(vehicleId, sessionId)))
                    },
                    onHistory = { vehicleId -> session.requestExit(ExitAction.ToRoute(AppRoute.history(vehicleId))) },
                    onSettings = { session.requestExit(ExitAction.ToRoute(AppRoute.SETTINGS)) },
                    onConnection = { session.requestExit(ExitAction.ToRoute(AppRoute.DIAGNOSTIC)) }
                )
            }
            composable(AppRoute.VEHICLES) { VehiclesScreen(session, vehiclesViewModel) }
            navigation(startDestination = AppRoute.DIAGNOSTIC, route = AppRoute.CONNECTION_GRAPH) {
                composable(AppRoute.DIAGNOSTIC) { DiagnosticScreen(session, diagnostics) { vehicleId ->
                    session.requestExit(ExitAction.ToRoute(AppRoute.history(vehicleId)))
                } }
                composable(AppRoute.CONNECTION) { ConnectionScreen(session, connectionViewModel, preferences) {
                    if (connectionViewModel.dirty) connectionLeavePending = ExitAction.ToRoute(AppRoute.DIAGNOSTIC)
                    else session.requestExit(ExitAction.ToRoute(AppRoute.DIAGNOSTIC))
                } }
            }
            composable(AppRoute.MAINTENANCE) {
                val active = sessionState.activeVehicleId
                if (active == null) EmptyVehicleDestination("Mantenimiento", session)
                else {
                    val settings by preferences.settings.collectAsState(initial = cl.cristian.bitacoraobd.domain.model.AppSettings())
                    MaintenanceScreen(active, maintenanceViewModel, settings.distanceUnit)
                }
            }
            composable(AppRoute.SETTINGS) { SettingsScreen(settingsViewModel) { target ->
                if (target == "back") navController.popBackStack()
                else session.requestExit(ExitAction.ToRoute(target))
            } }
            composable(
                route = AppRoute.HISTORY,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { entry ->
                val vehicleId = entry.arguments?.getLong("vehicleId") ?: return@composable
                val settings by preferences.settings.collectAsState(initial = cl.cristian.bitacoraobd.domain.model.AppSettings())
                HistoryScreen(vehicleId, historyViewModel, settings.distanceUnit) { id ->
                    session.requestExit(ExitAction.ToRoute(AppRoute.diagnosticDetail(vehicleId, id)))
                }
            }
            composable(
                route = AppRoute.DIAGNOSTIC_DETAIL,
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { entry ->
                DiagnosticDetailScreen(
                    entry.arguments?.getLong("vehicleId") ?: return@composable,
                    entry.arguments?.getLong("sessionId") ?: return@composable,
                    historyViewModel,
                    preferences.settings.collectAsState(initial = cl.cristian.bitacoraobd.domain.model.AppSettings()).value.distanceUnit
                )
            }
        }
    }
}
