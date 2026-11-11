package cl.cristian.bitacoraobd.ui.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.ui.AppSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val session: AppSession,
    private val preferences: AppPreferencesRepository
) : ViewModel() {
    var ip by mutableStateOf("")
        private set
    var port by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var loaded by mutableStateOf(false)
        private set
    private var originalIp = ""
    private var originalPort = ""

    val dirty get() = loaded && (ip != originalIp || port != originalPort)

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            try {
                val config = preferences.obdConnectionConfig.first()
                originalIp = config.ipAddress
                originalPort = config.port.toString()
                ip = originalIp
                port = originalPort
                loaded = true
                error = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                loaded = false
                error = "No se pudo cargar la configuración"
            }
        }
    }

    fun editIp(value: String) { ip = value; error = null }
    fun editPort(value: String) { port = value; error = null }

    fun validConfig(): Boolean = try {
        ObdConnectionConfig(ip.trim(), port.toIntOrNull() ?: 0)
        error = null
        true
    } catch (_: IllegalArgumentException) {
        error = "Revisa la dirección IP y el puerto"
        false
    }

    fun connect() {
        if (!validConfig()) return
        session.connect(ObdConnectionConfig(ip.trim(), port.toInt()), true)
    }

    fun markSaved(config: ObdConnectionConfig) {
        originalIp = config.ipAddress
        originalPort = config.port.toString()
    }

    fun discard() {
        ip = originalIp
        port = originalPort
        error = null
    }

    class Factory(
        private val session: AppSession,
        private val preferences: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ConnectionViewModel::class.java))
            return ConnectionViewModel(session, preferences) as T
        }
    }
}
