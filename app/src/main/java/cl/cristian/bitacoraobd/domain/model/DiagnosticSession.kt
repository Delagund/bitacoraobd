package cl.cristian.bitacoraobd.domain.model

data class DiagnosticSession(
    val id: Long = 0L,
    val vehicleId: Long,
    val timestampMillis: Long,
    val mileageAtScanKm: Int,
    val codes: List<String> = emptyList()
) {
    init {
        require(id >= 0L) { "Session id cannot be negative" }
        require(vehicleId > 0L) { "Vehicle id must be positive" }
        require(mileageAtScanKm >= 0) { "Mileage cannot be negative" }
    }
}
