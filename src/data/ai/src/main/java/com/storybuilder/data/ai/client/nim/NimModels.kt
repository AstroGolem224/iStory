package com.storybuilder.data.ai.client.nim

import com.google.gson.annotations.SerializedName

/**
 * NVIDIA NIM API Request Models (OpenAI-compatible)
 */
data class NimRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<NimMessage>,
    @SerializedName("temperature")
    val temperature: Float = 0.8f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    @SerializedName("top_p")
    val topP: Float = 1.0f,
    @SerializedName("stream")
    val stream: Boolean = false
)

data class NimMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

/**
 * NVIDIA NIM API Response Models
 */
data class NimResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<NimChoice>,
    @SerializedName("usage")
    val usage: NimUsage?,
    @SerializedName("error")
    val error: NimError?
)

data class NimChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: NimMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class NimUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null
)

data class NimError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("code")
    val code: String? = null
)
