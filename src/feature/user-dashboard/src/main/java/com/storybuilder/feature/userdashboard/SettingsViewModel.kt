package com.storybuilder.feature.userdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.data.local.preferences.SettingsDataStore
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.ProviderModels
import com.storybuilder.domain.model.VoiceProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val apiKeyMasked: String = "",
    val defaultSuggestOptions: Boolean = true,
    val ttsEnabled: Boolean = true,
    val ttsMuted: Boolean = false,
    val selectedVoiceProfile: String = "default",
    val aiTemperature: Float = 0.7f,
    val darkMode: String = "system",
    val genreThemeEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val showClearDataConfirmation: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    // Multi-provider fields
    val activeProvider: ApiProvider = ApiProvider.GOOGLE,
    val activeProviderName: String = "Google (Gemini)",
    val activeModelName: String = "Gemini 1.5 Pro",
    val configuredProviders: List<ApiProvider> = emptyList(),
    val showLegacyApiKey: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.getApiKey(),
                settingsDataStore.getDefaultSuggestOptions(),
                settingsDataStore.getTtsEnabled(),
                settingsDataStore.getTtsMuted(),
                settingsDataStore.getTtsVoiceProfile(),
                settingsDataStore.getAiTemperature(),
                settingsDataStore.getDarkMode(),
                settingsDataStore.getGenreThemeEnabled(),
                settingsDataStore.getHapticEnabled(),
                settingsDataStore.getActiveProvider(),
                settingsDataStore.getProviderConfigurations()
            ) { values ->
                val apiKey = values[0] as? String ?: ""
                val activeProvider = values[9] as ApiProvider
                val configs = values[10] as com.storybuilder.domain.model.ProviderConfigurations
                
                val activeCredentials = configs.getActiveCredentials()
                val activeModel = activeCredentials?.modelName ?: ProviderModels.getDefaultModel(activeProvider)
                val modelDisplayName = ProviderModels.getModelsForProvider(activeProvider)
                    .find { it.id == activeModel }?.displayName ?: activeModel
                
                @Suppress("UNCHECKED_CAST")
                SettingsUiState(
                    apiKey = apiKey,
                    apiKeyMasked = maskApiKey(apiKey),
                    defaultSuggestOptions = values[1] as Boolean,
                    ttsEnabled = values[2] as Boolean,
                    ttsMuted = values[3] as Boolean,
                    selectedVoiceProfile = values[4] as String,
                    aiTemperature = ((values[5] as? Double) ?: 0.7).toFloat(),
                    darkMode = values[6] as String,
                    genreThemeEnabled = values[7] as Boolean,
                    hapticEnabled = values[8] as Boolean,
                    activeProvider = activeProvider,
                    activeProviderName = getProviderDisplayName(activeProvider),
                    activeModelName = modelDisplayName,
                    configuredProviders = configs.providers.keys.toList(),
                    showLegacyApiKey = configs.providers.isEmpty() && apiKey.isNotBlank()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}...${apiKey.takeLast(4)}"
        } else if (apiKey.isNotEmpty()) {
            "****"
        } else {
            "Not set"
        }
    }

    fun updateApiKey(newApiKey: String) {
        viewModelScope.launch {
            settingsDataStore.saveApiKey(newApiKey)
            _uiState.value = _uiState.value.copy(
                apiKey = newApiKey,
                apiKeyMasked = maskApiKey(newApiKey)
            )
        }
    }

    fun setDefaultSuggestOptions(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDefaultSuggestOptions(enabled)
            _uiState.value = _uiState.value.copy(defaultSuggestOptions = enabled)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setTtsEnabled(enabled)
            _uiState.value = _uiState.value.copy(ttsEnabled = enabled)
        }
    }

    fun setTtsMuted(muted: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setTtsMuted(muted)
            _uiState.value = _uiState.value.copy(ttsMuted = muted)
        }
    }

    fun setVoiceProfile(profileId: String) {
        viewModelScope.launch {
            settingsDataStore.setTtsVoiceProfile(profileId)
            _uiState.value = _uiState.value.copy(selectedVoiceProfile = profileId)
        }
    }

    fun setAiTemperature(temperature: Float) {
        viewModelScope.launch {
            settingsDataStore.setAiTemperature(temperature.toDouble())
            _uiState.value = _uiState.value.copy(aiTemperature = temperature)
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(mode)
            _uiState.value = _uiState.value.copy(darkMode = mode)
        }
    }

    fun setGenreThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setGenreThemeEnabled(enabled)
            _uiState.value = _uiState.value.copy(genreThemeEnabled = enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setHapticEnabled(enabled)
            _uiState.value = _uiState.value.copy(hapticEnabled = enabled)
        }
    }

    fun showClearDataConfirmation() {
        _uiState.value = _uiState.value.copy(showClearDataConfirmation = true)
    }

    fun hideClearDataConfirmation() {
        _uiState.value = _uiState.value.copy(showClearDataConfirmation = false)
    }

    fun showApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = true)
    }

    fun hideApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = false)
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.clearAll()
            _uiState.value = SettingsUiState()
            onComplete()
        }
    }

    fun getVoiceProfiles(): List<VoiceProfile> = VoiceProfile.allProfiles()
    
    fun getProviderDisplayName(provider: ApiProvider): String {
        return when (provider) {
            ApiProvider.OPENAI -> "OpenAI"
            ApiProvider.ANTHROPIC -> "Anthropic"
            ApiProvider.GOOGLE -> "Google (Gemini)"
            ApiProvider.OPENROUTER -> "OpenRouter"
            ApiProvider.NIM -> "NVIDIA NIM"
        }
    }

    companion object {
        const val VERSION_NAME = "1.1.0"
    }
}
