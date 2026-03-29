package com.storybuilder.data.ai

import com.storybuilder.data.ai.model.GeminiRequest
import com.storybuilder.data.ai.model.GeminiResponse
import com.storybuilder.data.ai.model.Part
import com.storybuilder.data.ai.model.Content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIClient @Inject constructor(
    private val apiService: GeminiApiService
) {
    suspend fun generateStoryResponse(
        prompt: String,
        context: List<String> = emptyList()
    ): Result<GeminiResponse> {
        return try {
            val fullPrompt = buildString {
                if (context.isNotEmpty()) {
                    append("Previous context:\n")
                    context.forEach { appendLine("- $it") }
                    appendLine()
                }
                append(prompt)
            }

            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = fullPrompt)))
                )
            )

            val response = apiService.generateContent(request = request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
