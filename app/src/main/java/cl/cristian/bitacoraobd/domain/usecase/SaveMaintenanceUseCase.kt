package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import java.time.Clock
import java.time.LocalDate

class SaveMaintenanceUseCase(
    private val vehicles: VehicleRepository,
    private val maintenance: MaintenanceRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend operator fun invoke(
        record: MaintenanceRecord,
        confirmHistoricalMileage: Boolean = false
    ): SaveMaintenanceResult {
        if (record.performedDate > LocalDate.now(clock)) {
            return SaveMaintenanceResult.Failed("Service date cannot be in the future")
        }
        if (record.nextDueDate != null && record.nextDueDate < record.performedDate) {
            return SaveMaintenanceResult.Failed("Next due date cannot precede service date")
        }
        if (record.nextDueMileageKm != null && record.nextDueMileageKm < record.mileageAtServiceKm) {
            return SaveMaintenanceResult.Failed("Next due mileage cannot precede service mileage")
        }
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
