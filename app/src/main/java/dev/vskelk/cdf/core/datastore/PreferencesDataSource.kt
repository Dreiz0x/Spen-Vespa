package dev.vskelk.cdf.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dev.vskelk.cdf.core.datastore.proto.ProviderProto
import dev.vskelk.cdf.core.datastore.proto.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * PreferencesDataSource - Acceso centralizado a preferencias del usuario
 *
 * Maneja la persistencia de preferencias usando Proto DataStore,
 * incluyendo el cifrado de API keys.
 */
// ⚡ FUERA @Singleton y @Inject. Hilt lo arma en el DataStoreModule.
// ⚡ FUERA el Parser. No se usaba y era lo que rompía KSP.
class PreferencesDataSource(
    private val context: Context,
    private val cipherService: CipherService
) {
    private val dataStore: DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer(),
        produceFile = { context.dataStoreFile("user_preferences.pb") },
        corruptionHandler = ReplaceFileCorruptionHandler { UserPreferences.getDefaultInstance() }
    )

    /**
     * Flow reactivo de preferencias
     */
    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    // ===== MODIFICADORES =====

    suspend fun setOfflineMode(enabled: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setOfflineMode(enabled).build()
        }
    }

    suspend fun setDecisionEngineEnabled(enabled: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setDecisionEngineEnabled(enabled).build()
        }
    }

    suspend fun setActiveProvider(provider: ProviderProto) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setActiveProvider(provider).build()
        }
    }

    suspend fun setApiKey(provider: String, apiKey: String) {
        // Cifrar la API key antes de almacenar
        val encrypted = cipherService.encrypt(apiKey)
        dataStore.updateData { prefs ->
            val updated = prefs.apiKeysMap.toMutableMap()
            updated[provider] = encrypted.toStorageString()
            prefs.toBuilder()
                .clearApiKeys()
                .putAllApiKeys(updated)
                .build()
        }
    }

    suspend fun getApiKey(provider: String): String? {
        // ⚡ CORREGIDO: usar first() en lugar de last() para no congelar la app.
        val prefs = dataStore.data.first()
        val encrypted = prefs.apiKeysMap[provider] ?: return null
        return try {
            val cipherResult = CipherResult.fromStorageString(encrypted)
            cipherService.decrypt(cipherResult)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearApiKey(provider: String) {
        dataStore.updateData { prefs ->
            val updated = prefs.apiKeysMap.toMutableMap()
            updated.remove(provider)
            prefs.toBuilder()
                .clearApiKeys()
                .putAllApiKeys(updated)
                .build()
        }
    }

    suspend fun setSeedVersionApplied(version: String) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setSeedVersionApplied(version).build()
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setOnboardingCompleted(completed).build()
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setNotificationsEnabled(enabled).build()
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setLastSyncTimestamp(timestamp).build()
        }
    }

    // ===== OBSERVADORES CONVENIENTES =====

    val isOfflineMode: Flow<Boolean> = preferencesFlow.map { it.offlineMode }
    val isDecisionEngineEnabled: Flow<Boolean> = preferencesFlow.map { it.decisionEngineEnabled }
    val activeProvider: Flow<ProviderProto> = preferencesFlow.map { it.activeProvider }
    val seedVersionApplied: Flow<String> = preferencesFlow.map { it.seedVersionApplied }
    val isOnboardingCompleted: Flow<Boolean> = preferencesFlow.map { it.onboardingCompleted }
    val notificationsEnabled: Flow<Boolean> = preferencesFlow.map { it.notificationsEnabled }
    val lastSyncTimestamp: Flow<Long> = preferencesFlow.map { it.lastSyncTimestamp }

    /**
     * Verifica si hay API key configurada para el proveedor activo
     */
    val hasActiveApiKey: Flow<Boolean> = preferencesFlow.map { prefs ->
        val apiKeyEntry = prefs.apiKeysMap[prefs.activeProvider.name]
        apiKeyEntry != null
    }

    /**
     * Proveedor activo como enum de dominio
     */
    fun getActiveProviderFlow(): Flow<LlmProvider> = activeProvider.map { proto ->
        when (proto) {
            ProviderProto.PROVIDER_ANTHROPIC -> LlmProvider.ANTHROPIC
            ProviderProto.PROVIDER_GEMINI -> LlmProvider.GEMINI
            ProviderProto.PROVIDER_OPENAI -> LlmProvider.OPENAI
            else -> LlmProvider.UNSPECIFIED
        }
    }
}

/**
 * Enum de proveedores LLM - dominio
 */
enum class LlmProvider {
    UNSPECIFIED,
    ANTHROPIC,
    GEMINI,
    OPENAI;

    fun toProto(): ProviderProto = when (this) {
        UNSPECIFIED -> ProviderProto.PROVIDER_UNSPECIFIED
        ANTHROPIC -> ProviderProto.PROVIDER_ANTHROPIC
        GEMINI -> ProviderProto.PROVIDER_GEMINI
        OPENAI -> ProviderProto.PROVIDER_OPENAI
    }

    companion object {
        fun fromProto(proto: ProviderProto): LlmProvider = when (proto) {
            ProviderProto.PROVIDER_ANTHROPIC -> ANTHROPIC
            ProviderProto.PROVIDER_GEMINI -> GEMINI
            ProviderProto.PROVIDER_OPENAI -> OPENAI
            else -> UNSPECIFIED
        }
    }
}
