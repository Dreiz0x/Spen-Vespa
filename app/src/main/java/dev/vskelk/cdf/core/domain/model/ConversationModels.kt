package dev.vskelk.cdf.core.domain.model

/**
 * ConversationModels - Modelos para conversaciones
 */

/**
 * Mensaje en una conversación
 */
data class Mensaje(
    val id: Long,
    val role: String,
    val content: String,
    val timestamp: Long
) {
    val isUser: Boolean get() = role == "USER"
    val isAssistant: Boolean get() = role == "ASSISTANT"
}

/**
 * Conversación con mensajes
 */
data class Conversacion(
    val id: Long,
    val titulo: String,
    val contexto: String?,
    val mensajes: List<Mensaje>,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Resultado de una consulta al motor experto
 */
data class ConsultaMotorResult(
    val respuesta: String,
    val fragmentosCitados: List<Long>,
    val nodosRelacionados: List<Long>,
    val necesitaConfirmacion: Boolean = false
)
