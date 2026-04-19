package dev.vskelk.cdf.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import dev.vskelk.cdf.core.network.api.AnthropicApi
import dev.vskelk.cdf.core.network.datasource.*
import dev.vskelk.cdf.core.network.interceptor.AuthInterceptor
import dev.vskelk.cdf.core.network.interceptor.DebugLoggingInterceptorFactory
import dev.vskelk.cdf.core.network.interceptor.RetryBackoffInterceptor
import dev.vskelk.cdf.core.network.resilience.CircuitBreaker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * NetworkModule - Módulo de inyección para red
 *
 * Configura Retrofit con interceptores de resiliencia y auth.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideCircuitBreaker(): CircuitBreaker = CircuitBreaker()

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()

    @Provides
    @Singleton
    fun provideRetryBackoffInterceptor(): RetryBackoffInterceptor = RetryBackoffInterceptor()

    @Provides
    @Singleton
    fun provideDebugLoggingInterceptor(): DebugLoggingInterceptorFactory =
        DebugLoggingInterceptorFactory()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryBackoffInterceptor: RetryBackoffInterceptor,
        debugLoggingInterceptor: DebugLoggingInterceptorFactory
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(retryBackoffInterceptor)
            .addInterceptor(authInterceptor)
            // Solo agregar logging en debug
            // .addInterceptor(debugLoggingInterceptor.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(ANTHROPIC_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAnthropicApi(retrofit: Retrofit): AnthropicApi {
        return retrofit.create(AnthropicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAnthropicRemoteDataSource(
        api: AnthropicApi,
        circuitBreaker: CircuitBreaker
    ): AnthropicRemoteDataSource {
        return AnthropicRemoteDataSource(api, circuitBreaker)
    }

    @Provides
    @Singleton
    fun provideLlmRemoteDataSource(
        anthropic: AnthropicRemoteDataSource,
        preferencesDataSource: PreferencesDataSource
    ): LlmRemoteDataSource {
        // Por defecto devolvemos Anthropic
        // El MultiProviderLlmDataSource puede cambiar esto dinámicamente
        return anthropic
    }

    @Provides
    @Singleton
    fun provideMultiProviderLlmDataSource(
        anthropic: AnthropicRemoteDataSource,
        gemini: GeminiRemoteDataSource,
        openai: OpenAiRemoteDataSource
    ): MultiProviderLlmDataSource {
        return MultiProviderLlmDataSource(anthropic, gemini, openai)
    }
}
