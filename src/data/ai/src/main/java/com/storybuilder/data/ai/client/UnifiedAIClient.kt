package com.storybuilder.data.ai.client

import com.storybuilder.domain.model.ApiCredentials

/**
 * Unified response from any AI provider
 */
data class UnifiedAIResponse(
    val content: String,
    val tokensUsed: Int? = null,
    val model: String? = null
)

/**
 * Error types for AI provider operations
 */
sealed class AIClientError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidApiKey(provider: String) : AIClientError("Invalid API key for $provider")
    class RateLimited(provider: String, val retryAfter: Long? = null) : 
        AIClientError("Rate limited by $provider" + (retryAfter?.let { ". Retry after ${it}s" } ?: ""))
    class ModelNotAvailable(model: String, provider: String) : 
        AIClientError("Model '$model' not available for $provider")
    class NetworkError(cause: Throwable) : AIClientError("Network error: ${cause.message}", cause)
    class ProviderError(message: String, cause: Throwable? = null) : AIClientError(message, cause)
    class InvalidResponse(message: String) : AIClientError("Invalid response: $message")
}

/**
 * Unified interface for all AI providers
 */
interface UnifiedAIClient {
    /**
     * Generate a story response based on the provided prompt
     */
    suspend fun generateResponse(
        prompt: String,
        credentials: ApiCredentials,
        context: List<String> = emptyList(),
        temperature: Float = 0.8f
    ): Result<UnifiedAIResponse>
    
    /**
     * Test connection to the provider
     */
    suspend fun testConnection(credentials: ApiCredentials): Result<Boolean>
    
    /**
     * Get the provider type
     */
    fun getProviderType(): com.storybuilder.domain.model.ApiProvider
}

/**
 * Factory to get the appropriate client for a provider
 */
class AIClientFactory(
    private val clients: Map<com.storybuilder.domain.model.ApiProvider, UnifiedAIClient>
) {
    fun getClient(provider: com.storybuilder.domain.model.ApiProvider): UnifiedAIClient {
        return clients[provider] 
            ?: throw IllegalArgumentException("No client found for provider: $provider")
    }
    
    fun getAllClients(): Map<com.storybuilder.domain.model.ApiProvider, UnifiedAIClient> = clients
}
