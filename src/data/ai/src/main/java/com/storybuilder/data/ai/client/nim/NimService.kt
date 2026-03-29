package com.storybuilder.data.ai.client.nim

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NimService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: NimRequest
    ): NimResponse
}
