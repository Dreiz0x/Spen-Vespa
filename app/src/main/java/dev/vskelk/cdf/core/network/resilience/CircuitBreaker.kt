package dev.vskelk.cdf.core.network.resilience

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CircuitBreaker - Implementación del patrón Circuit Breaker
 *
 * Per spec: "Se abre tras 5 fallos, pausa 60 segundos"
 *
 * Estados:
 * - CLOSED: Operación normal, requests pasan directamente
 * - OPEN: Circuit abierto, requests se rechazan inmediatamente
 * - HALF_OPEN: Después del timeout, se permite un request de prueba
 */
@Singleton
class CircuitBreaker @Inject constructor() {

    companion object {
        private const val FAILURE_THRESHOLD = 5
        private const val RECOVERY_TIMEOUT_MS = 60_000L // 60 segundos
        private const val SUCCESS_THRESHOLD_HALF_OPEN = 1
    }

    private val _state = MutableStateFlow(CircuitState.CLOSED)
    val state: StateFlow<CircuitState> = _state.asStateFlow()

    private var failureCount = 0
    private var lastFailureTime = 0L
    private var successCountInHalfOpen = 0

    /**
     * Intenta ejecutar una acción con el circuit breaker
     * @return Result con éxito o CircuitOpenException
     */
    suspend fun <T> execute(action: suspend () -> T): Result<T> {
        return when (_state.value) {
            CircuitState.CLOSED -> executeClosed(action)
            CircuitState.OPEN -> executeOpen(action)
            CircuitState.HALF_OPEN -> executeHalfOpen(action)
        }
    }

    private suspend fun <T> executeClosed(action: suspend () -> T): Result<T> {
        return try {
            val result = action()
            resetFailures()
            Result.success(result)
        } catch (e: Exception) {
            recordFailure()
            Result.failure(e)
        }
    }

    private suspend fun <T> executeOpen(action: suspend () -> T): Result<T> {
        // Verificar si debemos pasar a HALF_OPEN
        if (shouldAttemptRecovery()) {
            transitionToHalfOpen()
            return executeHalfOpen(action)
        }
        return Result.failure(CircuitOpenException())
    }

    private suspend fun <T> executeHalfOpen(action: suspend () -> T): Result<T> {
        return try {
            val result = action()
            recordSuccessInHalfOpen()
            Result.success(result)
        } catch (e: Exception) {
            transitionToOpen()
            Result.failure(e)
        }
    }

    private fun recordFailure() {
        failureCount++
        lastFailureTime = System.currentTimeMillis()

        if (failureCount >= FAILURE_THRESHOLD) {
            transitionToOpen()
        }
    }

    private fun recordSuccessInHalfOpen() {
        successCountInHalfOpen++
        if (successCountInHalfOpen >= SUCCESS_THRESHOLD_HALF_OPEN) {
            resetFailures()
            transitionToClosed()
        }
    }

    private fun resetFailures() {
        failureCount = 0
        successCountInHalfOpen = 0
    }

    private fun shouldAttemptRecovery(): Boolean {
        val elapsed = System.currentTimeMillis() - lastFailureTime
        return elapsed >= RECOVERY_TIMEOUT_MS
    }

    private fun transitionToOpen() {
        _state.value = CircuitState.OPEN
    }

    private fun transitionToHalfOpen() {
        _state.value = CircuitState.HALF_OPEN
        successCountInHalfOpen = 0
    }

    private fun transitionToClosed() {
        _state.value = CircuitState.CLOSED
        resetFailures()
    }

    /**
     * Resetea manualmente el circuit breaker
     */
    fun reset() {
        resetFailures()
        _state.value = CircuitState.CLOSED
    }

    /**
     * Verifica si el circuit está cerrado (operación normal)
     */
    fun isClosed(): Boolean = _state.value == CircuitState.CLOSED

    /**
     * Verifica si el circuit permite requests
     */
    fun allowsRequest(): Boolean = _state.value != CircuitState.OPEN
}

/**
 * Estados del Circuit Breaker
 */
enum class CircuitState {
    /** Operación normal */
    CLOSED,

    /** Circuit abierto, requests bloqueados */
    OPEN,

    /** Recuperación en progreso */
    HALF_OPEN
}

/**
 * Exception cuando el circuit breaker está abierto
 */
class CircuitOpenException : Exception("Circuit breaker is open") {
    override val message: String = "El servicio no está disponible temporalmente. Intenta más tarde."
}
