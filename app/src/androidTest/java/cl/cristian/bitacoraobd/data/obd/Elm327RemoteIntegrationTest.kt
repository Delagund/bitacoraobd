package cl.cristian.bitacoraobd.data.obd

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import kotlinx.coroutines.runBlocking
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Elm327RemoteIntegrationTest {
    @Test
    fun connectsReadsMetricsAndReadsDocumentedDiagnosticCodes() = runBlocking {
        val arguments = InstrumentationRegistry.getArguments()
        val host = arguments.getString("elm327Host")
        Assume.assumeTrue("Pass elm327Host to run this manual network test", !host.isNullOrBlank())
        val port = arguments.getString("elm327Port")?.toIntOrNull() ?: ObdConnectionConfig.DEFAULT_PORT
        val config = ObdConnectionConfig(ipAddress = host!!, port = port)
        val gateway = Elm327ObdGateway()

        try {
            assertEquals(ObdResult.Success(Unit), gateway.connect(config))
            assertEquals(ObdConnectionState.Connected(config), gateway.connectionState.value)

            val metricsResult = gateway.readLiveMetrics()
            assertTrue("Metric read must succeed, got $metricsResult", metricsResult is ObdResult.Success)
            val metrics = (metricsResult as ObdResult.Success).value
            Log.i("Elm327RemoteTest", "metrics=$metrics")
            assertNotNull("RPM must be available", metrics.rpm)
            assertNotNull("Speed must be available", metrics.speedKmh)
            metrics.coolantTempCelsius?.let { assertTrue(it in -40..215) }

            val codesResult = gateway.readDiagnosticCodes()
            assertTrue("DTC read must succeed, got $codesResult", codesResult is ObdResult.Success)
            val codes = (codesResult as ObdResult.Success).value
            Log.i("Elm327RemoteTest", "dtcs=$codes")
            assertEquals(listOf("P0143", "P0196", "P0234"), codes)
            assertTrue(gateway.connectionState.value is ObdConnectionState.Connected)
        } finally {
            gateway.disconnect()
        }
    }
}
