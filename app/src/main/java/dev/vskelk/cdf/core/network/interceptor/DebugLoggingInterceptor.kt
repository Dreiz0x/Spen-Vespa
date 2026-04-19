package dev.vskelk.cdf.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DebugLoggingInterceptor - Logging de requests para desarrollo
 *
 * Solo activo en builds de debug.
 */
@Singleton
class DebugLoggingInterceptorFactory @Inject constructor() {

    fun create(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        val startTime = System.nanoTime()

        Log.d(TAG, "┌──────────────────────────────────────────────")
        Log.d(TAG, "│ ${request.method} ${request.url}")
        Log.d(TAG, "│ Headers: ${request.headers}")
        request.body?.let { body ->
            Log.d(TAG, "│ Body: ${body.contentLength()} bytes")
        }
        Log.d(TAG, "├──────────────────────────────────────────────")

        val response = chain.proceed(request)
        val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        Log.d(TAG, "│ Response: ${response.code} in ${durationMs}ms")
        Log.d(TAG, "└──────────────────────────────────────────────")

        response
    }

    companion object {
        private const val TAG = "VespaNetwork"
    }
}
