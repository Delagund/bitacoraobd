package cl.cristian.bitacoraobd.domain.obd

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdContractsTest {
    @Test fun validatesIpv4AndPortBoundaries() {
        assertEquals(1, ObdConnectionConfig("0.0.0.0", 1).port)
        assertEquals(65535, ObdConnectionConfig("255.255.255.255", 65535).port)
        listOf("", "1.2.3", "1.2.3.256", "x.2.3.4").forEach { ip ->
            expectInvalid { ObdConnectionConfig(ip, 35000) }
        }
        listOf(0, 65536).forEach { port ->
            expectInvalid { ObdConnectionConfig("192.168.0.10", port) }
        }
    }

    @Test fun cleanObdSuccessDiffersFromFailure() {
        val clean: ObdResult<List<String>> = ObdResult.Success(emptyList())
        val failed: ObdResult<List<String>> = ObdResult.Failure("read error")
        assertTrue((clean as ObdResult.Success).value.isEmpty())
        assertEquals("read error", (failed as ObdResult.Failure).message)
    }

    @Test fun nullableMetricDiffersFromZero() {
        val metrics = ObdLiveMetrics(null, 0, 0, 1L)
        assertEquals(null, metrics.rpm)
        assertEquals(0, metrics.coolantTempCelsius)
    }

    private fun expectInvalid(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected invalid configuration")
        } catch (_: IllegalArgumentException) {
        }
    }

    private class FakeGateway : ObdGateway {
        private val state = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
        override val connectionState: StateFlow<ObdConnectionState> = state
        override suspend fun connect(config: ObdConnectionConfig) = ObdResult.Success(Unit)
        override suspend fun disconnect() = Unit
        override suspend fun readLiveMetrics() =
            ObdResult.Success(ObdLiveMetrics(null, null, null, 0L))
        override suspend fun readDiagnosticCodes() = ObdResult.Success(emptyList<String>())
    }
}
