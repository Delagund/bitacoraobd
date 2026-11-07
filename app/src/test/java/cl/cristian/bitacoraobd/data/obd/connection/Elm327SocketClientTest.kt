package cl.cristian.bitacoraobd.data.obd.connection

import cl.cristian.bitacoraobd.data.obd.simulator.Elm327TestServer
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Elm327SocketClientTest {
    @Test
    fun sendsCommandWithCarriageReturnAndReadsFragmentedResponse() {
        runBlocking {
            val server = Elm327TestServer { command, _, output ->
                assertEquals("010C", command)
                listOf("41 ", "0C 14 ", "5F\r", ">").forEach {
                    output.write(it.toByteArray(StandardCharsets.US_ASCII))
                    output.flush()
                }
            }
            server.use {
                val client = Elm327SocketClient()
                assertTrue(client.connect(config(server)) is ObdResult.Success)
                assertEquals(ObdResult.Success("41 0C 14 5F\r>"), client.command("010C"))
                assertEquals(listOf("010C"), server.commands.toList())
                client.disconnect()
            }
        }
    }

    @Test
    fun commandsAreProcessedSequentially() {
        runBlocking {
            val server = Elm327TestServer { command, _, output ->
                output.write("$command>".toByteArray(StandardCharsets.US_ASCII))
                output.flush()
            }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                assertEquals(ObdResult.Success("A>"), client.command("A"))
                assertEquals(ObdResult.Success("B>"), client.command("B"))
                assertEquals(listOf("A", "B"), server.commands.toList())
                client.disconnect()
            }
        }
    }

    @Test
    fun silentServerTimesOutAndClosesConnection() {
        runBlocking {
            val received = CountDownLatch(1)
            val server = Elm327TestServer { _, _, _ -> received.countDown() }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                val result = client.command("03")
                assertTrue(received.await(1, TimeUnit.SECONDS))
                assertTrue(result is ObdResult.Failure)
                assertTrue(client.command("03") is ObdResult.Failure)
                client.disconnect()
            }
        }
    }

    @Test
    fun abruptEofFailsCommand() {
        runBlocking {
            val server = Elm327TestServer { _, socket, _ -> socket.close() }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                assertTrue(client.command("03") is ObdResult.Failure)
                client.disconnect()
            }
        }
    }

    @Test
    fun responseWithoutPromptTimesOut() {
        runBlocking {
            val server = Elm327TestServer { _, _, output ->
                output.write("response".toByteArray(StandardCharsets.US_ASCII))
                output.flush()
            }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                assertTrue(client.command("03") is ObdResult.Failure)
                client.disconnect()
            }
        }
    }

    @Test
    fun responseOver64KiBFails() {
        runBlocking {
            val server = Elm327TestServer { _, _, output ->
                output.write(ByteArray(64 * 1024 + 1) { 'x'.code.toByte() })
                output.flush()
            }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                assertTrue(client.command("03") is ObdResult.Failure)
                client.disconnect()
            }
        }
    }

    @Test
    fun cancellationDuringReadClosesSocketAndRethrowsCancellation() {
        runBlocking {
            val received = CountDownLatch(1)
            val server = Elm327TestServer { _, _, _ -> received.countDown() }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                val command = async(Dispatchers.IO) { client.command("03") }
                assertTrue(withContext(Dispatchers.IO) { received.await(2, TimeUnit.SECONDS) })
                command.cancel()
                command.join()
                assertTrue(command.isCancelled)
                assertTrue(client.command("03") is ObdResult.Failure)
                client.disconnect()
            }
        }
    }

    @Test
    fun disconnectDuringReadAllowsReconnect() {
        runBlocking {
            val firstRead = CountDownLatch(1)
            val server = Elm327TestServer { command, socket, output ->
                if (command == "WAIT") {
                    firstRead.countDown()
                    socket.getInputStream().read()
                } else {
                    output.write("OK>".toByteArray(StandardCharsets.US_ASCII))
                    output.flush()
                }
            }
            server.use {
                val client = Elm327SocketClient()
                client.connect(config(server))
                val pending = async(Dispatchers.IO) { client.command("WAIT") }
                assertTrue(withContext(Dispatchers.IO) { firstRead.await(2, TimeUnit.SECONDS) })
                client.disconnect()
                assertTrue(pending.await() is ObdResult.Failure)
                assertTrue(client.connect(config(server)) is ObdResult.Success)
                assertEquals(ObdResult.Success("OK>"), client.command("NEXT"))
                client.disconnect()
            }
        }
    }

    private fun config(server: Elm327TestServer) =
        ObdConnectionConfig(ipAddress = "127.0.0.1", port = server.port)
}
