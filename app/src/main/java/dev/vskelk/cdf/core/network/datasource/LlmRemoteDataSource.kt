package dev.vskelk.cdf.core.network.datasource

import dev.vskelk.cdf.core.network.dto.AnthropicRequest
import dev.vskelk.cdf.core.network.dto.MessageDto
import dev.vskelk.cdf.core.network.resilience.CircuitBreaker
import dev.vskelk.cdf.core.network.resilience.CircuitOpenException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LlmRemoteDataSource - Interfaz multi-proveedor para LLMs
 *
 * Per spec:
 * - "LlmRemoteDataSource es una interfaz. Nunca hay acoplamiento directo a Anthropic en capas superiores."
 * - "Solo AnthropicRemoteDataSource está implementado. Las demás retornan Result.failure."
 *
 * Esta interfaz define el contrato que todos los proveedores deben cumplir.
 */
interface LlmRemoteDataSource {

    /**
     * Envía un mensaje al proveedor LLM
     *
     * @param apiKey Clave API del proveedor
     * @param prompt Prompt del usuario
     * @param history Historial de conversación como pares (role, content)
     * @return Result con la respuesta del LLM o error
     */
    suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList()
    ): Result<String>
}

/**
 * Implementación para Anthropic Claude
 */
@Singleton
class AnthropicRemoteDataSource @Inject constructor(
    private val api: dev.vskelk.cdf.core.network.api.AnthropicApi,
    private val circuitBreaker: CircuitBreaker
) : LlmRemoteDataSource {

    override suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        // Usar CircuitBreaker
        circuitBreaker.execute {
            executeRequest(apiKey, prompt, history)
        }.mapCatching { response ->
            response
        }
    }

    private suspend fun executeRequest(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): String {
        // Construir mensajes
        val messages = buildMessages(prompt, history)

        val request = AnthropicRequest(
            messages = messages
        )

        val response = api.sendMessage(
            apiKey = apiKey,
            request = request
        )

        // Extraer texto de la respuesta
        return extractTextFromResponse(response)
    }

    private fun buildMessages(
        prompt: String,
        history: List<Pair<String, String>>
    ): List<MessageDto> {
        val messages = mutableListOf<MessageDto>()

        // Agregar historial
        history.forEach { (role, content) ->
            messages.add(MessageDto(role = role, content = content))
        }

        // Agregar mensaje actual
        messages.add(MessageDto(role = "user", content = prompt))

        return messages
    }

    private fun extractTextFromResponse(response: dev.vskelk.cdf.core.network.dto.AnthropicResponse): String {
        val textBlocks = response.content.filterIsInstance<dev.vskelk.cdf.core.network.dto.ContentBlock.TextBlock>()
        return textBlocks.joinToString("\n") { it.text }
    }
}

/**
 * Implementación placeholder para Gemini
 */
@Singleton
class GeminiRemoteDataSource @Inject constructor() : LlmRemoteDataSource {

    override suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        return Result.failure(
            NotImplementedError("Gemini provider not implemented yet")
        )
    }
}

/**
 * Implementación placeholder para OpenAI
 */
@Singleton
class OpenAiRemoteDataSource @Inject constructor() : LlmRemoteDataSource {

    override suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        return Result.failure(
            NotImplementedError("OpenAI provider not implemented yet")
        )
    }
}

/**
 * Proveedor seleccionado dinámicamente
 */
@Singleton
class MultiProviderLlmDataSource @Inject constructor(
    private val anthropic: AnthropicRemoteDataSource,
    private val gemini: GeminiRemoteDataSource,
    private val openai: OpenAiRemoteDataSource
) : LlmRemoteDataSource {

    private var currentProvider: Provider = Provider.ANTHROPIC

    fun setProvider(provider: Provider) {
        currentProvider = provider
    }

    override suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        return when (currentProvider) {
            Provider.ANTHROPIC -> anthropic.sendMessage(apiKey, prompt, history)
            Provider.GEMINI -> gemini.sendMessage(apiKey, prompt, history)
            Provider.OPENAI -> openai.sendMessage(apiKey, prompt, history)
        }
    }
}

enum class Provider {
    ANTHROPIC,
    GEMINI,
    OPENAI
}
