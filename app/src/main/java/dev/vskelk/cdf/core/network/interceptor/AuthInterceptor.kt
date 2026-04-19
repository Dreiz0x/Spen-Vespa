package dev.vskelk.cdf.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor - Inyección de API Key en headers
 *
 * Per spec: "AuthInterceptor – inyecta x-api-key en cada request"
 */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    companion object {
        const val HEADER_API_KEY = "x-api-key"
    }

    private var apiKey: String? = null

    /**
     * Actualiza la API key activa
     */
    fun setApiKey(key: String?) {
        apiKey = key
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val apiKeyValue = apiKey

        return if (apiKeyValue != null) {
            val newRequest = originalRequest.newBuilder()
                .header(HEADER_API_KEY, apiKeyValue)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
