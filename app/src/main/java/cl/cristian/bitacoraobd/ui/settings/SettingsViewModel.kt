package cl.cristian.bitacoraobd.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferences: AppPreferencesRepository) : ViewModel() {
    var draft by mutableStateOf<AppSettings?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var pendingLeave by mutableStateOf(false)
        private set
    var leaveTarget by mutableStateOf<String?>(null)
        private set

    private var saved: AppSettings? = null

    val dirty: Boolean get() = draft != null && draft != saved

    init { reload() }

    fun reload() {
        if (saving) return
        loading = true
        error = null
        viewModelScope.launch {
            try {
                val current = preferences.settings.first()
                saved = current
                draft = current
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = failure.message ?: "No se pudieron cargar los ajustes"
            } finally {
                loading = false
            }
        }
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        draft = draft?.let(transform)
        error = null
    }

    fun save() {
        val value = draft ?: return
        if (saving || loading) return
        saving = true
        error = null
        viewModelScope.launch {
            try {
                preferences.saveSettings(value)
                saved = value
                draft = value
                pendingLeave = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = failure.message ?: "No se pudieron guardar los ajustes"
            } finally {
                saving = false
            }
        }
    }

    fun requestLeave(target: String) {
        leaveTarget = target
        pendingLeave = dirty && !saving
    }

    fun takeSuccessfulLeaveTarget(): String? {
        if (saving || dirty || error != null) return null
        return leaveTarget.also { leaveTarget = null }
    }

    fun keepEditing() { pendingLeave = false; leaveTarget = null }

    fun discard(): String? {
        if (saving) return null
        draft = saved
        error = null
        pendingLeave = false
        return leaveTarget.also { leaveTarget = null }
    }

    class Factory(private val preferences: AppPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
            return SettingsViewModel(preferences) as T
        }
    }
}
