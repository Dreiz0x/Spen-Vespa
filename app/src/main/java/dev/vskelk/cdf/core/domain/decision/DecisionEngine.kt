package dev.vskelk.cdf.core.domain.decision

import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import dev.vskelk.cdf.core.network.resilience.CircuitBreaker
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DecisionEngine - Motor de decisión determinista
 *
 * Per spec:
 * - "Algoritmo determinista. No llama a la IA. Evalúa condiciones del sistema."
 * - "El DecisionEngine es determinista. No llama a la IA nunca."
 *
 * Este motor decide qué hacer con una solicitud basándose en:
 * - Si hay API key configurada
 * - Si hay conexión a internet
 * - Si el circuit breaker está abierto
 * - Si el modo offline está habilitado
 * - Si hay caché disponible
 * - Si el motor de decisión está habilitado
 */
@Singleton
class DecisionEngine @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val circuitBreaker: CircuitBreaker,
    private val networkMonitor: NetworkMonitor
) {

    /**
     * Evalúa el estado del sistema y retorna la ruta de decisión apropiada
     */
    suspend fun evaluate(): DecisionPath {
        val hasApiKey = preferencesDataSource.hasActiveApiKey.first()
        val online = networkMonitor.isOnline()
        val breakerOpen = !circuitBreaker.allowsRequest()
        val offlineMode = preferencesDataSource.isOfflineMode.first()
        val decisionEngineEnabled = preferencesDataSource.isDecisionEngineEnabled.first()
        val hasCache = checkCacheAvailability()

        return evaluate(
            hasApiKey = hasApiKey,
            online = online,
            breakerOpen = breakerOpen,
            offlineMode = offlineMode,
            decisionEngineEnabled = decisionEngineEnabled,
            hasCache = hasCache
        )
    }

    /**
     * Lógica de decisión determinista
     *
     * Per spec:
     * if (!hasApiKey) → BLOCK_MISSING_KEY
     * if (!decisionEngineEnabled) → online ? SEND_REMOTE : QUEUE_AND_DEFER
     * if (offlineMode) → hasCache ? SERVE_CACHE : QUEUE_AND_DEFER
     * if (breakerOpen) → hasCache ? SERVE_CACHE : QUEUE_AND_DEFER
     * if (!online) → hasCache ? SERVE_CACHE : QUEUE_AND_DEFER
     * else → SEND_REMOTE
     */
    fun evaluate(
        hasApiKey: Boolean,
        online: Boolean,
        breakerOpen: Boolean,
        offlineMode: Boolean,
        decisionEngineEnabled: Boolean,
        hasCache: Boolean
    ): DecisionPath {
        // Paso 1: Sin API key, no hay nada que hacer
        if (!hasApiKey) {
            return DecisionPath.BLOCK_MISSING_KEY
        }

        // Paso 2: Motor de decisión deshabilitado
        if (!decisionEngineEnabled) {
            return if (online) DecisionPath.SEND_REMOTE else DecisionPath.QUEUE_AND_DEFER
        }

        // Paso 3: Modo offline
        if (offlineMode) {
            return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        }

        // Paso 4: Circuit breaker abierto
        if (breakerOpen) {
            return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        }

        // Paso 5: Sin conexión
        if (!online) {
            return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        }

        // Paso 6: Todo bien, enviar al proveedor
        return DecisionPath.SEND_REMOTE
    }

    /**
     * Verifica si hay caché disponible para servir
     */
    private fun checkCacheAvailability(): Boolean {
        // Implementar lógica de verificación de caché
        // Por ahora retorna false - se implementará con Room
        return false
    }

    /**
     * Obtiene información de debug para diagnóstico
     */
    suspend fun getDebugInfo(): DecisionDebugInfo {
        return DecisionDebugInfo(
            hasApiKey = preferencesDataSource.hasActiveApiKey.first(),
            online = networkMonitor.isOnline(),
            breakerState = circuitBreaker.state.value,
            offlineMode = preferencesDataSource.isOfflineMode.first(),
            decisionEngineEnabled = preferencesDataSource.isDecisionEngineEnabled.first(),
            hasCache = checkCacheAvailability(),
            currentPath = evaluate()
        )
    }
}

/**
 * Rutas de decisión disponibles
 */
enum class DecisionPath {
    /** Llamar al proveedor LLM activo */
    SEND_REMOTE,

    /** Responder desde Room/caché */
    SERVE_CACHE,

    /** Encolar para enviar cuando haya conexión */
    QUEUE_AND_DEFER,

    /** Solicitar configuración de API key al usuario */
    BLOCK_MISSING_KEY
}

/**
 * Información de debug del motor de decisión
 */
data class DecisionDebugInfo(
    val hasApiKey: Boolean,
    val online: Boolean,
    val breakerState: dev.vskelk.cdf.core.network.resilience.CircuitState,
    val offlineMode: Boolean,
    val decisionEngineEnabled: Boolean,
    val hasCache: Boolean,
    val currentPath: DecisionPath
)
