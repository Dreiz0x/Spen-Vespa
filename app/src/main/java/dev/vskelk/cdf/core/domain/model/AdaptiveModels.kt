package dev.vskelk.cdf.core.domain.model

import dev.vskelk.cdf.core.database.entity.*

/**
 * AdaptiveModels - Modelos para el motor adaptativo
 */

/**
 * Reactivo para UI con opciones
 */
data class ReactivoUI(
    val id: Long,
    val enunciado: String,
    val tipoReactivo: String,
    val nivelCognitivo: String,
    val dificultad: Float,
    val opciones: List<OpcionUI>,
    val citaTextual: String?,
    val casoTexto: String?,
    val fundamentes: List<String> = emptyList()
)

data class OpcionUI(
    val id: Long,
    val texto: String,
    val isCorrect: Boolean,
    val explicacion: String?,
    val distractorTipo: String?
)

/**
 * Resultado de evaluar respuesta
 */
data class RespuestaEvaluada(
    val isCorrect: Boolean,
    val opcionSeleccionada: OpcionUI,
    val explicacion: String?,
    val tipoError: String?,
    val fundamentes: List<FundamentoUI>,
    val siguienteReactivo: ReactivoUI?
)

data class FundamentoUI(
    val fuente: String,
    val articleRef: String?,
    val contenido: String,
    val citation: String?
)

/**
 * Sesión de estudio activa
 */
data class SesionEstudio(
    val id: Long,
    val modulo: String,
    val examArea: String?,
    val startedAt: Long,
    val totalReactivos: Int,
    val currentIndex: Int,
    val correctos: Int,
    val reactivos: List<ReactivoUI>,
    val estado: SesionEstado
)

enum class SesionEstado {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

/**
 * Resultado de sesión
 */
data class SesionResultado(
    val sessionId: Long,
    val totalReactivos: Int,
    val correctos: Int,
    val incorrectos: Int,
    val precision: Float,
    val tiempoPromedioSeg: Float,
    val subtemasDebiles: List<Long>,
    val tiposErrorFrecuentes: List<String>,
    val mensaje: String
)
