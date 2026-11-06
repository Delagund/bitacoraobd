package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatusItem
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import java.time.Clock
import java.time.LocalDate

class GetMaintenanceStatusUseCase(
    private val vehicles: VehicleRepository,
    private val maintenance: MaintenanceRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend operator fun invoke(vehicleId: Long): List<MaintenanceStatusItem> {
        val vehicle = vehicles.getVehicleById(vehicleId) ?: return emptyList()
        val today = LocalDate.now(clock)
        return maintenance.getLatestRecordsPerAlertableType(vehicleId).map { record ->
            val scheduled = record.nextDueDate != null || record.nextDueMileageKm != null
            val due = (record.nextDueDate?.let { !today.isBefore(it) } == true) ||
                (record.nextDueMileageKm?.let { vehicle.currentMileageKm >= it } == true)
            MaintenanceStatusItem(
                record,
                when {
                    !scheduled -> MaintenanceStatus.UNSCHEDULED
                    due -> MaintenanceStatus.OVERDUE
                    else -> MaintenanceStatus.UPCOMING
                }
            )
        }
    }
}
