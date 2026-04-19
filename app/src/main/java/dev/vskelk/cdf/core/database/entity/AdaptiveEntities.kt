package dev.vskelk.cdf.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AdaptiveEntities - Motor Adaptativo de Vespa
 *
 * Gestiona el estado de dominio por subtema y el registro histórico
 * de errores para personalizar la experiencia de estudio.
 *
 * Per spec: Cada par (usuario, subtema) tiene un estado calculado
 * dinámicamente basado en su historial de respuestas.
 */

/**
 * UserTopicMasteryEntity - Estado de dominio por subtema
 *
 * Registra el nivel de dominio actual de cada subtema para el usuario.
 */
@Entity(
    tableName = "user_topic_mastery",
    indices = [
        Index(value = ["subtemaId"], unique = true),
        Index(value = ["estadoDominio"]),
        Index(value = ["precision"])
    ]
)
data class UserTopicMasteryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK al subtema ontológico */
    val subtemaId: Long,

    /** Estado actual de dominio */
    val estadoDominio: String, // NO_VISTO, EXPUESTO, INESTABLE, EN_CONSOLIDACION, DOMINADO, DOMINADO_BAJO_PRESION

    /** Precisión calculada (correctas / intentos) */
    val precision: Float = 0f,

    /** Velocidad promedio en segundos */
    val velocidadPromedio: Float = 0f,

    /** Consistencia (1 - desviación estándar) */
    val consistencia: Float = 1f,

    /** Total de intentos en este subtema */
    val totalIntentos: Int = 0,

    /** Total de respuestas correctas */
    val correctas: Int = 0,

    /** Timestamp del último intento */
    val ultimoIntentoAt: Long? = null,

    /** Timestamp de última actualización */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * UserGapLogEntity - Registro histórico de errores
 *
 * Cada error se clasifica para identificar patrones y generar
 * recomendaciones específicas.
 */
@Entity(
    tableName = "user_gap_log",
    indices = [
        Index(value = ["subtemaId"]),
        Index(value = ["errorType"]),
        Index(value = ["sessionId"]),
        Index(value = ["createdAt"])
    ]
)
data class UserGapLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK al subtema donde ocurrió el error */
    val subtemaId: Long,

    /** Tipo de error clasificado */
    val errorType: String,

    /** FK al reactivo que provocó el error */
    val reactivoId: Long,

    /** FK a la sesión donde ocurrió */
    val sessionId: Long,

    /** Timestamp del error */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * StudySessionEntity - Metadatos de sesiones de estudio
 */
@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["modulo"]),
        Index(value = ["examArea"]),
        Index(value = ["startedAt"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Módulo utilizado */
    val modulo: String, // SIMULADOR, DIAGNOSTICO

    /** Área de examen filtrada (null = todas) */
    val examArea: String? = null,

    /** Timestamp de inicio */
    val startedAt: Long,

    /** Timestamp de fin (null = en progreso) */
    val finishedAt: Long? = null,

    /** Total de reactivos en la sesión */
    val totalReactivos: Int = 0,

    /** Respuestas correctas */
    val correctos: Int = 0,

    /** Tiempo promedio por reactivo (segundos) */
    val tiempoPromedioSeg: Float = 0f,

    /** Sub temas débilеs identificados (JSON array) */
    val weakSubtemasJson: String? = null,

    /** Tipos de error dominantes (JSON array) */
    val dominantErrorTypesJson: String? = null,

    /** Estado de la sesión */
    val status: String = "IN_PROGRESS" // IN_PROGRESS, COMPLETED, ABANDONED
)

/**
 * Constantes de estado de dominio
 */
object DomainState {
    /** Sin intentos en este subtema */
    const val NO_VISTO = "NO_VISTO"

    /** Al menos 1 intento, precisión < 40% */
    const val EXPUESTO = "EXPUESTO"

    /** Precisión 40-60%, alta varianza */
    const val INESTABLE = "INESTABLE"

    /** Precisión 60-80%, consistente */
    const val EN_CONSOLIDACION = "EN_CONSOLIDACION"

    /** Precisión > 80%, mínimo 5 intentos */
    const val DOMINADO = "DOMINADO"

    /** Dominado + tiempo < umbral */
    const val DOMINADO_BAJO_PRESION = "DOMINADO_BAJO_PRESION"

    /**
     * Thresholds para cambio de estado
     */
    object Thresholds {
        const val PRECISION_EXPUESTO = 0.4f
        const val PRECISION_INESTABLE_MIN = 0.4f
        const val PRECISION_INESTABLE_MAX = 0.6f
        const val PRECISION_CONSOLIDACION = 0.6f
        const val PRECISION_DOMINADO = 0.8f
        const val MIN_INTENTOS_DOMINADO = 5
        const val TIEMPO_BAJO_PRESION_SEG = 15f
    }

    /**
     * Estados que requieren estudio adicional
     */
    val estadosDebiles = setOf(NO_VISTO, EXPUESTO, INESTABLE)
}

/**
 * Constantes de tipo de error
 */
object ErrorType {
    /** No recuerda el dato o artículo */
    const val ERROR_MEMORIA = "ERROR_MEMORIA"

    /** Confunde a qué órgano corresponde la función */
    const val ERROR_ATRIBUCION = "ERROR_ATRIBUCION"

    /** Confunde el orden de pasos de un procedimiento */
    const val ERROR_SECUENCIA = "ERROR_SECUENCIA"

    /** Confunde fechas o plazos */
    const val ERROR_PLAZO = "ERROR_PLAZO"

    /** Aplica la regla general donde hay excepción */
    const val ERROR_EXCEPCION = "ERROR_EXCEPCION"

    /** Elige opción parecida en wording pero incorrecta */
    const val ERROR_DISTRACTOR_SEMANTICO = "ERROR_DISTRACTOR_SEMANTICO"

    /** Elige incorrecta por no leer completo */
    const val ERROR_LECTURA_RAPIDA = "ERROR_LECTURA_RAPIDA"

    /** Confunde normas de órganos similares */
    const val ERROR_CONFUSION_NORMATIVA = "ERROR_CONFUSION_NORMATIVA"

    /**
     * Mapeo de distractor tipo a tipo de error
     */
    fun fromDistractor(distractorTipo: String): String = when (distractorTipo) {
        "SIMILAR_ORGANO" -> ERROR_ATRIBUCION
        "PLAZO_INCORRECTO" -> ERROR_PLAZO
        "EXCEPCION" -> ERROR_EXCEPCION
        "SEMANTICO" -> ERROR_DISTRACTOR_SEMANTICO
        "NUMERICO" -> ERROR_MEMORIA
        "INVERSION" -> ERROR_LECTURA_RAPIDA
        else -> ERROR_MEMORIA
    }
}

/**
 * Constantes de estado de sesión
 */
object SessionStatus {
    const val IN_PROGRESS = "IN_PROGRESS"
    const val COMPLETED = "COMPLETED"
    const val ABANDONED = "ABANDONED"
}
