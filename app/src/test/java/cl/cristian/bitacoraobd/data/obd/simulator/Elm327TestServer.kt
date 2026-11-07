package cl.cristian.bitacoraobd.data.obd.simulator

import java.io.BufferedInputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Elm327TestServer(
    private val respond: (String, Socket, OutputStream) -> Unit
) : AutoCloseable {
    private val server = ServerSocket(0)
    private val activeSocket = java.util.concurrent.atomic.AtomicReference<Socket?>()
    private val closed = java.util.concurrent.atomic.AtomicBoolean(false)
    private val accepted = CountDownLatch(1)
    private val thread = Thread(::serve, "elm327-test-server").apply {
        isDaemon = true
        start()
    }

    val port: Int get() = server.localPort
    val commands = CopyOnWriteArrayList<String>()

    fun awaitConnection(timeout: Long = 2, unit: TimeUnit = TimeUnit.SECONDS): Boolean =
        accepted.await(timeout, unit)

    private fun serve() {
        try {
            while (!closed.get()) {
                val socket = server.accept()
                activeSocket.set(socket)
                accepted.countDown()
                socket.use { connection ->
                    val input = BufferedInputStream(connection.getInputStream())
                    val command = StringBuilder()
                    while (!closed.get()) {
                        val byte = input.read()
                        if (byte < 0) break
                        if (byte == 13) {
                            val value = command.toString()
                            commands += value
                            command.setLength(0)
                            respond(value, connection, connection.getOutputStream())
                        } else {
                            command.append(byte.toChar())
                        }
                    }
                }
                activeSocket.compareAndSet(socket, null)
            }
        } catch (error: java.net.SocketException) {
            if (!closed.get()) throw error
        } catch (error: java.io.IOException) {
            if (!closed.get()) throw error
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { activeSocket.getAndSet(null)?.close() }
            runCatching { server.close() }
            thread.join(2_000)
        }
    }
}
