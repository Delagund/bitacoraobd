package cl.cristian.bitacoraobd.domain.model

sealed interface DiagnosticScanResult {
    data class Success(
        val vehicleId: Long,
        val timestampMillis: Long,
        val mileageAtScanKm: Int,
        val codes: List<String>
    ) : DiagnosticScanResult {
        init {
            require(vehicleId > 0L) { "Vehicle id must be positive" }
            require(mileageAtScanKm >= 0) { "Mileage cannot be negative" }
        }
    }

    data class Failure(val message: String) : DiagnosticScanResult
}
