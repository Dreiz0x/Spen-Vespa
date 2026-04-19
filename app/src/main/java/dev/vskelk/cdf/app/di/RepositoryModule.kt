package dev.vskelk.cdf.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.vskelk.cdf.core.data.repository.BootstrapRepositoryImpl
import dev.vskelk.cdf.core.data.repository.OntologyRepositoryImpl
import dev.vskelk.cdf.core.data.repository.AdaptiveRepositoryImpl
import dev.vskelk.cdf.core.domain.repository.BootstrapRepository
import dev.vskelk.cdf.core.domain.repository.OntologyRepository
import dev.vskelk.cdf.core.domain.repository.AdaptiveRepository
import javax.inject.Singleton

/**
 * RepositoryModule - Módulo de inyección para repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBootstrapRepository(
        impl: BootstrapRepositoryImpl
    ): BootstrapRepository

    @Binds
    @Singleton
    abstract fun bindOntologyRepository(
        impl: OntologyRepositoryImpl
    ): OntologyRepository

    @Binds
    @Singleton
    abstract fun bindAdaptiveRepository(
        impl: AdaptiveRepositoryImpl
    ): AdaptiveRepository
}
