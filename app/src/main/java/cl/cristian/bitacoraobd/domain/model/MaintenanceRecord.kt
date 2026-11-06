package cl.cristian.bitacoraobd.domain.model

import java.time.LocalDate

data class MaintenanceRecord(
    val id: Long = 0L,
    val vehicleId: Long,
    val serviceType: MaintenanceType,
    val performedDate: LocalDate,
    val mileageAtServiceKm: Int,
    val notes: String = "",
    val nextDueDate: LocalDate? = null,
    val nextDueMileageKm: Int? = null
) {
    init {
        require(id >= 0L) { "Record id cannot be negative" }
        require(vehicleId > 0L) { "Vehicle id must be positive" }
        require(mileageAtServiceKm >= 0) { "Mileage cannot be negative" }
        require(nextDueMileageKm == null || nextDueMileageKm >= 0) {
            "Next due mileage cannot be negative"
        }
    }
}
