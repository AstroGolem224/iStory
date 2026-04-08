package com.storybuilder.data.ai.client.openrouter

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
class OpenRouterClient @Inject constructor(
    private val service: OpenRouterService
) : UnifiedAIClient {

    override fun getProviderType(): ApiProvider = ApiProvider.OPENROUTER

    override suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String>,
        temperature: Float
    ): Result<UnifiedAIResponse> {
        return try {
            val messages = buildList {
                add(OpenRouterMessage(
                    role = "system",
                    content = "You are a creative story writer. Always respond with valid JSON."
                ))
                
                context.forEach { ctx ->
                    add(OpenRouterMessage(role = "user", content = "Context: $ctx"))
                }
                
                add(OpenRouterMessage(role = "user", content = prompt))
            }

            val request = OpenRouterRequest(
                model = credentials.modelName,
                messages = messages,
                temperature = temperature,
                responseFormat = OpenRouterResponseFormat.jsonSchema()
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request = request)

            if (response.error != null) {
                return handleOpenRouterError(response.error)
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
            Result.failure(AIClientError.ProviderError("OpenRouter error: ${e.message}", e))
        }
    }

    override suspend fun testConnection(credentials: ApiCredentials): Result<Boolean> {
        return try {
            val request = OpenRouterRequest(
                model = credentials.modelName,
                messages = listOf(
                    OpenRouterMessage(role = "user", content = "Say 'OK' and nothing else.")
                ),
                temperature = 0.7f,
                maxTokens = 10
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request = request)

            if (response.error != null) {
                return handleOpenRouterError(response.error).map { false }
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

    private fun handleOpenRouterError(error: OpenRouterError): Result<Nothing> {
        return when (error.code) {
            401 -> Result.failure(AIClientError.InvalidApiKey("OpenRouter"))
            429 -> Result.failure(AIClientError.RateLimited("OpenRouter"))
            404 -> Result.failure(AIClientError.ModelNotAvailable(error.message, "OpenRouter"))
            else -> Result.failure(
                AIClientError.ProviderError("OpenRouter: ${error.message}")
            )
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        return when (e.code()) {
            401 -> Result.failure(AIClientError.InvalidApiKey("OpenRouter"))
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toLongOrNull()
                Result.failure(AIClientError.RateLimited("OpenRouter", retryAfter))
            }
            404 -> Result.failure(AIClientError.ModelNotAvailable("Unknown", "OpenRouter"))
            in 500..599 -> Result.failure(
                AIClientError.ProviderError("OpenRouter server error: ${e.code()}")
            )
            else -> Result.failure(
                AIClientError.ProviderError("OpenRouter HTTP error: ${e.code()}")
            )
        }
    }
}
