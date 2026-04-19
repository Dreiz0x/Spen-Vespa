package dev.vskelk.cdf.core.common

/**
 * AppCommon - Utilidades comunes de la aplicación
 */

/**
 * Dispatchers nombrados para uso en la aplicación
 */
object AppDispatchers {
    const val IO = "kotlinx.coroutines.IO"
    const val DEFAULT = "kotlinx.coroutines.DEFAULT"
    const val MAIN = "kotlinx.coroutines.Main"
}

/**
 * Result wrapper para operaciones que pueden fallar
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: IllegalStateException(message)
    }

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String, Throwable?) -> Unit): AppResult<T> {
        if (this is Error) action(message, cause)
        return this
    }

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun error(message: String, cause: Throwable? = null): AppResult<Nothing> = Error(message, cause)
    }
}

/**
 * Errors comunes de la aplicación
 */
object AppError {
    const val NO_API_KEY = "No se ha configurado clave API"
    const val NETWORK_ERROR = "Error de conexión a internet"
    const val CIRCUIT_OPEN = "El servicio no está disponible temporalmente"
    const val PARSE_ERROR = "Error al procesar la respuesta"
    const val DATABASE_ERROR = "Error de base de datos"
    const val UNKNOWN_ERROR = "Error desconocido"

    fun network(cause: Throwable? = null): AppResult<Nothing> =
        AppResult.error(NETWORK_ERROR, cause)

    fun noApiKey(): AppResult<Nothing> =
        AppResult.error(NO_API_KEY)

    fun circuitOpen(): AppResult<Nothing> =
        AppResult.error(CIRCUIT_OPEN)

    fun parse(cause: Throwable): AppResult<Nothing> =
        AppResult.error(PARSE_ERROR, cause)

    fun database(cause: Throwable): AppResult<Nothing> =
        AppResult.error(DATABASE_ERROR, cause)

    fun unknown(cause: Throwable): AppResult<Nothing> =
        AppResult.error(UNKNOWN_ERROR, cause)
}
