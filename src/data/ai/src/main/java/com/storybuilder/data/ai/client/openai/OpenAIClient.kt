package com.storybuilder.data.ai.client.openai

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
class OpenAIClient @Inject constructor(
    private val service: OpenAIService
) : UnifiedAIClient {

    override fun getProviderType(): ApiProvider = ApiProvider.OPENAI

    override suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String>,
        temperature: Float
    ): Result<UnifiedAIResponse> {
        return try {
            val messages = buildList {
                // Add system prompt for JSON output
                add(OpenAIMessage(
                    role = "system",
                    content = "You are a creative story writer. Always respond with valid JSON."
                ))
                
                // Add context if provided
                context.forEach { ctx ->
                    add(OpenAIMessage(role = "user", content = "Context: $ctx"))
                }
                
                // Add main prompt
                add(OpenAIMessage(role = "user", content = prompt))
            }

            val request = OpenAIRequest(
                model = credentials.modelName,
                messages = messages,
                temperature = temperature,
                responseFormat = ResponseFormat.jsonSchema()
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request)

            // Check for API error
            if (response.error != null) {
                return handleOpenAIError(response.error)
            }

            val choice = response.choices.firstOrNull()
                ?: return Result.failure(AIClientError.InvalidResponse("No choices in response"))

            Result.success(UnifiedAIResponse(
                content = choice.message.content,
                tokensUsed = response.usage?.totalTokens,
                model = response.model
            ))
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: IOException) {
            Result.failure(AIClientError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AIClientError.ProviderError("OpenAI error: ${e.message}", e))
        }
    }

    override suspend fun testConnection(credentials: ApiCredentials): Result<Boolean> {
        return try {
            val request = OpenAIRequest(
                model = credentials.modelName,
                messages = listOf(
                    OpenAIMessage(role = "user", content = "Say 'OK' and nothing else.")
                ),
                temperature = 0.0f,
                maxTokens = 10,
                responseFormat = ResponseFormat.text()
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request)

            if (response.error != null) {
                return handleOpenAIError(response.error).map { false }
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

    private fun handleOpenAIError(error: OpenAIError): Result<Nothing> {
        return when (error.code) {
            "invalid_api_key" -> Result.failure(
                AIClientError.InvalidApiKey("OpenAI")
            )
            "rate_limit_exceeded" -> Result.failure(
                AIClientError.RateLimited("OpenAI")
            )
            "model_not_found" -> Result.failure(
                AIClientError.ModelNotAvailable(error.message, "OpenAI")
            )
            else -> Result.failure(
                AIClientError.ProviderError("OpenAI: ${error.message}")
            )
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        return when (e.code()) {
            401 -> Result.failure(AIClientError.InvalidApiKey("OpenAI"))
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toLongOrNull()
                Result.failure(AIClientError.RateLimited("OpenAI", retryAfter))
            }
            404 -> Result.failure(AIClientError.ModelNotAvailable("Unknown", "OpenAI"))
            in 500..599 -> Result.failure(
                AIClientError.ProviderError("OpenAI server error: ${e.code()}")
            )
            else -> Result.failure(
                AIClientError.ProviderError("OpenAI HTTP error: ${e.code()}")
            )
        }
    }
}
