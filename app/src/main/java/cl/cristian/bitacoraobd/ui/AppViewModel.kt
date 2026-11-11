package cl.cristian.bitacoraobd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(val session: AppSession) : ViewModel() {
    val state: StateFlow<AppSessionState> = session.state
    val connectionState = session.connectionState

    class Factory(private val session: AppSession) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AppViewModel::class.java))
            return AppViewModel(session) as T
        }
    }
}
