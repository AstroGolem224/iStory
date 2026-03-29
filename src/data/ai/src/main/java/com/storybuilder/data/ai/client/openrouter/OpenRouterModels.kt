package com.storybuilder.data.ai.client.openrouter

import com.google.gson.annotations.SerializedName

/**
 * OpenRouter API Request Models (OpenAI-compatible)
 */
data class OpenRouterRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<OpenRouterMessage>,
    @SerializedName("temperature")
    val temperature: Float = 0.8f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    @SerializedName("response_format")
    val responseFormat: OpenRouterResponseFormat? = OpenRouterResponseFormat.jsonSchema()
)

data class OpenRouterMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class OpenRouterResponseFormat(
    @SerializedName("type")
    val type: String = "json_object"
) {
    companion object {
        fun jsonSchema(): OpenRouterResponseFormat = OpenRouterResponseFormat("json_object")
        fun text(): OpenRouterResponseFormat = OpenRouterResponseFormat("text")
    }
}

/**
 * OpenRouter API Response Models
 */
data class OpenRouterResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<OpenRouterChoice>,
    @SerializedName("usage")
    val usage: OpenRouterUsage?,
    @SerializedName("error")
    val error: OpenRouterError?
)

data class OpenRouterChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: OpenRouterMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class OpenRouterUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

data class OpenRouterError(
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * OpenRouter model listing
 */
data class OpenRouterModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null
)
