package com.storybuilder.data.ai.client.nim

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
class NimClient @Inject constructor(
    private val service: NimService
) : UnifiedAIClient {

    override fun getProviderType(): ApiProvider = ApiProvider.NIM

    override suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String>,
        temperature: Float
    ): Result<UnifiedAIResponse> {
        return try {
            val messages = buildList {
                add(NimMessage(
                    role = "system",
                    content = "You are a creative story writer. Always respond with valid JSON."
                ))
                
                context.forEach { ctx ->
                    add(NimMessage(role = "user", content = "Context: $ctx"))
                }
                
                add(NimMessage(role = "user", content = prompt))
            }

            val request = NimRequest(
                model = credentials.modelName,
                messages = messages,
                temperature = temperature,
                maxTokens = 2048,
                stream = false
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request)

            if (response.error != null) {
                return handleNimError(response.error)
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
            Result.failure(AIClientError.ProviderError("NVIDIA NIM error: ${e.message}", e))
        }
    }

    override suspend fun testConnection(credentials: ApiCredentials): Result<Boolean> {
        return try {
            val request = NimRequest(
                model = credentials.modelName,
                messages = listOf(
                    NimMessage(role = "user", content = "Say 'OK' and nothing else.")
                ),
                temperature = 0.0f,
                maxTokens = 10,
                stream = false
            )

            val authHeader = "Bearer ${credentials.apiKey}"
            val response = service.createChatCompletion(authHeader, request)

            if (response.error != null) {
                return handleNimError(response.error).map { false }
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

    private fun handleNimError(error: NimError): Result<Nothing> {
        return when {
            error.code?.contains("invalid_api_key", ignoreCase = true) == true ->
                Result.failure(AIClientError.InvalidApiKey("NVIDIA NIM"))
            error.code?.contains("rate_limit", ignoreCase = true) == true ->
                Result.failure(AIClientError.RateLimited("NVIDIA NIM"))
            error.code?.contains("model_not_found", ignoreCase = true) == true ->
                Result.failure(AIClientError.ModelNotAvailable(error.message, "NVIDIA NIM"))
            else -> Result.failure(
                AIClientError.ProviderError("NVIDIA NIM: ${error.message}")
            )
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        return when (e.code()) {
            401 -> Result.failure(AIClientError.InvalidApiKey("NVIDIA NIM"))
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toLongOrNull()
                Result.failure(AIClientError.RateLimited("NVIDIA NIM", retryAfter))
            }
            404 -> Result.failure(AIClientError.ModelNotAvailable("Unknown", "NVIDIA NIM"))
            in 500..599 -> Result.failure(
                AIClientError.ProviderError("NVIDIA NIM server error: ${e.code()}")
            )
            else -> Result.failure(
                AIClientError.ProviderError("NVIDIA NIM HTTP error: ${e.code()}")
            )
        }
    }
}
