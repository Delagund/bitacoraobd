package cl.cristian.bitacoraobd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(val dashboard: DashboardState? = null, val error: String? = null, val loading: Boolean = true)

class HomeViewModel(private val getDashboard: GetDashboardDataUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = mutableState.asStateFlow()
    private var collection: Job? = null
    private var activation: Job? = null
    private var generation = 0

    fun setActive(active: Boolean) {
        val request = ++generation
        activation?.cancel()
        activation = viewModelScope.launch {
            collection?.cancelAndJoin()
            collection = null
            if (!active || request != generation) return@launch
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            collection = viewModelScope.launch {
                try {
                    getDashboard().collect { dashboard ->
                        mutableState.value = HomeState(dashboard = dashboard, loading = false)
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    mutableState.value = mutableState.value.copy(
                        error = error.message ?: "Could not load dashboard",
                        loading = false
                    )
                }
            }
        }
    }

    fun retry() = setActive(true)

    class Factory(private val getDashboard: GetDashboardDataUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(getDashboard) as T
        }
    }
}
