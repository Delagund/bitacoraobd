package cl.cristian.bitacoraobd.domain.obd

data class ObdLiveMetrics(
    val rpm: Int?,
    val coolantTempCelsius: Int?,
    val speedKmh: Int?,
    val timestampMillis: Long
)
