package dev.vskelk.cdf.core.network.datasource

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.blob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRemoteDataSource @Inject constructor() {

    /**
     * Procesa texto o archivos (PDF) directamente con Gemini 1.5 Pro.
     * Si mandas [pdfBytes], Gemini lo analiza como documento multimodal.
     */
    suspend fun generateContent(
        apiKey: String,
        prompt: String,
        pdfBytes: ByteArray? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Declaramos el modelo aquí mismo para usar la API Key dinámica del DataStore
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-pro",
                apiKey = apiKey
            )

            val response = if (pdfBytes != null) {
                // ⚡ AQUÍ ESTÁ EL TRUCO DEL PDF: Multimodal nativo
                generativeModel.generateContent(
                    content {
                        blob("application/pdf", pdfBytes)
                        text(prompt)
                    }
                )
            } else {
                generativeModel.generateContent(prompt)
            }

            val resultText = response.text
            if (resultText != null) {
                Result.success(resultText)
            } else {
                Result.failure(Exception("Gemini regresó una respuesta vacía"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
