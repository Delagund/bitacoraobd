package cl.cristian.bitacoraobd.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.CancellationException

object AppRoute {
    const val HOME = "home"
    const val VEHICLES = "vehicles"
    const val DIAGNOSTIC = "diagnostic"
    const val MAINTENANCE = "maintenance"
    const val SETTINGS = "settings"
    const val HISTORY = "history/{vehicleId}"
    const val DIAGNOSTIC_DETAIL = "diagnostic-detail/{sessionId}"
    const val CONNECTION_GRAPH = "connection-flow"
    const val CONNECTION = "connection"

    val primaryRoutes = setOf(HOME, VEHICLES, DIAGNOSTIC, MAINTENANCE)

    fun history(vehicleId: Long) = "history/$vehicleId"
    fun diagnosticDetail(sessionId: Long) = "diagnostic-detail/$sessionId"

    fun isAllowedTarget(route: String): Boolean =
        route in primaryRoutes || route == SETTINGS || route == CONNECTION ||
            route.toLongRouteId("history/") || route.toLongRouteId("diagnostic-detail/")

    private fun String.toLongRouteId(prefix: String): Boolean =
        startsWith(prefix) && removePrefix(prefix).toLongOrNull()?.let { it > 0L } == true
}

@androidx.compose.runtime.Composable
private fun EmptyVehicleDestination(title: String, session: AppSession) {
    val state by session.state.collectAsState()
    if (state.vehicleLoadStatus.name == "Ready" && state.vehicles.isEmpty()) {
        androidx.compose.foundation.layout.Column {
            Text(title)
            Text("Agrega un vehículo para continuar")
            androidx.compose.material3.Button(
                onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.VEHICLES)) }
            ) { Text("Agregar vehículo") }
        }
    } else Text(title)
}

@androidx.compose.runtime.Composable
fun AppNavigation(navController: NavHostController, session: AppSession, vehiclesViewModel: VehiclesViewModel, onBackAtRoot: () -> Unit) {
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
    val sessionState by session.state.collectAsState()
    LaunchedEffect(sessionState.operation, sessionState.sensitiveOperationRunning, vehiclesViewModel.queuedExitRoute, vehiclesViewModel.queuedExitSaveSucceeded) {
        if (sessionState.operation == null && !sessionState.sensitiveOperationRunning) vehiclesViewModel.settleQueuedExit()
    }
    BackHandler {
        if (currentRoute?.destination?.route == AppRoute.VEHICLES) {
            if (vehiclesViewModel.draft != null) vehiclesViewModel.requestBackFromForm()
            else session.requestExit(ExitAction.Back)
        } else session.requestExit(ExitAction.Back)
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
            NavigationBar {
                listOf(
                    AppRoute.HOME to "Inicio",
                    AppRoute.VEHICLES to "Vehículos",
                    AppRoute.DIAGNOSTIC to "Diagnóstico",
                    AppRoute.MAINTENANCE to "Mantenimiento"
                ).forEach { (route, label) ->
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == route,
                        onClick = { vehiclesViewModel.requestLeave(route) { session.requestExit(ExitAction.ToRoute(it)) } },
                        label = { Text(label) },
                        icon = {}
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
                val state by session.state.collectAsState()
                if (state.vehicleLoadStatus.name == "Ready" && state.vehicles.isEmpty()) {
                    androidx.compose.material3.Button(
                        onClick = { session.requestExit(ExitAction.ToRoute(AppRoute.VEHICLES)) }
                    ) { Text("Agregar vehículo") }
                } else {
                    Text(if (state.vehicleLoadStatus.name == "Failed") "No se pudieron cargar los vehículos" else "Inicio")
                }
            }
            composable(AppRoute.VEHICLES) { VehiclesScreen(session, vehiclesViewModel) }
            navigation(startDestination = AppRoute.DIAGNOSTIC, route = AppRoute.CONNECTION_GRAPH) {
                composable(AppRoute.DIAGNOSTIC) { EmptyVehicleDestination("Diagnostic", session) }
                composable(AppRoute.CONNECTION) { Text("Connection") }
            }
            composable(AppRoute.MAINTENANCE) { EmptyVehicleDestination("Maintenance", session) }
            composable(AppRoute.SETTINGS) { Text("Settings") }
            composable(
                route = AppRoute.HISTORY,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { EmptyVehicleDestination("History", session) }
            composable(
                route = AppRoute.DIAGNOSTIC_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { Text("Diagnostic detail") }
        }
    }
}
