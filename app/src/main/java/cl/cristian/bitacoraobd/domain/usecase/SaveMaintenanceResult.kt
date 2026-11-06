package cl.cristian.bitacoraobd.domain.usecase

sealed interface SaveMaintenanceResult {
    data class Saved(val recordId: Long) : SaveMaintenanceResult
    data class RequiresHistoricalMileageConfirmation(
        val enteredMileageKm: Int,
        val currentMileageKm: Int
    ) : SaveMaintenanceResult
    data class Failed(val message: String) : SaveMaintenanceResult
}
