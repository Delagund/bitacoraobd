package cl.cristian.bitacoraobd.domain.model

data class DashboardSummary(
    val activeVehicle: Vehicle,
    val connectionState: cl.cristian.bitacoraobd.domain.obd.ObdConnectionState,
    val lastDiagnosticSession: DiagnosticSession?,
    val codesInLastSession: List<DiagnosedDtc>,
    val activeMaintenanceSchedules: List<MaintenanceStatusItem>,
    val alerts: List<VehicleAlert>
)
