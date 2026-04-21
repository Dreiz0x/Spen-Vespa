package dev.vskelk.cdf.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * NormativeFragmentEntity - Fragmentos normativos canónicos
 *
 * Almacena el conocimiento normativo validado por el usuario.
 *
 * Per spec: La IA nunca escribe conocimiento canónico directamente.
 * Solo extrae, estructura y propone. El usuario valida.
 *
 * REGLAS DE FUENTES:
 * - LEGIPE
 * - Reglamento de Elecciones INE
 * - Acuerdos del Consejo General INE
 * - TEPJF
 * - Reglamento Interior INE
 * - Institución académica acreditada
 *
 * NADA de Wikipedia, blogs, fuentes no oficiales.
 */
@Entity(
    tableName = "normative_fragments",
    indices = [
        Index(value = ["source"]),
        Index(value = ["articleRef"]),
        Index(value = ["status"]),
        Index(value = ["areaExamen"])
    ]
)
data class NormativeFragmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Contenido textual del fragmento */
    val content: String,

    /** Fuente oficial del fragmento */
    val source: String, // LEGIPE, REGLAMENTO_INE, ACUERDO_CG, TEPJF, etc.

    /** Referencia al artículo específico */
    val articleRef: String? = null, // "Art. 256", "Art. 15", etc.

    /** Tipo de fuente */
    val sourceType: String, // CONSTITUCION, LEY, REGLAMENTO, ACUERDO, SENTENCIA

    /** Nivel de certeza de la extracción */
    val certainty: String, // ALTA, MEDIA, BAJA

    /** Área de examen a la que pertenece */
    val areaExamen: String, // TECNICO, SISTEMA, GENERAL

    /** Nodos ontológicos relacionados (JSON array de IDs) */
    val relatedNodesJson: String? = null,

    /** Versión del fragmento para versionado normativo */
    val versionId: Long = 1,

    /** Inicio de vigencia (timestamp) */
    val vigenciaDesde: Long,

    /** Fin de vigencia (null = vigente actualmente) */
    val vigenciaHasta: Long? = null,

    /** Estado: VIGENTE, DEROGADO, MODIFICADO */
    val status: String = "VIGENTE",

    /** Si fue modificado, FK al fragmento que lo reemplaza */
    val reemplazadoPorId: Long? = null,

    /** Razón de invalidación (cuando fue derogado/modificado) */
    val invalidationReason: String? = null,

    /** Confianza acumulada (conteo de validaciones) */
    val confidenceCount: Int = 1,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp de última actualización */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * DocumentSourceEntity - Fuentes documentales registradas
 *
 * Mantiene registro de todas las fuentes normativas utilizadas.
 */
@Entity(
    tableName = "document_sources",
    indices = [Index(value = ["abreviatura"], unique = true)]
)
data class DocumentSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreCompleto: String,
    val abreviatura: String, // LEGIPE, REGLAMENTO, etc.
    val tipo: String, // CONSTITUCION, LEY, REGLAMENTO, ACUERDO, SENTENCIA
    val autoridadEmisora: String? = null,
    val fechaPublicacion: Long? = null,
    val urlOficial: String? = null,
    val isOficial: Boolean = true, // false = fuente no oficial = certeza BAJA
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Cross-reference: Reactivos a Fragmentos Normativos
 */
@Entity(
    tableName = "reactivo_fragment_cross_ref",
    primaryKeys = ["reactivoId", "fragmentId"],
    indices = [Index(value = ["reactivoId"]), Index(value = ["fragmentId"])]
)
data class ReactivoFragmentCrossRef(
    val reactivoId: Long,
    val fragmentId: Long,
    val isPrimary: Boolean = true // El fragmento principal vs referencias adicionales
)

/**
 * Constantes de fuentes normativas
 */
object NormativeSources {
    const val LEGIPE = "LEGIPE"
    const val REGLAMENTO_INE = "REGLAMENTO_INE"
    const val ACUERDO_CG = "ACUERDO_CG"
    const val TEPJF = "TEPJF"
    const val REGLAMENTO_INTERIOR = "REGLAMENTO_INTERIOR"
    const val CONSTITUCION = "CONSTITUCION"
    const val OPLE = "OPLE"
}

/**
 * Constantes de certeza de extracción
 */
object ExtractionCertainty {
    const val ALTA = "ALTA"
    const val MEDIA = "MEDIA"
    const val BAJA = "BAJA"
}

/**
 * Constantes de estado normativo
 */
object NormativeStatus {
    const val VIGENTE = "VIGENTE"
    const val DEROGADO = "DEROGADO"
    const val MODIFICADO = "MODIFICADO"
}

/**
 * Constantes de tipo de fuente
 */
object SourceType {
    const val CONSTITUCION = "CONSTITUCION"
    const val LEY = "LEY"
    const val REGLAMENTO = "REGLAMENTO"
    const val ACUERDO = "ACUERDO"
    const val SENTENCIA = "SENTENCIA"
    const val CIRCULAR = "CIRCULAR"
}
