package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.BootstrapState
import kotlinx.coroutines.flow.Flow

/**
 * BootstrapRepository - Gestión del bootstrap de la aplicación
 */
interface BootstrapRepository {

    /**
     * Flow del estado actual de bootstrap
     */
    val bootstrapState: Flow<BootstrapState>

    /**
     * Inicializa la aplicación (bootstrap)
     */
    suspend fun initialize()

    /**
     * Verifica si necesita seeding
     */
    suspend fun needsSeeding(): Boolean

    /**
     * Obtiene la versión del seed aplicado
     */
    suspend fun getSeedVersion(): String?

    /**
     * Obtiene el contenido del manifest
     */
    suspend fun getManifest(): String
}
