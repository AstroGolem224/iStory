package com.storybuilder.data.ai.client.gemini

import com.storybuilder.data.ai.GeminiApiService
import com.storybuilder.data.ai.client.AIClientError
import com.storybuilder.data.ai.client.UnifiedAIClient
import com.storybuilder.data.ai.client.UnifiedAIResponse
import com.storybuilder.data.ai.model.Content
import com.storybuilder.data.ai.model.GeminiRequest
import com.storybuilder.data.ai.model.Part
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiUnifiedClient @Inject constructor(
    private val service: GeminiApiService
) : UnifiedAIClient {

    override fun getProviderType(): ApiProvider = ApiProvider.GOOGLE

    override suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String>,
        temperature: Float
    ): Result<UnifiedAIResponse> {
        return try {
            val fullPrompt = buildString {
                append("You are a creative story writer. Always respond with valid JSON.\n\n")
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
                ),
                generationConfig = com.storybuilder.data.ai.model.GenerationConfig(
                    temperature = temperature,
                    responseMimeType = "application/json"
                )
            )

            val response = service.generateContent(
                model = credentials.modelName,
                request = request
            )

            val content = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(AIClientError.InvalidResponse("No content in response"))

            Result.success(UnifiedAIResponse(
                content = content,
                tokensUsed = null, // Gemini response doesn't include token count in the current model
                model = credentials.modelName
            ))
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: IOException) {
            Result.failure(AIClientError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AIClientError.ProviderError("Gemini error: ${e.message}", e))
        }
    }

    override suspend fun testConnection(credentials: ApiCredentials): Result<Boolean> {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = "Say 'OK' and nothing else.")))
                ),
                generationConfig = com.storybuilder.data.ai.model.GenerationConfig(
                    temperature = 0.0f,
                    maxOutputTokens = 10
                )
            )

            service.generateContent(
                model = credentials.modelName,
                request = request
            )

            Result.success(true)
        } catch (e: HttpException) {
            handleHttpException<Boolean>(e).map { false }
        } catch (e: IOException) {
            Result.failure(AIClientError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AIClientError.ProviderError("Connection test failed: ${e.message}", e))
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        return when (e.code()) {
            400 -> {
                val errorBody = e.response()?.errorBody()?.string()
                when {
                    errorBody?.contains("API key not valid") == true ->
                        Result.failure(AIClientError.InvalidApiKey("Google"))
                    else -> Result.failure(AIClientError.ProviderError("Google API error: $errorBody"))
                }
            }
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toLongOrNull()
                Result.failure(AIClientError.RateLimited("Google", retryAfter))
            }
            404 -> Result.failure(AIClientError.ModelNotAvailable("Unknown", "Google"))
            in 500..599 -> Result.failure(
                AIClientError.ProviderError("Google server error: ${e.code()}")
            )
            else -> Result.failure(
                AIClientError.ProviderError("Google HTTP error: ${e.code()}")
            )
        }
    }
}
