package cl.cristian.bitacoraobd.domain.obd

sealed interface ObdResult<out T> {
    data class Success<T>(val value: T) : ObdResult<T>
    data class Failure(val message: String) : ObdResult<Nothing>
}
