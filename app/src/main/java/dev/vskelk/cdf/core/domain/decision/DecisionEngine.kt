package dev.vskelk.cdf.core.domain.decision

import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import dev.vskelk.cdf.core.network.resilience.CircuitBreaker
import dev.vskelk.cdf.core.network.resilience.NetworkMonitor // ⚡ EL IMPORT EXACTO DE TU ARCHIVO
import kotlinx.coroutines.flow.first

class DecisionEngine(
    private val preferencesDataSource: PreferencesDataSource,
    private val circuitBreaker: CircuitBreaker,
    private val networkMonitor: NetworkMonitor
) {

    suspend fun evaluate(): DecisionPath {
        val hasApiKey = preferencesDataSource.hasActiveApiKey.first()
        val online = networkMonitor.isOnline()
        val breakerOpen = !circuitBreaker.allowsRequest()
        val offlineMode = preferencesDataSource.isOfflineMode.first()
        val decisionEngineEnabled = preferencesDataSource.isDecisionEngineEnabled.first()
        val hasCache = checkCacheAvailability()

        return evaluate(hasApiKey, online, breakerOpen, offlineMode, decisionEngineEnabled, hasCache)
    }

    fun evaluate(
        hasApiKey: Boolean,
        online: Boolean,
        breakerOpen: Boolean,
        offlineMode: Boolean,
        decisionEngineEnabled: Boolean,
        hasCache: Boolean
    ): DecisionPath {
        if (!hasApiKey) return DecisionPath.BLOCK_MISSING_KEY
        if (!decisionEngineEnabled) return if (online) DecisionPath.SEND_REMOTE else DecisionPath.QUEUE_AND_DEFER
        if (offlineMode) return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        if (breakerOpen) return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        if (!online) return if (hasCache) DecisionPath.SERVE_CACHE else DecisionPath.QUEUE_AND_DEFER
        return DecisionPath.SEND_REMOTE
    }

    private fun checkCacheAvailability(): Boolean = false

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

enum class DecisionPath {
    SEND_REMOTE,
    SERVE_CACHE,
    QUEUE_AND_DEFER,
    BLOCK_MISSING_KEY
}

data class DecisionDebugInfo(
    val hasApiKey: Boolean,
    val online: Boolean,
    val breakerState: dev.vskelk.cdf.core.network.resilience.CircuitState,
    val offlineMode: Boolean,
    val decisionEngineEnabled: Boolean,
    val hasCache: Boolean,
    val currentPath: DecisionPath
)
