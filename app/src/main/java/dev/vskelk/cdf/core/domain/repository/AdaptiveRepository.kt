package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * AdaptiveRepository - Motor adaptativo y gestión de sesiones
 */
interface AdaptiveRepository {

    /**
     * Obtiene reactivos priorizados según el dominio actual
     */
    suspend fun getPrioritizedReactivos(
        limit: Int,
        modulo: String,
        examArea: String? = null
    ): List<ReactivoUI>

    /**
     * Obtiene un reactivo con opciones
     */
    suspend fun getReactivoWithOptions(reactivoId: Long): ReactivoUI?

    /**
     * Registra una respuesta y actualiza el dominio
     */
    suspend fun recordAnswer(
        sessionId: Long,
        reactivoId: Long,
        selectedOptionId: Long,
        isCorrect: Boolean,
        tiempoRespuestaMs: Long,
        errorType: String?
    )

    /**
     * Inicia una nueva sesión de estudio
     */
    suspend fun startSession(modulo: String, examArea: String?): Long

    /**
     * Completa una sesión de estudio
     */
    suspend fun completeSession(sessionId: Long): SesionResultado

    /**
     * Obtiene el historial de sesiones
     */
    fun observeRecentSessions(limit: Int): Flow<List<StudySessionEntity>>

    /**
     * Obtiene el conteo de sesiones completadas
     */
    fun observeCompletedSessionCount(): Flow<Int>

    /**
     * Obtiene subtemas que necesitan práctica
     */
    suspend fun observeWeakTopics(): Flow<List<SubtemaConDominio>>

    /**
     * Obtiene tipos de error frecuentes
     */
    suspend fun getFrequentErrorTypes(limit: Int): List<Pair<String, Int>>

    /**
     * Obtiene estadísticas generales
     */
    suspend fun getOverallStats(): OverallStats
}

data class OverallStats(
    val totalSesiones: Int,
    val precisionGeneral: Float,
    val subtemasDominados: Int,
    val totalSubtemas: Int,
    val brechasActivas: Int
)
