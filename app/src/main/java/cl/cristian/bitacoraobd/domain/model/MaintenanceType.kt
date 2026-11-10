package cl.cristian.bitacoraobd.domain.model

enum class MaintenanceType(val supportsAutoAlerts: Boolean) {
    OIL_AND_FILTER(true),
    OIL_ONLY(true),
    FILTER_ONLY(true),
    BRAKES(true),
    TIRES(true),
    BATTERY(true),
    COOLANT(true),
    OTHER(false)
}
