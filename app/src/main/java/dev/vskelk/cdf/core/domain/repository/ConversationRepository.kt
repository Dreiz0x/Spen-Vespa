package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * ConversationRepository - Gestión de conversaciones con el motor experto
 */
interface ConversationRepository {

    /**
     * Obtiene todas las conversaciones activas
     */
    fun observeConversations(): Flow<List<Conversacion>>

    /**
     * Obtiene una conversación con sus mensajes
     */
    suspend fun getConversationWithMessages(conversationId: Long): Conversacion?

    /**
     * Crea una nueva conversación
     */
    suspend fun createConversation(titulo: String, contexto: String?): Long

    /**
     * Envía un mensaje y obtiene respuesta
     */
    suspend fun sendMessage(
        conversationId: Long,
        content: String
    ): ConsultaMotorResult

    /**
     * Archiva una conversación
     */
    suspend fun archiveConversation(conversationId: Long)

    /**
     * Elimina una conversación
     */
    suspend fun deleteConversation(conversationId: Long)

    /**
     * Obtiene conversaciones recientes
     */
    fun observeRecentConversations(limit: Int): Flow<List<Conversacion>>
}
