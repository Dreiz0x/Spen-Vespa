package dev.vskelk.cdf.core.network.datasource

import dev.vskelk.cdf.core.network.gemini.GeminiService // ⚡ ESTO TE FALTABA
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRemoteDataSource @Inject constructor(
    private val geminiService: GeminiService
) : LlmRemoteDataSource {

    override suspend fun sendMessage(
        apiKey: String,
        prompt: String,
        pdfBytes: ByteArray?
    ): Result<String> {
        return geminiService.generateWithVision(
            apiKey = apiKey,
            prompt = prompt,
            pdfBytes = pdfBytes
        )
    }

    suspend fun sendMessageWithJsonResponse(
        apiKey: String,
        prompt: String,
        pdfBytes: ByteArray?
    ): Result<String> {
        return geminiService.generateStructuredJson(
            apiKey = apiKey,
            prompt = prompt,
            pdfBytes = pdfBytes
        )
    }
}
