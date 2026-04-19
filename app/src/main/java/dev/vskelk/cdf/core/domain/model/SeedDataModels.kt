package dev.vskelk.cdf.core.domain.model

/**
 * SeedDataModels - Modelos para parsing de archivos JSON de seed
 *
 * Estos modelos representan la estructura exacta de los archivos JSON
 * en assets/seed/ para poder cargarlos en la base de datos Room.
 */

/**
 * Modelo para normativa_sources.json
 */
data class SeedNormativaSource(
    val id: Long,
    val code: String,
    val name: String,
    val type: String,
    val version: String,
    val active_since: Long,
    val status: String
)

/**
 * Modelo para normativa_fragments.json
 */
data class SeedNormativaFragment(
    val id: Long,
    val source_id: Long,
    val version_id: Long,
    val article_ref: String,
    val citation_text: String?,
    val content: String,
    val vigencia_desde: Long,
    val vigencia_hasta: Long?,
    val status: String,
    val replaced_by_id: Long?
)

/**
 * Modelo para ontologia.json
 */
data class SeedOntologiaNode(
    val id: Long,
    val node_type: String,
    val label: String,
    val description: String,
    val confidence: Double,
    val parent_id: Long?,
    val is_active: Boolean
)

/**
 * Modelo para ontologia_edges.json
 */
data class SeedOntologiaEdge(
    val id: Long,
    val source_id: Long,
    val target_id: Long,
    val relation_type: String,
    val weight: Double
)

/**
 * Modelo para reactivos.json
 */
data class SeedReactivo(
    val id: Long,
    val enunciado: String,
    val modulo: String,
    val exam_area: String,
    val tema_id: Long,
    val subtema_id: Long?,
    val tipo_reactivo: String,
    val nivel_cognitivo: String,
    val dificultad: Double,
    val patron_error_id: Long?,
    val fundamento_id: Long?,
    val cita_textual: String?,
    val vigencia_desde: Long,
    val vigencia_hasta: Long?,
    val origen: String,
    val status: String,
    val invalidation_reason: String?,
    val options: List<SeedReactivoOption>
)

/**
 * Modelo para opciones de reactivo
 */
data class SeedReactivoOption(
    val id: Long,
    val texto: String,
    val is_correct: Boolean,
    val explicacion: String?,
    val distractor_tipo: String?
)
