package com.storybuilder.data.ai

import com.storybuilder.data.ai.model.GeminiRequest
import com.storybuilder.data.ai.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-1.5-pro",
        @Body request: GeminiRequest
    ): GeminiResponse
}
