package cl.cristian.bitacoraobd.domain.obd

sealed interface ObdConnectionState {
    data object Disconnected : ObdConnectionState
    data object Connecting : ObdConnectionState
    data class Connected(val config: ObdConnectionConfig) : ObdConnectionState
    data object Disconnecting : ObdConnectionState
    data class Error(val message: String) : ObdConnectionState
}
