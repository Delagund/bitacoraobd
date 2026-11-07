package cl.cristian.bitacoraobd.data.obd.connection

import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

class Elm327SocketClient {
    private val socket = AtomicReference<Socket?>()
    private val commandMutex = Mutex()

    suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> = withContext(Dispatchers.IO) {
        disconnect()
        val newSocket = Socket()
        socket.set(newSocket)
        try {
            newSocket.connect(InetSocketAddress(config.ipAddress, config.port), CONNECT_TIMEOUT_MS)
            currentCoroutineContext().ensureActive()
            newSocket.soTimeout = READ_POLL_TIMEOUT_MS
            ObdResult.Success(Unit)
        } catch (cancelled: CancellationException) {
            close(newSocket)
            throw cancelled
        } catch (error: Exception) {
            close(newSocket)
            ObdResult.Failure(error.message ?: "Unable to connect")
        }
    }

    suspend fun command(command: String): ObdResult<String> = commandMutex.withLock {
        require(command.isNotEmpty() && command.all { it.code in 0x20..0x7E }) {
            "Command must contain printable ASCII only"
        }
        val activeSocket = socket.get()?.takeIf { it.isConnected && !it.isClosed }
            ?: return@withLock ObdResult.Failure("Socket is not connected")

        try {
            withContext(Dispatchers.IO) {
                val context = currentCoroutineContext()
                val output = activeSocket.getOutputStream()
                output.write(command.toByteArray(StandardCharsets.US_ASCII))
                output.write(CARRIAGE_RETURN)
                output.flush()

                val input = activeSocket.getInputStream()
                val response = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                val deadline = System.nanoTime() + COMMAND_TIMEOUT_NS

                while (true) {
                    context.ensureActive()
                    if (System.nanoTime() >= deadline) throw SocketTimeoutException("Command timed out")
                    val count = try {
                        input.read(buffer)
                    } catch (_: SocketTimeoutException) {
                        continue
                    }
                    if (count < 0) throw IOException("Connection closed before response prompt")
                    for (index in 0 until count) {
                        if (response.size() >= MAX_RESPONSE_BYTES) {
                            throw IOException("Response exceeds $MAX_RESPONSE_BYTES bytes")
                        }
                        val byte = buffer[index]
                        response.write(byte.toInt())
                        if (byte == PROMPT) {
                            return@withContext ObdResult.Success(
                                response.toString(StandardCharsets.US_ASCII.name())
                            )
                        }
                    }
                }
                @Suppress("UNREACHABLE_CODE")
                ObdResult.Failure("No response")
            }
        } catch (cancelled: CancellationException) {
            close(activeSocket)
            throw cancelled
        } catch (error: Exception) {
            close(activeSocket)
            ObdResult.Failure(error.message ?: "Socket communication failed")
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        socket.getAndSet(null)?.let(::close)
    }

    private fun close(target: Socket) {
        socket.compareAndSet(target, null)
        runCatching { target.close() }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5_000
        const val READ_POLL_TIMEOUT_MS = 250
        const val COMMAND_TIMEOUT_NS = 10_000_000_000L
        const val MAX_RESPONSE_BYTES = 64 * 1024
        const val CARRIAGE_RETURN = 13
        const val PROMPT = '>'.code.toByte()
    }
}
