package com.storybuilder.domain.model

/**
 * Supported AI API providers
 */
enum class ApiProvider {
    OPENAI,
    ANTHROPIC,
    GOOGLE,
    OPENROUTER,
    NIM;

    companion object {
        fun fromString(value: String): ApiProvider? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Available models for each provider
 */
object ProviderModels {
    
    val OPENAI_MODELS = listOf(
        ProviderModel("gpt-4o", "GPT-4o", "Most capable multimodal model"),
        ProviderModel("gpt-4o-mini", "GPT-4o Mini", "Fast and affordable"),
        ProviderModel("gpt-4-turbo", "GPT-4 Turbo", "High capability, lower cost")
    )
    
    val ANTHROPIC_MODELS = listOf(
        ProviderModel("claude-3-5-sonnet-latest", "Claude 3.5 Sonnet", "Best balance of intelligence and speed"),
        ProviderModel("claude-3-opus-latest", "Claude 3 Opus", "Highest capability for complex tasks"),
        ProviderModel("claude-3-haiku-latest", "Claude 3 Haiku", "Fastest responses")
    )
    
    val GOOGLE_MODELS = listOf(
        ProviderModel("gemini-1.5-pro-latest", "Gemini 1.5 Pro", "Most capable Google model"),
        ProviderModel("gemini-1.5-flash-latest", "Gemini 1.5 Flash", "Fast and efficient")
    )
    
    val OPENROUTER_MODELS = listOf(
        ProviderModel("google/gemini-2.0-flash-lite-preview-02-05:free", "Gemini 2.0 Flash Lite (Free)", "Free experimental Gemini model"),
        ProviderModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (Free)", "Free Meta model"),
        ProviderModel("qwen/qwen-2-72b-instruct:free", "Qwen 2 72B (Free)", "Free Qwen model"),
        ProviderModel("openai/gpt-4o", "OpenAI GPT-4o", "OpenAI Flagship"),
        ProviderModel("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet", "Anthropic Flagship"),
        ProviderModel("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "Meta Flagship")
    )
    
    val NIM_MODELS = listOf(
        ProviderModel("meta/llama-3.1-8b-instruct", "Llama 3.1 8B", "Meta's lightweight Llama via NVIDIA NIM"),
        ProviderModel("meta/llama-3.3-70b-instruct", "Llama 3.3 70B", "Meta's flagship Llama via NVIDIA"),
        ProviderModel("mistralai/mistral-large", "Mistral Large", "Mistral via NVIDIA NIM"),
        ProviderModel("nvidia/llama-3.1-nemotron-70b-instruct", "Nemotron 70B", "NVIDIA's optimized model")
    )
    
    fun getModelsForProvider(provider: ApiProvider): List<ProviderModel> {
        return when (provider) {
            ApiProvider.OPENAI -> OPENAI_MODELS
            ApiProvider.ANTHROPIC -> ANTHROPIC_MODELS
            ApiProvider.GOOGLE -> GOOGLE_MODELS
            ApiProvider.OPENROUTER -> OPENROUTER_MODELS
            ApiProvider.NIM -> NIM_MODELS
        }
    }
    
    fun getDefaultModel(provider: ApiProvider): String {
        return when (provider) {
            ApiProvider.OPENAI -> "gpt-4o"
            ApiProvider.ANTHROPIC -> "claude-3-5-sonnet-latest"
            ApiProvider.GOOGLE -> "gemini-1.5-pro-latest"
            ApiProvider.OPENROUTER -> "google/gemini-2.0-flash-lite-preview-02-05:free"
            ApiProvider.NIM -> "meta/llama-3.1-8b-instruct"
        }
    }
}

/**
 * Model information for a provider
 */
data class ProviderModel(
    val id: String,
    val displayName: String,
    val description: String
)

/**
 * API credentials for a specific provider
 */
data class ApiCredentials(
    val provider: ApiProvider,
    val apiKey: String,
    val baseUrl: String?, // Optional for custom endpoints
    val modelName: String, // e.g., "gpt-4o", "claude-3-5-sonnet-20241022"
    val isActive: Boolean = false
) {
    companion object {
        fun getDefaultBaseUrl(provider: ApiProvider): String {
            return when (provider) {
                ApiProvider.OPENAI -> "https://api.openai.com/v1/"
                ApiProvider.ANTHROPIC -> "https://api.anthropic.com/v1/"
                ApiProvider.GOOGLE -> "https://generativelanguage.googleapis.com/"
                ApiProvider.OPENROUTER -> "https://openrouter.ai/api/v1/"
                ApiProvider.NIM -> "https://integrate.api.nvidia.com/v1/"
            }
        }
    }
}

/**
 * Complete provider configuration for all providers
 */
data class ProviderConfigurations(
    val providers: Map<ApiProvider, ApiCredentials>,
    val activeProvider: ApiProvider
) {
    fun getActiveCredentials(): ApiCredentials? {
        return providers[activeProvider]
    }
    
    fun isConfigured(provider: ApiProvider): Boolean {
        val credentials = providers[provider]
        return credentials != null && credentials.apiKey.isNotBlank()
    }
    
    fun hasAnyConfiguredProvider(): Boolean {
        return providers.values.any { it.apiKey.isNotBlank() }
    }
    
    companion object {
        fun empty(): ProviderConfigurations {
            return ProviderConfigurations(
                providers = emptyMap(),
                activeProvider = ApiProvider.GOOGLE // Default fallback
            )
        }
    }
}
