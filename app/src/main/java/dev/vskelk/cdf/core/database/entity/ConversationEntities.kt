package dev.vskelk.cdf.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ConversationEntities - Historial de conversaciones con el motor experto
 *
 * Mantiene el contexto de las conversaciones del usuario con el sistema
 * para generar respuestas contextualizadas.
 */

/**
 * ConversationEntity - Conversación con el motor experto
 */
@Entity(
    tableName = "conversations",
    indices = [Index(value = ["createdAt"])]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Título/resumen de la conversación */
    val titulo: String,

    /** Contexto de la conversación (tema principal) */
    val contexto: String? = null,

    /** Subtema ontológico relacionado (si aplica) */
    val relatedSubtemaId: Long? = null,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp de última actualización */
    val updatedAt: Long = System.currentTimeMillis(),

    /** Si está archivada */
    val isArchived: Boolean = false
)

/**
 * MessageEntity - Mensaje individual en una conversación
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["createdAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK a la conversación padre */
    val conversationId: Long,

    /** Rol del emisor */
    val role: String, // USER, ASSISTANT, SYSTEM

    /** Contenido del mensaje */
    val content: String,

    /** Fragmentos normativos citados en este mensaje (JSON array de IDs) */
    val citedFragmentsJson: String? = null,

    /** Nodos ontológicos relacionados (JSON array de IDs) */
    val relatedNodesJson: String? = null,

    /** Si está pendiente de sincronización */
    val pendingSync: Boolean = false,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * PendingSyncEntity - Mensajes pendientes de sincronización
 *
 * Para el flujo offline-first: los mensajes se guardan localmente
 * y se sincronizan cuando hay conexión.
 */
@Entity(
    tableName = "pending_sync",
    indices = [Index(value = ["messageId"], unique = true)]
)
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK al mensaje pendiente */
    val messageId: Long,

    /** Tipo de operación */
    val operationType: String, // SEND, RETRY

    /** Número de intentos */
    val attempts: Int = 0,

    /** Timestamp del último intento */
    val lastAttemptAt: Long? = null,

    /** Error del último intento (si falló) */
    val lastError: String? = null,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Constantes de rol de mensaje
 */
object MessageRole {
    const val USER = "USER"
    const val ASSISTANT = "ASSISTANT"
    const val SYSTEM = "SYSTEM"
}

/**
 * Constantes de tipo de operación de sync
 */
object SyncOperationType {
    const val SEND = "SEND"
    const val RETRY = "RETRY"
}
