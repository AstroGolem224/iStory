package com.storybuilder.data.ai.client.openai

import com.google.gson.annotations.SerializedName

/**
 * OpenAI API Request Models
 */
data class OpenAIRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<OpenAIMessage>,
    @SerializedName("temperature")
    val temperature: Float = 0.8f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    @SerializedName("response_format")
    val responseFormat: ResponseFormat? = ResponseFormat.jsonSchema()
)

data class OpenAIMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    @SerializedName("content")
    val content: String
)

data class ResponseFormat(
    @SerializedName("type")
    val type: String = "json_object"
) {
    companion object {
        fun jsonSchema(): ResponseFormat = ResponseFormat("json_object")
        fun text(): ResponseFormat = ResponseFormat("text")
    }
}

/**
 * OpenAI API Response Models
 */
data class OpenAIResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<OpenAIChoice>,
    @SerializedName("usage")
    val usage: OpenAIUsage?,
    @SerializedName("error")
    val error: OpenAIError?
)

data class OpenAIChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: OpenAIMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class OpenAIUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

data class OpenAIError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("code")
    val code: String? = null
)
