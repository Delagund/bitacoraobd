package cl.cristian.bitacoraobd.domain.model

enum class DistanceUnit { KILOMETERS, MILES }
enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

data class AppSettings(
    val displayName: String? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val showRpm: Boolean = true,
    val showCoolant: Boolean = true,
    val showSpeed: Boolean = true
)
