package cl.cristian.bitacoraobd.domain.obd

import kotlinx.coroutines.flow.StateFlow

interface ObdGateway {
    val connectionState: StateFlow<ObdConnectionState>

    suspend fun connect(config: ObdConnectionConfig): ObdResult<Unit>
    suspend fun disconnect()
    suspend fun readLiveMetrics(): ObdResult<ObdLiveMetrics>
    suspend fun readDiagnosticCodes(): ObdResult<List<String>>
}
