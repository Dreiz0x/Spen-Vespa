package dev.vskelk.cdf.core.domain.model

import dev.vskelk.cdf.core.database.entity.*
import kotlinx.serialization.Serializable

/**
 * BootstrapModels - Modelos para el sistema de bootstrap
 *
 * Per spec: Seed manifest define conteos mínimos.
 * Los conteos mínimos son 0 intencionalmente - la app arranca limpia.
 */

/**
 * Estado del bootstrap
 */
sealed interface BootstrapState {
    /** Verificando estado inicial */
    data object Checking : BootstrapState

    /** Siembra en progreso */
    data class Seeding(val message: String, val progress: Float = 0f) : BootstrapState

    /** Sistema listo para uso */
    data object Ready : BootstrapState

    /** Error durante bootstrap */
    data class Error(val cause: String, val canRetry: Boolean = true) : BootstrapState
}

/**
 * Manifest del seed
 */
@Serializable
data class SeedManifest(
    val version: String,
    val minReactivos: Int,
    val minNormativa: Int,
    val minOntologia: Int,
    val descripcion: String
)

/**
 * Resultado de la verificación de bootstrap
 */
data class BootstrapCheckResult(
    val needsSeeding: Boolean,
    val currentVersion: String?,
    val manifestVersion: String,
    val counts: ContentCounts
)

/**
 * Conteos de contenido en la base de datos
 */
data class ContentCounts(
    val reactivos: Int,
    val normativa: Int,
    val ontologia: Int
)
