package cl.cristian.bitacoraobd.domain.obd

data class ObdConnectionConfig(
    val ipAddress: String = DEFAULT_IP,
    val port: Int = DEFAULT_PORT
) {
    init {
        require(isIpv4Address(ipAddress)) { "IP address must be a valid IPv4 address" }
        require(port in 1..65535) { "Port must be between 1 and 65535" }
    }

    companion object {
        const val DEFAULT_IP = "192.168.0.10"
        const val DEFAULT_PORT = 35000

        private fun isIpv4Address(value: String): Boolean {
            val parts = value.split('.')
            return parts.size == 4 && parts.all { part ->
                part.isNotEmpty() && part.all(Char::isDigit) &&
                    part.toIntOrNull()?.let { it in 0..255 } == true
            }
        }
    }
}
