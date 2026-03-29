package com.storybuilder.app.screens.apiprovider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.app.data.storage.SecureApiKeyStorage
import com.storybuilder.data.ai.di.ApiKeyProvider
import com.storybuilder.data.local.preferences.SettingsDataStore
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.ProviderConfigurations
import com.storybuilder.domain.model.ProviderModels
import com.storybuilder.domain.repository.ProviderConfigRepository
import com.storybuilder.domain.repository.StoryAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderUiState(
    val provider: ApiProvider,
    val apiKey: String = "",
    val modelName: String = "",
    val baseUrl: String? = null,
    val isConfigured: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val availableModels: List<com.storybuilder.domain.model.ProviderModel> = emptyList()
)

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}

data class ApiProviderSetupUiState(
    val providers: Map<ApiProvider, ProviderUiState> = emptyMap(),
    val activeProvider: ApiProvider = ApiProvider.GOOGLE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canProceed: Boolean = false,
    val expandedProvider: ApiProvider? = null
)

@HiltViewModel
class ApiProviderSetupViewModel @Inject constructor(
    private val secureApiKeyStorage: SecureApiKeyStorage,
    private val settingsDataStore: SettingsDataStore,
    private val providerConfigRepository: ProviderConfigRepository,
    private val storyAIRepository: StoryAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiProviderSetupUiState())
    val uiState: StateFlow<ApiProviderSetupUiState> = _uiState.asStateFlow()

    init {
        loadConfigurations()
    }

    private fun loadConfigurations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val configs = providerConfigRepository.getProviderConfigurations().first()
                val activeProvider = providerConfigRepository.getActiveProvider().first()
                
                val providerStates = ApiProvider.entries.associate { provider ->
                    val credentials = configs.providers[provider]
                    val hasCredentials = credentials != null && credentials.apiKey.isNotBlank()
                    
                    provider to ProviderUiState(
                        provider = provider,
                        apiKey = credentials?.apiKey ?: "",
                        modelName = credentials?.modelName ?: ProviderModels.getDefaultModel(provider),
                        baseUrl = credentials?.baseUrl,
                        isConfigured = hasCredentials,
                        availableModels = ProviderModels.getModelsForProvider(provider)
                    )
                }
                
                val hasAnyConfigured = providerStates.values.any { it.isConfigured }
                
                _uiState.value = ApiProviderSetupUiState(
                    providers = providerStates,
                    activeProvider = activeProvider,
                    isLoading = false,
                    canProceed = hasAnyConfigured
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load configurations: ${e.message}"
                )
            }
        }
    }

    fun setApiKey(provider: ApiProvider, apiKey: String) {
        val currentProviders = _uiState.value.providers.toMutableMap()
        val currentState = currentProviders[provider] ?: ProviderUiState(provider)
        
        currentProviders[provider] = currentState.copy(
            apiKey = apiKey,
            isConfigured = apiKey.isNotBlank()
        )
        
        _uiState.value = _uiState.value.copy(
            providers = currentProviders,
            canProceed = currentProviders.values.any { it.apiKey.isNotBlank() }
        )
    }

    fun setModel(provider: ApiProvider, modelName: String) {
        val currentProviders = _uiState.value.providers.toMutableMap()
        val currentState = currentProviders[provider] ?: ProviderUiState(provider)
        
        currentProviders[provider] = currentState.copy(modelName = modelName)
        _uiState.value = _uiState.value.copy(providers = currentProviders)
    }

    fun setBaseUrl(provider: ApiProvider, baseUrl: String) {
        val currentProviders = _uiState.value.providers.toMutableMap()
        val currentState = currentProviders[provider] ?: ProviderUiState(provider)
        
        currentProviders[provider] = currentState.copy(
            baseUrl = baseUrl.takeIf { it.isNotBlank() }
        )
        _uiState.value = _uiState.value.copy(providers = currentProviders)
    }

    fun setActiveProvider(provider: ApiProvider) {
        _uiState.value = _uiState.value.copy(activeProvider = provider)
    }

    fun toggleExpandProvider(provider: ApiProvider) {
        val current = _uiState.value.expandedProvider
        _uiState.value = _uiState.value.copy(
            expandedProvider = if (current == provider) null else provider
        )
    }

    fun testConnection(provider: ApiProvider) {
        viewModelScope.launch {
            val currentProviders = _uiState.value.providers.toMutableMap()
            val currentState = currentProviders[provider] ?: ProviderUiState(provider)
            
            if (currentState.apiKey.isBlank()) {
                currentProviders[provider] = currentState.copy(
                    testResult = TestResult.Error("API key is required")
                )
                _uiState.value = _uiState.value.copy(providers = currentProviders)
                return@launch
            }
            
            currentProviders[provider] = currentState.copy(isTesting = true, testResult = null)
            _uiState.value = _uiState.value.copy(providers = currentProviders)
            
            try {
                // Temporarily save credentials for testing
                val credentials = ApiCredentials(
                    provider = provider,
                    apiKey = currentState.apiKey,
                    baseUrl = currentState.baseUrl,
                    modelName = currentState.modelName,
                    isActive = false
                )
                
                // Save temporarily for the test
                providerConfigRepository.saveApiCredentials(credentials)
                
                // Use repository to test connection
                val result = storyAIRepository.testConnection(provider)
                
                val testResult = result.fold(
                    onSuccess = { 
                        TestResult.Success("Connection successful!") 
                    },
                    onFailure = { error ->
                        TestResult.Error(error.message ?: "Connection failed")
                    }
                )
                
                currentProviders[provider] = currentProviders[provider]!!.copy(
                    isTesting = false,
                    testResult = testResult
                )
                _uiState.value = _uiState.value.copy(providers = currentProviders)
                
            } catch (e: Exception) {
                currentProviders[provider] = currentProviders[provider]!!.copy(
                    isTesting = false,
                    testResult = TestResult.Error("Test failed: ${e.message}")
                )
                _uiState.value = _uiState.value.copy(providers = currentProviders)
            }
        }
    }

    fun saveAll(onSaved: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val state = _uiState.value
                
                // Save all provider configurations
                state.providers.values.forEach { providerState ->
                    if (providerState.apiKey.isNotBlank()) {
                        val credentials = ApiCredentials(
                            provider = providerState.provider,
                            apiKey = providerState.apiKey,
                            baseUrl = providerState.baseUrl,
                            modelName = providerState.modelName,
                            isActive = providerState.provider == state.activeProvider
                        )
                        
                        // Save to secure storage
                        secureApiKeyStorage.saveApiCredentials(credentials)
                        
                        // Save to DataStore via repository
                        providerConfigRepository.saveApiCredentials(credentials)
                    }
                }
                
                // Save active provider
                providerConfigRepository.setActiveProvider(state.activeProvider)
                secureApiKeyStorage.setActiveProvider(state.activeProvider)
                
                // Update legacy API key provider for backward compatibility
                val activeCredentials = state.providers[state.activeProvider]
                activeCredentials?.let {
                    ApiKeyProvider.setApiKey(it.apiKey)
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSaved()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun getProviderDisplayName(provider: ApiProvider): String {
        return when (provider) {
            ApiProvider.OPENAI -> "OpenAI"
            ApiProvider.ANTHROPIC -> "Anthropic"
            ApiProvider.GOOGLE -> "Google (Gemini)"
            ApiProvider.OPENROUTER -> "OpenRouter"
            ApiProvider.NIM -> "NVIDIA NIM"
        }
    }
    
    fun getProviderDescription(provider: ApiProvider): String {
        return when (provider) {
            ApiProvider.OPENAI -> "GPT-4o, GPT-4o Mini"
            ApiProvider.ANTHROPIC -> "Claude 3.5 Sonnet, Claude 3 Opus"
            ApiProvider.GOOGLE -> "Gemini 1.5 Pro, Gemini 1.5 Flash"
            ApiProvider.OPENROUTER -> "Access multiple models through one API"
            ApiProvider.NIM -> "Llama, Mistral via NVIDIA"
        }
    }
}
