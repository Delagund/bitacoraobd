package cl.cristian.bitacoraobd.data.obd.parser

class ObdPidInterpreter {
    fun rpm(bytes: List<Int>?): Int? {
        val payload = bytes?.takeIf { it.isValidPayload(2) } ?: return null
        return (payload[0] * 256 + payload[1]) / 4
    }

    fun coolantTempCelsius(bytes: List<Int>?): Int? {
        val payload = bytes?.takeIf { it.isValidPayload(1) } ?: return null
        return payload[0] - 40
    }

    fun speedKmh(bytes: List<Int>?): Int? {
        val payload = bytes?.takeIf { it.isValidPayload(1) } ?: return null
        return payload[0]
    }

    private fun List<Int>.isValidPayload(expectedSize: Int): Boolean =
        size == expectedSize && all { it in 0..0xFF }
}
