package dev.vskelk.cdf.app.di

import android.content.Context
import com.google.protobuf.Parser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vskelk.cdf.core.datastore.CipherService
import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import dev.vskelk.cdf.core.datastore.UserPreferencesSerializer
import dev.vskelk.cdf.core.datastore.proto.UserPreferences
import javax.inject.Singleton

/**
 * DataStoreModule - Módulo de inyección para Proto DataStore
 *
 * Proporciona las dependencias para PreferencesDataSource y
 * el servicio de cifrado.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideCipherService(
        @ApplicationContext context: Context
    ): CipherService = CipherService(context)

    @Provides
    @Singleton
    fun provideUserPreferencesSerializer(): UserPreferencesSerializer =
        UserPreferencesSerializer()

    @Provides
    @Singleton
    fun provideUserPreferencesParser(): Parser<UserPreferences> =
        UserPreferences.parser()

    @Provides
    @Singleton
    fun providePreferencesDataSource(
        @ApplicationContext context: Context,
        cipherService: CipherService,
        parser: Parser<UserPreferences>
    ): PreferencesDataSource = PreferencesDataSource(context, cipherService, parser)
}
