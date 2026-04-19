package dev.vskelk.cdf.core.database.dao

import androidx.room.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * StudySessionDao - Gestión de sesiones de estudio
 */
@Dao
interface StudySessionDao {

    @Insert
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    fun observeSessionById(sessionId: Long): Flow<StudySessionEntity?>

    @Query("""
        SELECT * FROM study_sessions
        ORDER BY startedAt DESC
        LIMIT :limit
    """)
    fun getRecentSessions(limit: Int = 10): Flow<List<StudySessionEntity>>

    @Query("""
        SELECT * FROM study_sessions
        WHERE modulo = :modulo
        ORDER BY startedAt DESC
        LIMIT :limit
    """)
    fun getSessionsByModulo(modulo: String, limit: Int = 10): Flow<List<StudySessionEntity>>

    @Query("""
        SELECT * FROM study_sessions
        WHERE status = :status
        ORDER BY startedAt DESC
    """)
    fun getSessionsByStatus(status: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveSession(): StudySessionEntity?

    @Query("""
        SELECT AVG(correctos * 100.0 / totalReactivos) FROM study_sessions
        WHERE status = 'COMPLETED' AND totalReactivos > 0
    """)
    suspend fun getOverallAccuracy(): Float?

    @Query("SELECT COUNT(*) FROM study_sessions WHERE status = 'COMPLETED'")
    suspend fun getCompletedSessionCount(): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE status = 'COMPLETED'")
    fun observeCompletedSessionCount(): Flow<Int>

    @Transaction
    suspend fun completeSession(
        sessionId: Long,
        correctos: Int,
        tiempoPromedioSeg: Float,
        weakSubtemas: List<Long>,
        dominantErrors: List<String>
    ) {
        val session = getSessionById(sessionId) ?: return
        updateSession(
            session.copy(
                status = SessionStatus.COMPLETED,
                finishedAt = System.currentTimeMillis(),
                correctos = correctos,
                tiempoPromedioSeg = tiempoPromedioSeg,
                weakSubtemasJson = weakSubtemas.joinToString(","),
                dominantErrorTypesJson = dominantErrors.joinToString(",")
            )
        )
    }
}

/**
 * CuarentenaDao - Gestión de fragmentos en cuarentena
 */
@Dao
interface CuarentenaDao {

    @Insert
    suspend fun insertFragmento(fragmento: CuarentenaFragmentoEntity): Long

    @Insert
    suspend fun insertFragmentos(fragmentos: List<CuarentenaFragmentoEntity>): List<Long>

    @Update
    suspend fun updateFragmento(fragmento: CuarentenaFragmentoEntity)

    @Delete
    suspend fun deleteFragmento(fragmento: CuarentenaFragmentoEntity)

    @Query("SELECT * FROM cuarentena_fragmentos WHERE id = :id")
    suspend fun getFragmentoById(id: Long): CuarentenaFragmentoEntity?

    @Query("SELECT * FROM cuarentena_fragmentos WHERE id = :id")
    fun observeFragmentoById(id: Long): Flow<CuarentenaFragmentoEntity?>

    @Query("SELECT * FROM cuarentena_fragmentos WHERE estado = :estado ORDER BY creadoEn DESC")
    fun getFragmentosByEstado(estado: String): Flow<List<CuarentenaFragmentoEntity>>

    @Query("SELECT * FROM cuarentena_fragmentos ORDER BY creadoEn DESC")
    fun observeAllFragmentos(): Flow<List<CuarentenaFragmentoEntity>>

    @Query("SELECT * FROM cuarentena_fragmentos WHERE estado = 'PENDIENTE' ORDER BY creadoEn ASC")
    fun observePendientes(): Flow<List<CuarentenaFragmentoEntity>>

    @Query("SELECT * FROM cuarentena_fragmentos WHERE estado = 'CONFLICTO' ORDER BY creadoEn DESC")
    fun observeConflictos(): Flow<List<CuarentenaFragmentoEntity>>

    @Query("SELECT COUNT(*) FROM cuarentena_fragmentos WHERE estado = 'PENDIENTE'")
    fun observePendienteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cuarentena_fragmentos WHERE estado = 'CONFLICTO'")
    fun observeConflictoCount(): Flow<Int>

    @Transaction
    suspend fun approveFragmento(fragmentoId: Long) {
        val fragmento = getFragmentoById(fragmentoId) ?: return
        updateFragmento(
            fragmento.copy(
                estado = CuarentenaEstado.APROBADO,
                revisadoEn = System.currentTimeMillis()
            )
        )
    }

    @Transaction
    suspend fun rejectFragmento(fragmentoId: Long) {
        val fragmento = getFragmentoById(fragmentoId) ?: return
        updateFragmento(
            fragmento.copy(
                estado = CuarentenaEstado.RECHAZADO,
                revisadoEn = System.currentTimeMillis()
            )
        )
    }

    @Query("DELETE FROM cuarentena_fragmentos WHERE estado = 'RECHAZADO'")
    suspend fun deleteRejected()

    @Query("""
        SELECT * FROM cuarentena_fragmentos
        WHERE contenido = :contenido
        AND estado IN ('PENDIENTE', 'APROBADO')
        LIMIT 1
    """)
    suspend fun findDuplicate(contenido: String): CuarentenaFragmentoEntity?
}

/**
 * ConversationDao - Gestión de conversaciones con el motor experto
 */
@Dao
interface ConversationDao {

    @Insert
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun observeActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentConversations(limit: Int = 20): Flow<List<ConversationEntity>>

    // ===== MENSAJES =====

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt")
    suspend fun getMessagesForConversationSync(conversationId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: Long): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: Long): Int
}
