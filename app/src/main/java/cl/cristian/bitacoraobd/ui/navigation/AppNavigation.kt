package cl.cristian.bitacoraobd.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import cl.cristian.bitacoraobd.ui.AppSession
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
fun AppNavigation(navController: NavHostController, session: AppSession, onBackAtRoot: () -> Unit) {
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

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoute.HOME) { Text("Home") }
            composable(AppRoute.VEHICLES) { Text("Vehicles") }
            navigation(startDestination = AppRoute.DIAGNOSTIC, route = AppRoute.CONNECTION_GRAPH) {
                composable(AppRoute.DIAGNOSTIC) { Text("Diagnostic") }
                composable(AppRoute.CONNECTION) { Text("Connection") }
            }
            composable(AppRoute.MAINTENANCE) { Text("Maintenance") }
            composable(AppRoute.SETTINGS) { Text("Settings") }
            composable(
                route = AppRoute.HISTORY,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { Text("History") }
            composable(
                route = AppRoute.DIAGNOSTIC_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { Text("Diagnostic detail") }
        }
    }
}
