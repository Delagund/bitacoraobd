package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository

class SaveMaintenanceUseCase(
    private val vehicles: VehicleRepository,
    private val maintenance: MaintenanceRepository
) {
    suspend operator fun invoke(
        record: MaintenanceRecord,
        confirmHistoricalMileage: Boolean = false
    ): SaveMaintenanceResult {
        val vehicle = vehicles.getVehicleById(record.vehicleId)
            ?: return SaveMaintenanceResult.Failed("Vehicle does not exist")
        if (record.mileageAtServiceKm < vehicle.currentMileageKm && !confirmHistoricalMileage) {
            return SaveMaintenanceResult.RequiresHistoricalMileageConfirmation(
                record.mileageAtServiceKm, vehicle.currentMileageKm
            )
        }
        return try {
            SaveMaintenanceResult.Saved(maintenance.saveMaintenanceRecord(record))
        } catch (exception: IllegalArgumentException) {
            SaveMaintenanceResult.Failed(exception.message ?: "Could not save maintenance")
        }
    }
}
