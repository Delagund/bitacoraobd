package cl.cristian.bitacoraobd.data.obd

import cl.cristian.bitacoraobd.data.obd.simulator.Elm327TestServer
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Elm327ObdGatewayTest {
    @Test
    fun incomplete0100BitmapFailsAndClosesSocket() = runBlocking {
        val closedAfterInvalidResponse = CountDownLatch(1)
        val server = Elm327TestServer { command, socket, output ->
            output.respond(if (command == "0100") "41 00" else validResponse(command))
            if (command == "0100") {
                if (socket.getInputStream().read() == -1) closedAfterInvalidResponse.countDown()
            }
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Failure)
            assertTrue(gateway.connectionState.value is ObdConnectionState.Error)
            assertFalse(gateway.connectionState.value is ObdConnectionState.Connected)
            assertTrue(withContext(Dispatchers.IO) {
                closedAfterInvalidResponse.await(2, TimeUnit.SECONDS)
            })
            gateway.disconnect()
        }
    }

    @Test
    fun cancellingMetricReadPropagatesAndDoesNotLeaveConnectedState() = runBlocking {
        val commandReceived = CountDownLatch(1)
        val socketClosed = CountDownLatch(1)
        val server = Elm327TestServer { command, socket, output ->
            if (command == "010C") {
                commandReceived.countDown()
                while (socket.getInputStream().read() != -1) Unit
                socketClosed.countDown()
            } else {
                output.respond(validResponse(command))
            }
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            val read = async(Dispatchers.IO) { gateway.readLiveMetrics() }
            assertTrue(withContext(Dispatchers.IO) { commandReceived.await(2, TimeUnit.SECONDS) })
            read.cancel()
            var cancellation: CancellationException? = null
            try {
                read.await()
            } catch (error: CancellationException) {
                cancellation = error
            }
            assertNotNull(cancellation)
            assertTrue(withContext(Dispatchers.IO) { socketClosed.await(2, TimeUnit.SECONDS) })
            assertFalse(gateway.connectionState.value is ObdConnectionState.Connected)
            gateway.disconnect()
        }
    }

    @Test
    fun cancellingDiagnosticReadPropagatesAndDoesNotLeaveConnectedState() = runBlocking {
        val commandReceived = CountDownLatch(1)
        val socketClosed = CountDownLatch(1)
        val server = Elm327TestServer { command, socket, output ->
            if (command == "03") {
                commandReceived.countDown()
                while (socket.getInputStream().read() != -1) Unit
                socketClosed.countDown()
            } else {
                output.respond(validResponse(command))
            }
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            val read = async(Dispatchers.IO) { gateway.readDiagnosticCodes() }
            assertTrue(withContext(Dispatchers.IO) { commandReceived.await(2, TimeUnit.SECONDS) })
            read.cancel()
            var cancellation: CancellationException? = null
            try {
                read.await()
            } catch (error: CancellationException) {
                cancellation = error
            }
            assertNotNull(cancellation)
            assertTrue(withContext(Dispatchers.IO) { socketClosed.await(2, TimeUnit.SECONDS) })
            assertFalse(gateway.connectionState.value is ObdConnectionState.Connected)
            gateway.disconnect()
        }
    }

    @Test
    fun initializesInOrderAndPublishesConnectedOnlyAfterHandshake() = runBlocking {
        val reached0100 = CountDownLatch(1)
        val continue0100 = CountDownLatch(1)
        val server = Elm327TestServer { command, _, output ->
            if (command == "0100") {
                reached0100.countDown()
                assertTrue(continue0100.await(2, TimeUnit.SECONDS))
            }
            output.respond(when (command) {
                "ATZ" -> "ELM327 v1.5"
                "0100" -> "41 00 BE 3E B8 13"
                else -> "OK"
            })
        }
        server.use {
            val gateway = Elm327ObdGateway()
            val connect = async(Dispatchers.IO) { gateway.connect(config(server)) }
            assertTrue(withContext(Dispatchers.IO) { reached0100.await(2, TimeUnit.SECONDS) })
            assertEquals(ObdConnectionState.Connecting, gateway.connectionState.value)
            continue0100.countDown()

            assertEquals(ObdResult.Success(Unit), connect.await())
            assertEquals(ObdConnectionState.Connected(config(server)), gateway.connectionState.value)
            assertEquals(listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100"), server.commands.toList())
            gateway.disconnect()
        }
    }

    @Test
    fun invalidResetOrConfigurationResponseFailsAndClosesConnection() = runBlocking {
        val expected = listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100")
        for (failedCommand in expected) {
            val server = Elm327TestServer { command, _, output ->
                output.respond(when {
                    command != failedCommand -> validResponse(command)
                    command == "ATZ" -> "Generic adapter"
                    command == "0100" -> "NO DATA"
                    else -> "NO"
                })
            }
            server.use {
                val gateway = Elm327ObdGateway()
                assertTrue(gateway.connect(config(server)) is ObdResult.Failure)
                assertEquals(ObdConnectionState.Error((gateway.connectionState.value as ObdConnectionState.Error).message),
                    gateway.connectionState.value)
                assertEquals(expected.take(expected.indexOf(failedCommand) + 1), server.commands.toList())
                assertTrue(gateway.readLiveMetrics() is ObdResult.Failure)
                gateway.disconnect()
                assertEquals(ObdConnectionState.Disconnected, gateway.connectionState.value)
            }
        }
    }

    @Test
    fun readsEachMetricOnceAndKeepsUnavailablePidsNull() = runBlocking {
        val server = Elm327TestServer { command, _, output ->
            output.respond(
                when (command) {
                    "010C" -> "41 0C 14 5F"
                    "0105" -> "NO DATA"
                    "010D" -> "41 0D 2A"
                    else -> validResponse(command)
                }
            )
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertEquals(ObdResult.Success(Unit), gateway.connect(config(server)))
            assertEquals(
                listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100"),
                server.commands.toList()
            )
            val result = gateway.readLiveMetrics()
            assertTrue(result is ObdResult.Success)
            val metrics = (result as ObdResult.Success).value
            assertEquals(1303, metrics.rpm)
            assertNull(metrics.coolantTempCelsius)
            assertEquals(42, metrics.speedKmh)
            assertTrue(metrics.timestampMillis > 0)
            assertEquals(
                listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100", "010C", "0105", "010D"),
                server.commands.toList()
            )
            gateway.disconnect()
        }
    }

    @Test
    fun invalidPidPayloadDoesNotInvalidateOtherMetrics() = runBlocking {
        val server = Elm327TestServer { command, _, output ->
            output.respond(
                when (command) {
                    "010C" -> "41 05 28"
                    "0105" -> "41 05 50"
                    "010D" -> "41 0D 00"
                    else -> validResponse(command)
                }
            )
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            val result = gateway.readLiveMetrics() as ObdResult.Success
            assertNull(result.value.rpm)
            assertEquals(40, result.value.coolantTempCelsius)
            assertEquals(0, result.value.speedKmh)
            gateway.disconnect()
        }
    }

    @Test
    fun transportLossDuringMetricsFailsAndPublishesError() = runBlocking {
        val server = Elm327TestServer { command, socket, output ->
            if (command == "0105") socket.close()
            else output.respond(validResponse(command))
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            assertTrue(gateway.readLiveMetrics() is ObdResult.Failure)
            assertTrue(gateway.connectionState.value is ObdConnectionState.Error)
            assertEquals(listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100", "010C", "0105"), server.commands.toList())
            gateway.disconnect()
        }
    }

    @Test
    fun readsCleanAndNonEmptyDiagnosticResponsesWithOnlyMode03() = runBlocking {
        var codes = "43 00 00"
        val server = Elm327TestServer { command, _, output ->
            output.respond(if (command == "03") codes else validResponse(command))
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            assertEquals(ObdResult.Success(emptyList<String>()), gateway.readDiagnosticCodes())
            codes = "43 01 43 01 96 02 34"
            assertEquals(ObdResult.Success(listOf("P0143", "P0196", "P0234")), gateway.readDiagnosticCodes())
            assertEquals(2, server.commands.count { it == "03" })
            assertFalse(server.commands.any { it == "04" || it == "07" || it == "0A" })
            gateway.disconnect()
        }
    }

    @Test
    fun operationsWithoutConnectionFailWithoutSendingCommands() = runBlocking {
        val gateway = Elm327ObdGateway()
        assertEquals(ObdConnectionState.Disconnected, gateway.connectionState.value)
        assertTrue(gateway.readLiveMetrics() is ObdResult.Failure)
        assertTrue(gateway.readDiagnosticCodes() is ObdResult.Failure)
    }

    @Test
    fun disconnectDuringInitializationPreventsLateConnectedStateAndCanReconnect() = runBlocking {
        val atzStarted = CountDownLatch(1)
        val releaseAtz = CountDownLatch(1)
        val server = Elm327TestServer { command, _, output ->
            if (command == "ATZ") {
                atzStarted.countDown()
                assertTrue(releaseAtz.await(2, TimeUnit.SECONDS))
            }
            output.respond(validResponse(command))
        }
        server.use {
            val gateway = Elm327ObdGateway()
            val connecting = async(Dispatchers.IO) { gateway.connect(config(server)) }
            assertTrue(withContext(Dispatchers.IO) { atzStarted.await(2, TimeUnit.SECONDS) })
            gateway.disconnect()
            releaseAtz.countDown()
            assertTrue(connecting.await() is ObdResult.Failure)
            assertEquals(ObdConnectionState.Disconnected, gateway.connectionState.value)
        }
    }

    @Test
    fun metricAndDiagnosticOperationsAreNotInterleaved() = runBlocking {
        val rpmReceived = CountDownLatch(1)
        val finishRpm = CountDownLatch(1)
        val server = Elm327TestServer { command, _, output ->
            if (command == "010C") {
                rpmReceived.countDown()
                assertTrue(finishRpm.await(2, TimeUnit.SECONDS))
            }
            output.respond(
                when (command) {
                    "010C" -> "41 0C 00 00"
                    "0105" -> "41 05 28"
                    "010D" -> "41 0D 01"
                    "03" -> "43 00 00"
                    else -> validResponse(command)
                }
            )
        }
        server.use {
            val gateway = Elm327ObdGateway()
            assertTrue(gateway.connect(config(server)) is ObdResult.Success)
            val metrics = async(Dispatchers.IO) { gateway.readLiveMetrics() }
            assertTrue(withContext(Dispatchers.IO) { rpmReceived.await(2, TimeUnit.SECONDS) })
            val diagnostics = async(Dispatchers.IO) { gateway.readDiagnosticCodes() }
            finishRpm.countDown()
            assertTrue(metrics.await() is ObdResult.Success)
            assertTrue(diagnostics.await() is ObdResult.Success)
            assertEquals(
                listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100", "010C", "0105", "010D", "03"),
                server.commands.toList()
            )
            gateway.disconnect()
        }
    }

    @Test
    fun disconnectIsIdempotentAndReconnectStartsFreshInitialization() = runBlocking {
        val server = Elm327TestServer { command, _, output -> output.respond(validResponse(command)) }
        server.use {
            val gateway = Elm327ObdGateway()
            repeat(2) {
                assertEquals(ObdResult.Success(Unit), gateway.connect(config(server)))
                gateway.disconnect()
                gateway.disconnect()
                assertEquals(ObdConnectionState.Disconnected, gateway.connectionState.value)
            }
            assertEquals(
                listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100", "ATZ", "ATE0", "ATH0", "ATSP0", "0100"),
                server.commands.toList()
            )
        }
    }

    private fun validResponse(command: String): String = when (command) {
        "ATZ" -> "ELM327 v1.5"
        "0100" -> "41 00 BE 3E B8 13"
        "010C" -> "41 0C 00 00"
        "0105" -> "41 05 28"
        "010D" -> "41 0D 01"
        "03" -> "43 00 00"
        else -> "OK"
    }

    private fun OutputStream.respond(response: String) {
        write("$response>".toByteArray(StandardCharsets.US_ASCII))
        flush()
    }

    private fun config(server: Elm327TestServer) =
        ObdConnectionConfig(ipAddress = "127.0.0.1", port = server.port)
}
