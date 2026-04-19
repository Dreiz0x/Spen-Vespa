package dev.vskelk.cdf.core.network.api

import dev.vskelk.cdf.core.network.dto.AnthropicRequest
import dev.vskelk.cdf.core.network.dto.AnthropicResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit API para Anthropic Claude
 *
 * Per spec: Interfaz LlmRemoteDataSource es multi-proveedor.
 * Solo AnthropicRemoteDataSource está implementado.
 */
interface AnthropicApi {

    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") anthropicVersion: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse
}
