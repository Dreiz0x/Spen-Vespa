package dev.vskelk.cdf.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs para comunicación con Anthropic Claude API
 */

@Serializable
data class AnthropicRequest(
    @SerialName("model")
    val model: String = "claude-sonnet-4-20250514",

    @SerialName("max_tokens")
    val maxTokens: Int = 4096,

    @SerialName("messages")
    val messages: List<MessageDto>
)

@Serializable
data class MessageDto(
    @SerialName("role")
    val role: String,

    @SerialName("content")
    val content: String
)

@Serializable
data class AnthropicResponse(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("role")
    val role: String,

    @SerialName("content")
    val content: List<ContentBlock>,

    @SerialName("model")
    val model: String,

    @SerialName("stop_reason")
    val stopReason: String? = null,

    @SerialName("stop_sequence")
    val stopSequence: String? = null,

    @SerialName("usage")
    val usage: UsageDto? = null
)

@Serializable
sealed class ContentBlock {
    @Serializable
    @SerialName("text")
    data class TextBlock(
        @SerialName("text")
        val text: String
    ) : ContentBlock()
}

@Serializable
data class UsageDto(
    @SerialName("input_tokens")
    val inputTokens: Int,

    @SerialName("output_tokens")
    val outputTokens: Int
)
