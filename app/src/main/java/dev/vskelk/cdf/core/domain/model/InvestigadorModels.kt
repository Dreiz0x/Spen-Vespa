package dev.vskelk.cdf.core.domain.model

import dev.vskelk.cdf.core.database.entity.*

/**
 * InvestigadorModels - Modelos para el Auto-Investigador
 */

/**
 * Resultado de investigación
 */
data class InvestigacionResult(
    val fragmentos: List<FragmentoInvestigado>,
    val conflictos: List<ConflictoDetectado>,
    val fuentesVerificadas: Int,
    val necesitaRevision: Boolean
)

/**
 * Fragmento extraído por la IA
 */
data class FragmentoInvestigado(
    val contenido: String,
    val fuente: String,
    val articleRef: String?,
    val certeza: String,
    val areaExamen: String?,
    val nodoSugerido: String?
)

/**
 * Conflicto entre fragmentos
 */
data class ConflictoDetectado(
    val fragmento1: FragmentoInvestigado,
    val fragmento2: NormativeFragmentEntity,
    val descripcion: String
)

/**
 * Estados del proceso de investigación
 */
sealed class InvestigacionEstado {
    data object Idle : InvestigacionEstado()
    data class Acotando(val mensaje: String) : InvestigacionEstado()
    data class Formulando(val mensaje: String) : InvestigacionEstado()
    data class Consultando(val mensaje: String) : InvestigacionEstado()
    data class Validando(val mensaje: String) : InvestigacionEstado()
    data class Completado(val resultado: InvestigacionResult) : InvestigacionEstado()
    data class Error(val mensaje: String) : InvestigacionEstado()
}
