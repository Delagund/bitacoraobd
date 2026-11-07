package cl.cristian.bitacoraobd.data.obd.parser

import cl.cristian.bitacoraobd.domain.obd.ObdResult

class ObdResponseParser {
    fun parseDiagnosticCodes(response: String): ObdResult<List<String>> {
        val bytes = responseBytes(response, "03") ?: return ObdResult.Failure("Invalid OBD response")
        if (bytes.firstOrNull() != 0x43) return ObdResult.Failure("Unexpected response mode")
        val payload = bytes.drop(1)
        if (payload.isEmpty() || payload.size % 2 != 0) {
            return ObdResult.Failure("Incomplete DTC response")
        }
        val codes = payload.chunked(2)
            .filterNot { it[0] == 0 && it[1] == 0 }
            .map(::decodeDtc)
            .distinct()
        return ObdResult.Success(codes)
    }

    fun parsePidResponse(response: String, pid: Int): ObdResult<List<Int>> {
        require(pid in 0..0xFF)
        val bytes = responseBytes(response, "%02X%02X".format(0x01, pid))
            ?: return ObdResult.Failure("Invalid OBD response")
        if (bytes.size < 2 || bytes[0] != 0x41 || bytes[1] != pid) {
            return ObdResult.Failure("Unexpected response PID")
        }
        val payload = bytes.drop(2)
        val expectedBytes = when (pid) {
            0x00 -> 4
            0x0C -> 2
            0x05, 0x0D -> 1
            else -> 0
        }
        if (payload.size < expectedBytes) return ObdResult.Failure("Incomplete PID response")
        return ObdResult.Success(payload)
    }

    private fun responseBytes(response: String, command: String): List<Int>? {
        if (response.contains(NO_DATA, ignoreCase = true) ||
            response.contains("UNABLE TO CONNECT", ignoreCase = true) ||
            response.contains("ERROR", ignoreCase = true) ||
            response.contains('?')
        ) return null

        val normalized = response.lineSequence()
            .filterNot { it.trim().equals(command, ignoreCase = true) }
            .filterNot { it.contains("SEARCHING...", ignoreCase = true) }
            .joinToString(" ")
            .replace(">", "")
            .uppercase()
        val tokens = normalized.split(Regex("[\\s]+")).filter(String::isNotBlank)
            .flatMap { token ->
                if (token.length == 2) listOf(token)
                else if (token.length % 2 == 0 && token.all { it.digitToIntOrNull(16) != null }) {
                    token.chunked(2)
                } else return null
            }
        if (tokens.isEmpty() || tokens.any { it.toIntOrNull(16) == null }) return null
        return tokens.map { it.toInt(16) }
    }

    private fun decodeDtc(pair: List<Int>): String {
        val first = pair[0]
        val prefix = charArrayOf('P', 'C', 'B', 'U')[first ushr 6]
        val digit = (first ushr 4) and 0x03
        return "$prefix$digit%X%02X".format(first and 0x0F, pair[1])
    }

    private companion object {
        const val NO_DATA = "NO DATA"
    }
}
