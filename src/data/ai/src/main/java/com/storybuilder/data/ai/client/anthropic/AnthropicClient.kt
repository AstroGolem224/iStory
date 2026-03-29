package com.storybuilder.data.ai.client.anthropic

import com.storybuilder.data.ai.client.AIClientError
import com.storybuilder.data.ai.client.UnifiedAIClient
import com.storybuilder.data.ai.client.UnifiedAIResponse
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnthropicClient @Inject constructor(
    private val service: AnthropicService
) : UnifiedAIClient {

    override fun getProviderType(): ApiProvider = ApiProvider.ANTHROPIC

    override suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String>,
        temperature: Float
    ): Result<UnifiedAIResponse> {
        return try {
            // Build system prompt for JSON output
            val systemPrompt = buildString {
                append("You are a creative story writer. Always respond with valid JSON.")
                if (context.isNotEmpty()) {
                    append("\n\nPrevious context:\n")
                    context.forEach { appendLine("- $it") }
                }
            }

            val request = AnthropicRequest(
                model = credentials.modelName,
                maxTokens = 2048,
                temperature = temperature,
                system = systemPrompt,
                messages = listOf(
                    AnthropicMessage(
                        role = "user",
                        content = listOf(
                            AnthropicContent(type = "text", text = prompt)
                        )
                    )
                )
            )

            val response = service.createMessage(
                apiKey = credentials.apiKey,
                request = request
            )

            // Check for API error
            if (response.error != null) {
                return handleAnthropicError(response.error)
            }

            val content = response.content.firstOrNull()?.text
                ?: return Result.failure(AIClientError.InvalidResponse("No content in response"))

            Result.success(UnifiedAIResponse(
                content = content,
                tokensUsed = response.usage?.totalTokens,
                model = response.model
            ))
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: IOException) {
            Result.failure(AIClientError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AIClientError.ProviderError("Anthropic error: ${e.message}", e))
        }
    }

    override suspend fun testConnection(credentials: ApiCredentials): Result<Boolean> {
        return try {
            val request = AnthropicRequest(
                model = credentials.modelName,
                maxTokens = 10,
                temperature = 0.0f,
                messages = listOf(
                    AnthropicMessage(
                        role = "user",
                        content = listOf(
                            AnthropicContent(type = "text", text = "Say 'OK' and nothing else.")
                        )
                    )
                )
            )

            val response = service.createMessage(
                apiKey = credentials.apiKey,
                request = request
            )

            if (response.error != null) {
                return handleAnthropicError(response.error).map { false }
            }

            Result.success(true)
        } catch (e: HttpException) {
            val result: Result<Boolean> = handleHttpException(e)
            result.map { false }
        } catch (e: IOException) {
            Result.failure(AIClientError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AIClientError.ProviderError("Connection test failed: ${e.message}", e))
        }
    }

    private fun handleAnthropicError(error: AnthropicError): Result<Nothing> {
        return when (error.type) {
            "authentication_error" -> Result.failure(
                AIClientError.InvalidApiKey("Anthropic")
            )
            "rate_limit_error" -> Result.failure(
                AIClientError.RateLimited("Anthropic")
            )
            "not_found_error" -> Result.failure(
                AIClientError.ModelNotAvailable(error.message, "Anthropic")
            )
            else -> Result.failure(
                AIClientError.ProviderError("Anthropic: ${error.message}")
            )
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        return when (e.code()) {
            401 -> Result.failure(AIClientError.InvalidApiKey("Anthropic"))
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toLongOrNull()
                Result.failure(AIClientError.RateLimited("Anthropic", retryAfter))
            }
            404 -> Result.failure(AIClientError.ModelNotAvailable("Unknown", "Anthropic"))
            in 500..599 -> Result.failure(
                AIClientError.ProviderError("Anthropic server error: ${e.code()}")
            )
            else -> Result.failure(
                AIClientError.ProviderError("Anthropic HTTP error: ${e.code()}")
            )
        }
    }
}
