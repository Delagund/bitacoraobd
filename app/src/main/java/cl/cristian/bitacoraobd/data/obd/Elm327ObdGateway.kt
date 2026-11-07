package cl.cristian.bitacoraobd.data.obd

import cl.cristian.bitacoraobd.data.obd.connection.Elm327SocketClient
import cl.cristian.bitacoraobd.data.obd.parser.ObdPidInterpreter
import cl.cristian.bitacoraobd.data.obd.parser.ObdResponseParser
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdLiveMetrics
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

class Elm327ObdGateway(
    private val client: Elm327SocketClient = Elm327SocketClient(),
    private val parser: ObdResponseParser = ObdResponseParser(),
    private val interpreter: ObdPidInterpreter = ObdPidInterpreter()
) : ObdGateway {
    private val mutableConnectionState = MutableStateFlow<ObdConnectionState>(
        ObdConnectionState.Disconnected
    )
    override val connectionState: StateFlow<ObdConnectionState> = mutableConnectionState.asStateFlow()

    private val operationMutex = Mutex()
    private val generation = AtomicLong()
    private val stateLock = Any()

    override suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit> =
        operationMutex.withLock {
            val attempt = generation.incrementAndGet()
            setState(attempt, ObdConnectionState.Connecting)
            try {
                val socketResult = client.connect(config)
                if (socketResult is ObdResult.Failure) return@withLock fail(attempt, socketResult.message)
                if (!isCurrentAttempt(attempt, ObdConnectionState.Connecting)) {
                    client.disconnect()
                    return@withLock ObdResult.Failure("Connection was interrupted")
                }

                for (command in INITIALIZATION_COMMANDS) {
                    if (!isCurrentAttempt(attempt, ObdConnectionState.Connecting)) {
                        client.disconnect()
                        return@withLock ObdResult.Failure("Connection was interrupted")
                    }
                    val response = client.command(command)
                    if (response is ObdResult.Failure) return@withLock fail(attempt, response.message)
                    val text = (response as ObdResult.Success).value
                    val valid = when (command) {
                        "ATZ" -> text.contains("ELM327", ignoreCase = true)
                        "0100" -> parser.parsePidResponse(text, 0x00) is ObdResult.Success
                        else -> text.lineSequence()
                            .filterNot { it.trim().equals(command, ignoreCase = true) }
                            .joinToString(" ")
                            .replace(">", "")
                            .trim()
                            .equals("OK", ignoreCase = true)
                    }
                    if (!valid) return@withLock fail(attempt, "Invalid response to $command")
                }

                val connected = synchronized(stateLock) {
                    if (generation.get() != attempt ||
                        mutableConnectionState.value != ObdConnectionState.Connecting
                    ) {
                        false
                    } else {
                        mutableConnectionState.value = ObdConnectionState.Connected(config)
                        true
                    }
                }
                if (!connected) {
                    client.disconnect()
                    return@withLock ObdResult.Failure("Connection was interrupted")
                }
                ObdResult.Success(Unit)
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) { client.disconnect() }
                setState(attempt, ObdConnectionState.Disconnected)
                throw cancelled
            } catch (error: Exception) {
                fail(attempt, error.message ?: "OBD initialization failed")
            }
        }

    override suspend fun disconnect() {
        val disconnectGeneration = generation.incrementAndGet()
        synchronized(stateLock) {
            mutableConnectionState.value = ObdConnectionState.Disconnecting
        }
        client.disconnect()
        setState(disconnectGeneration, ObdConnectionState.Disconnected)
    }

    override suspend fun readLiveMetrics(): ObdResult<ObdLiveMetrics> = operationMutex.withLock {
        val connected = mutableConnectionState.value as? ObdConnectionState.Connected
            ?: return@withLock ObdResult.Failure("OBD adapter is not connected")
        val attempt = generation.get()
        try {
            val rpm = readPid("010C", 0x0C)
            if (rpm is ObdResult.Failure) return@withLock fail(attempt, rpm.message)
            val temperature = readPid("0105", 0x05)
            if (temperature is ObdResult.Failure) return@withLock fail(attempt, temperature.message)
            val speed = readPid("010D", 0x0D)
            if (speed is ObdResult.Failure) return@withLock fail(attempt, speed.message)

            if (!isCurrentConnection(attempt, connected)) {
                return@withLock ObdResult.Failure("Connection was interrupted")
            }
            ObdResult.Success(
                ObdLiveMetrics(
                    rpm = (rpm as ObdResult.Success).value,
                    coolantTempCelsius = (temperature as ObdResult.Success).value,
                    speedKmh = (speed as ObdResult.Success).value,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { client.disconnect() }
            setState(attempt, ObdConnectionState.Disconnected)
            throw cancelled
        } catch (error: Exception) {
            fail(attempt, error.message ?: "OBD communication failed")
        }
    }

    override suspend fun readDiagnosticCodes(): ObdResult<List<String>> = operationMutex.withLock {
        val connected = mutableConnectionState.value as? ObdConnectionState.Connected
            ?: return@withLock ObdResult.Failure("OBD adapter is not connected")
        val attempt = generation.get()
        try {
            when (val response = client.command("03")) {
                is ObdResult.Failure -> fail(attempt, response.message)
                is ObdResult.Success -> when (val parsed = parser.parseDiagnosticCodes(response.value)) {
                    is ObdResult.Failure -> parsed
                    is ObdResult.Success -> if (isCurrentConnection(attempt, connected)) parsed
                        else ObdResult.Failure("Connection was interrupted")
                }
            }
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { client.disconnect() }
            setState(attempt, ObdConnectionState.Disconnected)
            throw cancelled
        } catch (error: Exception) {
            fail(attempt, error.message ?: "OBD communication failed")
        }
    }

    private suspend fun readPid(command: String, pid: Int): ObdResult<Int?> =
        when (val response = client.command(command)) {
            is ObdResult.Failure -> response
            is ObdResult.Success -> when (val parsed = parser.parsePidResponse(response.value, pid)) {
                is ObdResult.Failure -> ObdResult.Success(null)
                is ObdResult.Success -> ObdResult.Success(
                    when (pid) {
                        0x0C -> interpreter.rpm(parsed.value)
                        0x05 -> interpreter.coolantTempCelsius(parsed.value)
                        else -> interpreter.speedKmh(parsed.value)
                    }
                )
            }
        }

    private suspend fun fail(attempt: Long, message: String): ObdResult.Failure {
        client.disconnect()
        setState(attempt, ObdConnectionState.Error(message))
        return ObdResult.Failure(message)
    }

    private fun setState(attempt: Long, state: ObdConnectionState) {
        synchronized(stateLock) {
            if (generation.get() == attempt) mutableConnectionState.value = state
        }
    }

    private fun isCurrentConnection(
        attempt: Long,
        connected: ObdConnectionState.Connected
    ): Boolean = synchronized(stateLock) {
        generation.get() == attempt && mutableConnectionState.value == connected
    }

    private fun isCurrentAttempt(attempt: Long, state: ObdConnectionState): Boolean =
        synchronized(stateLock) {
            generation.get() == attempt && mutableConnectionState.value == state
        }

    private companion object {
        val INITIALIZATION_COMMANDS = listOf("ATZ", "ATE0", "ATH0", "ATSP0", "0100")
    }
}
