package com.storybuilder.data.ai

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.storybuilder.data.ai.model.GeminiResponse
import com.storybuilder.data.ai.model.StoryBeatResponse
import com.storybuilder.domain.model.StoryBeat
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseParser @Inject constructor() {

    private val gson = Gson()

    fun parseStoryBeatResponse(
        geminiResponse: GeminiResponse,
        storyId: String,
        sequenceOrder: Int
    ): Result<StoryBeat> {
        return try {
            val rawText = extractJsonText(geminiResponse)
                ?: return Result.failure(IllegalStateException("No text content in response"))

            val jsonText = rawText.replace(Regex("```xml|```json|```\\w*\\s*"), "").replace(Regex("```\\s*$"), "").trim()

            val storyBeatResponse = gson.fromJson(jsonText, StoryBeatResponse::class.java)
                ?: return Result.failure(JsonParseException("Failed to parse JSON response"))

            // Validate response
            if (storyBeatResponse.narratorText.isBlank()) {
                return Result.failure(IllegalStateException("Empty narrator text"))
            }

            // For options mode, validate that we have 3 options
            storyBeatResponse.suggestedOptions?.let { options ->
                if (options.isNotEmpty() && options.size != 3) {
                    return Result.failure(IllegalStateException(
                        "Expected 3 options, got ${options.size}"
                    ))
                }
            }

            val storyBeat = StoryBeat(
                id = UUID.randomUUID().toString(),
                storyId = storyId,
                sequenceOrder = sequenceOrder,
                narratorText = storyBeatResponse.narratorText.trim(),
                suggestedOptions = storyBeatResponse.suggestedOptions?.map { it.trim() },
                selectedOptionIndex = null,
                freeTextInput = null
            )

            Result.success(storyBeat)
        } catch (e: JsonParseException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractJsonText(response: GeminiResponse): String? {
        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
    }
}
