package cl.cristian.bitacoraobd.domain.model

data class Vehicle(
    val id: Long = 0L,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: String,
    val engineDescription: String,
    val currentMileageKm: Int
) {
    init {
        require(id >= 0L) { "Vehicle id cannot be negative" }
        require(currentMileageKm >= 0) { "Mileage cannot be negative" }
    }
}
