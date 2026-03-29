package com.storybuilder.data.ai.client.openrouter

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://storybuilder.app",
        @Header("X-Title") title: String = "StoryBuilder",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): OpenRouterModelList
}

data class OpenRouterModelList(
    val data: List<OpenRouterModel>
)
