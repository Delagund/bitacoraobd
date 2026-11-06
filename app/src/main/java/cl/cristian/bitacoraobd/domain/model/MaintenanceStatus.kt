package cl.cristian.bitacoraobd.domain.model

enum class MaintenanceStatus {
    UNSCHEDULED,
    UPCOMING,
    OVERDUE
}

data class MaintenanceStatusItem(
    val record: MaintenanceRecord,
    val status: MaintenanceStatus
)
