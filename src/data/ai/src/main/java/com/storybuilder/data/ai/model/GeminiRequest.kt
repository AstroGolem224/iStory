package com.storybuilder.data.ai.model

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>,
    @SerializedName("role")
    val role: String = "user"
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Float = 0.8f,
    @SerializedName("topK")
    val topK: Int = 40,
    @SerializedName("topP")
    val topP: Float = 0.95f,
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 1024,
    @SerializedName("responseMimeType")
    val responseMimeType: String = "application/json"
)
