package cl.cristian.bitacoraobd.domain.model

import java.util.UUID

data class DiagnosticSession(
    val id: Long = 0L,
    val vehicleId: Long,
    val timestampMillis: Long,
    val mileageAtScanKm: Int,
    val codes: List<String> = emptyList(),
    val captureId: String = UUID.randomUUID().toString()
) {
    init {
        require(id >= 0L) { "Session id cannot be negative" }
        require(vehicleId > 0L) { "Vehicle id must be positive" }
        require(mileageAtScanKm >= 0) { "Mileage cannot be negative" }
    }
}
