package dev.vskelk.cdf.core.database.dao

import androidx.room.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * UserMasteryDao - Acceso al motor adaptativo
 *
 * Gestiona el estado de dominio por subtema y el registro de errores.
 */
@Dao
interface UserMasteryDao {

    // ===== DOMINIO POR SUBTEMA =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMastery(mastery: UserTopicMasteryEntity): Long

    @Update
    suspend fun updateMastery(mastery: UserTopicMasteryEntity)

    @Query("SELECT * FROM user_topic_mastery WHERE subtemaId = :subtemaId")
    suspend fun getMasteryBySubtema(subtemaId: Long): UserTopicMasteryEntity?

    @Query("SELECT * FROM user_topic_mastery WHERE subtemaId = :subtemaId")
    fun observeMasteryBySubtema(subtemaId: Long): Flow<UserTopicMasteryEntity?>

    @Query("SELECT * FROM user_topic_mastery ORDER BY precision ASC")
    fun observeAllMastery(): Flow<List<UserTopicMasteryEntity>>

    @Query("""
        SELECT * FROM user_topic_mastery
        WHERE estadoDominio IN (:states)
        ORDER BY precision ASC
    """)
    fun getMasteryByStates(states: List<String>): Flow<List<UserTopicMasteryEntity>>

    @Query("""
        SELECT * FROM user_topic_mastery
        WHERE precision < :threshold
        ORDER BY precision ASC
        LIMIT :limit
    """)
    suspend fun getWeakSubtemas(threshold: Float = 0.8f, limit: Int = 10): List<UserTopicMasteryEntity>

    @Query("""
        SELECT * FROM user_topic_mastery
        WHERE estadoDominio = :state
        ORDER BY precision ASC
    """)
    fun getMasteryByState(state: String): Flow<List<UserTopicMasteryEntity>>

    @Query("SELECT COUNT(*) FROM user_topic_mastery")
    suspend fun getMasteryCount(): Int

    @Query("SELECT COUNT(*) FROM user_topic_mastery WHERE estadoDominio = 'DOMINADO'")
    fun observeDominadoCount(): Flow<Int>

    /**
     * Actualiza el estado de dominio después de un intento
     * Este método implementa la lógica de transición de estados
     */
    @Transaction
    suspend fun recordAttemptAndUpdateMastery(
        subtemaId: Long,
        isCorrect: Boolean,
        tiempoRespuestaMs: Long
    ): UserTopicMasteryEntity {
        val current = getMasteryBySubtema(subtemaId)
        val now = System.currentTimeMillis()

        val newTotalIntentos = (current?.totalIntentos ?: 0) + 1
        val newCorrectas = (current?.correctas ?: 0) + if (isCorrect) 1 else 0
        val newPrecision = newCorrectas.toFloat() / newTotalIntentos

        // Calcular nueva velocidad promedio
        val currentVelocidad = current?.velocidadPromedio ?: 0f
        val newVelocidad = if (current == null) {
            tiempoRespuestaMs / 1000f
        } else {
            (currentVelocidad * current.totalIntentos + tiempoRespuestaMs / 1000f) / newTotalIntentos
        }

        // Determinar nuevo estado
        val newEstado = calcularNuevoEstado(
            precision = newPrecision,
            totalIntentos = newTotalIntentos,
            velocidadPromedio = newVelocidad,
            estadoAnterior = current?.estadoDominio ?: DomainState.NO_VISTO
        )

        val updatedMastery = UserTopicMasteryEntity(
            id = current?.id ?: 0,
            subtemaId = subtemaId,
            estadoDominio = newEstado,
            precision = newPrecision,
            velocidadPromedio = newVelocidad,
            consistencia = current?.consistencia ?: 1f, // Simplificado por ahora
            totalIntentos = newTotalIntentos,
            correctas = newCorrectas,
            ultimoIntentoAt = now,
            updatedAt = now
        )

        upsertMastery(updatedMastery)
        return updatedMastery
    }

    private fun calcularNuevoEstado(
        precision: Float,
        totalIntentos: Int,
        velocidadPromedio: Float,
        estadoAnterior: String
    ): String {
        return when {
            // Orden es importante - verificar el más restrictivo primero
            precision > DomainState.Thresholds.PRECISION_DOMINADO &&
            totalIntentos >= DomainState.Thresholds.MIN_INTENTOS_DOMINADO &&
            velocidadPromedio < DomainState.Thresholds.TIEMPO_BAJO_PRESION_SEG -> DomainState.DOMINADO_BAJO_PRESION

            precision > DomainState.Thresholds.PRECISION_DOMINADO &&
            totalIntentos >= DomainState.Thresholds.MIN_INTENTOS_DOMINADO -> DomainState.DOMINADO

            precision > DomainState.Thresholds.PRECISION_CONSOLIDACION -> DomainState.EN_CONSOLIDACION

            precision >= DomainState.Thresholds.PRECISION_INESTABLE_MIN &&
            precision <= DomainState.Thresholds.PRECISION_INESTABLE_MAX -> DomainState.INESTABLE

            precision < DomainState.Thresholds.PRECISION_EXPUESTO -> DomainState.EXPUESTO

            else -> if (totalIntentos > 0) DomainState.EXPUESTO else DomainState.NO_VISTO
        }
    }

    @Transaction
    suspend fun upsertMastery(mastery: UserTopicMasteryEntity) {
        val existing = getMasteryBySubtema(mastery.subtemaId)
        if (existing != null) {
            updateMastery(mastery.copy(id = existing.id))
        } else {
            insertMastery(mastery)
        }
    }

    // ===== GAP LOG =====

    @Insert
    suspend fun insertGapLog(gapLog: UserGapLogEntity): Long

    @Query("SELECT * FROM user_gap_log WHERE subtemaId = :subtemaId ORDER BY createdAt DESC")
    fun getGapLogsBySubtema(subtemaId: Long): Flow<List<UserGapLogEntity>>

    @Query("""
        SELECT errorType, COUNT(*) as count
        FROM user_gap_log
        WHERE subtemaId = :subtemaId
        GROUP BY errorType
        ORDER BY count DESC
    """)
    suspend fun getErrorTypeCountsBySubtema(subtemaId: Long): List<ErrorTypeCount>

    @Query("""
        SELECT errorType, COUNT(*) as count
        FROM user_gap_log
        GROUP BY errorType
        ORDER BY count DESC
        LIMIT :limit
    """)
    suspend fun getGlobalErrorTypeCounts(limit: Int = 10): List<ErrorTypeCount>

    @Query("SELECT * FROM user_gap_log WHERE sessionId = :sessionId ORDER BY createdAt")
    suspend fun getGapLogsBySession(sessionId: Long): List<UserGapLogEntity>

    @Query("SELECT COUNT(*) FROM user_gap_log")
    suspend fun getTotalGapCount(): Int

    @Query("SELECT COUNT(DISTINCT subtemaId) FROM user_gap_log")
    fun observeAffectedSubtemaCount(): Flow<Int>
}

data class ErrorTypeCount(
    val errorType: String,
    val count: Int
)
