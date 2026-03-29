package com.storybuilder.data.ai.model

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>?
)

data class Candidate(
    @SerializedName("content")
    val content: ContentResponse?,
    @SerializedName("finishReason")
    val finishReason: String?,
    @SerializedName("index")
    val index: Int?
)

data class ContentResponse(
    @SerializedName("parts")
    val parts: List<PartResponse>?,
    @SerializedName("role")
    val role: String?
)

data class PartResponse(
    @SerializedName("text")
    val text: String
)
