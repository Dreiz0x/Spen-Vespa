package dev.vskelk.cdf.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * RetryBackoffInterceptor - Retry con backoff exponencial
 *
 * Per spec: "RetryBackoffInterceptor – 3 reintentos, backoff exponencial"
 *
 * Reintenta requests fallidos con delays crecientes:
 * - Intento 1: inmediato
 * - Intento 2: 1 segundo
 * - Intento 3: 4 segundos (2^2)
 */
@Singleton
class RetryBackoffInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 10_000L

        // Códigos HTTP que merecen reintento
        private val RETRYABLE_STATUS_CODES = setOf(
            408, // Request Timeout
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        for (attempt in 0 until MAX_RETRIES) {
            try {
                response?.close() // Cerrar respuesta anterior si existe

                val newRequest = request.newBuilder()
                    .header("X-Retry-Attempt", attempt.toString())
                    .build()

                response = chain.proceed(newRequest)

                // Verificar si debemos reintentar
                if (isRetryable(response)) {
                    if (attempt < MAX_RETRIES - 1) {
                        // Esperar antes de reintentar
                        val delay = calculateDelay(attempt)
                        Thread.sleep(delay)
                        continue
                    }
                }

                // Si llegamos aquí, no necesitamos reintentar
                return response

            } catch (e: IOException) {
                exception = e
                if (attempt < MAX_RETRIES - 1) {
                    val delay = calculateDelay(attempt)
                    Thread.sleep(delay)
                } else {
                    throw e
                }
            }
        }

        // Si llegamos aquí después de todos los reintentos
        return response ?: throw exception ?: IOException("Unknown error after retries")
    }

    private fun isRetryable(response: Response): Boolean {
        return response.code in RETRYABLE_STATUS_CODES
    }

    /**
     * Calcula el delay con backoff exponencial
     * Con jitter para evitar thundering herd
     */
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * 2.0.pow(attempt.toDouble()).toLong()
        val cappedDelay = minOf(exponentialDelay, MAX_DELAY_MS)

        // Agregar jitter (±25%)
        val jitter = (cappedDelay * 0.25 * Math.random()).toLong()

        return if (attempt % 2 == 0) {
            cappedDelay - jitter
        } else {
            cappedDelay + jitter
        }
    }
}
