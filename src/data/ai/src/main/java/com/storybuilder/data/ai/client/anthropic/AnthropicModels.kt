package com.storybuilder.data.ai.client.anthropic

import com.google.gson.annotations.SerializedName

/**
 * Anthropic API Request Models
 */
data class AnthropicRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    @SerializedName("temperature")
    val temperature: Float = 0.8f,
    @SerializedName("system")
    val system: String? = null,
    @SerializedName("messages")
    val messages: List<AnthropicMessage>
)

data class AnthropicMessage(
    @SerializedName("role")
    val role: String, // "user", "assistant"
    @SerializedName("content")
    val content: List<AnthropicContent>
)

data class AnthropicContent(
    @SerializedName("type")
    val type: String = "text", // "text", "image"
    @SerializedName("text")
    val text: String? = null
)

/**
 * Anthropic API Response Models
 */
data class AnthropicResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("content")
    val content: List<AnthropicContent>,
    @SerializedName("stop_reason")
    val stopReason: String?,
    @SerializedName("usage")
    val usage: AnthropicUsage?,
    @SerializedName("error")
    val error: AnthropicError?
)

data class AnthropicUsage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
) {
    val totalTokens: Int get() = inputTokens + outputTokens
}

data class AnthropicError(
    @SerializedName("type")
    val type: String,
    @SerializedName("message")
    val message: String
)
