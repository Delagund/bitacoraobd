package cl.cristian.bitacoraobd.domain.model

sealed interface DashboardState {
    data object NoVehicles : DashboardState
    data class Ready(val summary: DashboardSummary) : DashboardState
}
