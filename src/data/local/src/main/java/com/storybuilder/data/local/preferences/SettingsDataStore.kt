package com.storybuilder.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.ProviderConfigurations
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore for app settings persistence
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Legacy API Key (for migration)
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        
        // Multi-Provider API Keys
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val GOOGLE_API_KEY = stringPreferencesKey("google_api_key")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val NIM_API_KEY = stringPreferencesKey("nim_api_key")
        
        // Multi-Provider Model Selection
        val OPENAI_MODEL = stringPreferencesKey("openai_model")
        val ANTHROPIC_MODEL = stringPreferencesKey("anthropic_model")
        val GOOGLE_MODEL = stringPreferencesKey("google_model")
        val OPENROUTER_MODEL = stringPreferencesKey("openrouter_model")
        val NIM_MODEL = stringPreferencesKey("nim_model")
        
        // Multi-Provider Base URLs (optional, for custom endpoints)
        val OPENAI_BASE_URL = stringPreferencesKey("openai_base_url")
        val ANTHROPIC_BASE_URL = stringPreferencesKey("anthropic_base_url")
        val GOOGLE_BASE_URL = stringPreferencesKey("google_base_url")
        val OPENROUTER_BASE_URL = stringPreferencesKey("openrouter_base_url")
        val NIM_BASE_URL = stringPreferencesKey("nim_base_url")
        
        // Active Provider
        val ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        
        // Default Mode
        val DEFAULT_SUGGEST_OPTIONS = booleanPreferencesKey("default_suggest_options")
        
        // TTS Settings
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val TTS_VOICE_PROFILE = stringPreferencesKey("tts_voice_profile")
        val TTS_MUTED = booleanPreferencesKey("tts_muted")
        
        // AI Creativity
        val AI_TEMPERATURE = doublePreferencesKey("ai_temperature")
        
        // Chat Theme
        val CHAT_THEME = stringPreferencesKey("chat_theme")
        val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        
        // Genre Theme
        val GENRE_THEME_ENABLED = booleanPreferencesKey("genre_theme_enabled")
        
        // App Info
        val AGE_GATE_CONFIRMED = booleanPreferencesKey("age_gate_confirmed")
        val LAST_ACTIVE_STORY_ID = stringPreferencesKey("last_active_story_id")
        
        // Haptic
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        
        // Default models
        const val DEFAULT_OPENAI_MODEL = "gpt-4o"
        const val DEFAULT_ANTHROPIC_MODEL = "claude-3-5-sonnet-latest"
        const val DEFAULT_GOOGLE_MODEL = "gemini-1.5-pro-latest"
        const val DEFAULT_OPENROUTER_MODEL = "openai/gpt-4o"
        const val DEFAULT_NIM_MODEL = "meta/llama-3.1-70b-instruct"
    }

    // ==================== Multi-Provider API Configuration ====================
    
    suspend fun saveApiCredentials(credentials: ApiCredentials) {
        val (apiKeyKey, modelKey, baseUrlKey) = when (credentials.provider) {
            ApiProvider.OPENAI -> Triple(OPENAI_API_KEY, OPENAI_MODEL, OPENAI_BASE_URL)
            ApiProvider.ANTHROPIC -> Triple(ANTHROPIC_API_KEY, ANTHROPIC_MODEL, ANTHROPIC_BASE_URL)
            ApiProvider.GOOGLE -> Triple(GOOGLE_API_KEY, GOOGLE_MODEL, GOOGLE_BASE_URL)
            ApiProvider.OPENROUTER -> Triple(OPENROUTER_API_KEY, OPENROUTER_MODEL, OPENROUTER_BASE_URL)
            ApiProvider.NIM -> Triple(NIM_API_KEY, NIM_MODEL, NIM_BASE_URL)
        }
        
        context.dataStore.edit { preferences ->
            preferences[apiKeyKey] = credentials.apiKey
            preferences[modelKey] = credentials.modelName
            credentials.baseUrl?.let { preferences[baseUrlKey] = it }
                ?: preferences.remove(baseUrlKey)
        }
    }
    
    suspend fun setActiveProvider(provider: ApiProvider) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_PROVIDER] = provider.name
        }
    }
    
    fun getActiveProvider(): Flow<ApiProvider> = context.dataStore.data.map { preferences ->
        val providerName = preferences[ACTIVE_PROVIDER]
        ApiProvider.fromString(providerName ?: "") ?: ApiProvider.GOOGLE
    }
    
    suspend fun getActiveProviderSync(): ApiProvider {
        return getActiveProvider().first()
    }
    
    fun getProviderConfigurations(): Flow<ProviderConfigurations> {
        return combine(
            // API Keys
            context.dataStore.data.map { it[OPENAI_API_KEY] ?: "" },
            context.dataStore.data.map { it[ANTHROPIC_API_KEY] ?: "" },
            context.dataStore.data.map { it[GOOGLE_API_KEY] ?: "" },
            context.dataStore.data.map { it[OPENROUTER_API_KEY] ?: "" },
            context.dataStore.data.map { it[NIM_API_KEY] ?: "" },
            // Legacy migration
            context.dataStore.data.map { it[GEMINI_API_KEY] ?: "" },
            // Models
            context.dataStore.data.map { it[OPENAI_MODEL] ?: DEFAULT_OPENAI_MODEL },
            context.dataStore.data.map { it[ANTHROPIC_MODEL] ?: DEFAULT_ANTHROPIC_MODEL },
            context.dataStore.data.map { it[GOOGLE_MODEL] ?: DEFAULT_GOOGLE_MODEL },
            context.dataStore.data.map { it[OPENROUTER_MODEL] ?: DEFAULT_OPENROUTER_MODEL },
            context.dataStore.data.map { it[NIM_MODEL] ?: DEFAULT_NIM_MODEL },
            // Base URLs
            context.dataStore.data.map { it[OPENAI_BASE_URL] },
            context.dataStore.data.map { it[ANTHROPIC_BASE_URL] },
            context.dataStore.data.map { it[GOOGLE_BASE_URL] },
            context.dataStore.data.map { it[OPENROUTER_BASE_URL] },
            context.dataStore.data.map { it[NIM_BASE_URL] },
            // Active Provider
            getActiveProvider()
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val providers = mutableMapOf<ApiProvider, ApiCredentials>()
            
            // Handle legacy Google/Gemini API key
            val legacyGoogleKey = values[5] as String
            val googleKey = values[2] as String
            val finalGoogleKey = googleKey.ifBlank { legacyGoogleKey }
            
            // OpenAI
            (values[0] as String).takeIf { it.isNotBlank() }?.let { key ->
                providers[ApiProvider.OPENAI] = ApiCredentials(
                    provider = ApiProvider.OPENAI,
                    apiKey = key,
                    baseUrl = values[10] as? String,
                    modelName = values[5] as String,
                    isActive = false
                )
            }
            
            // Anthropic
            (values[1] as String).takeIf { it.isNotBlank() }?.let { key ->
                providers[ApiProvider.ANTHROPIC] = ApiCredentials(
                    provider = ApiProvider.ANTHROPIC,
                    apiKey = key,
                    baseUrl = values[11] as? String,
                    modelName = values[6] as String,
                    isActive = false
                )
            }
            
            // Google
            finalGoogleKey.takeIf { it.isNotBlank() }?.let { key ->
                providers[ApiProvider.GOOGLE] = ApiCredentials(
                    provider = ApiProvider.GOOGLE,
                    apiKey = key,
                    baseUrl = values[12] as? String,
                    modelName = values[7] as String,
                    isActive = false
                )
            }
            
            // OpenRouter
            (values[3] as String).takeIf { it.isNotBlank() }?.let { key ->
                providers[ApiProvider.OPENROUTER] = ApiCredentials(
                    provider = ApiProvider.OPENROUTER,
                    apiKey = key,
                    baseUrl = values[13] as? String,
                    modelName = values[8] as String,
                    isActive = false
                )
            }
            
            // NIM
            (values[4] as String).takeIf { it.isNotBlank() }?.let { key ->
                providers[ApiProvider.NIM] = ApiCredentials(
                    provider = ApiProvider.NIM,
                    apiKey = key,
                    baseUrl = values[14] as? String,
                    modelName = values[9] as String,
                    isActive = false
                )
            }
            
            val activeProvider = values[15] as ApiProvider
            
            ProviderConfigurations(
                providers = providers,
                activeProvider = activeProvider
            )
        }
    }
    
    suspend fun getProviderConfigurationsSync(): ProviderConfigurations {
        return getProviderConfigurations().first()
    }
    
    suspend fun clearProviderCredentials(provider: ApiProvider) {
        val (apiKeyKey, modelKey, baseUrlKey) = when (provider) {
            ApiProvider.OPENAI -> Triple(OPENAI_API_KEY, OPENAI_MODEL, OPENAI_BASE_URL)
            ApiProvider.ANTHROPIC -> Triple(ANTHROPIC_API_KEY, ANTHROPIC_MODEL, ANTHROPIC_BASE_URL)
            ApiProvider.GOOGLE -> Triple(GOOGLE_API_KEY, GOOGLE_MODEL, GOOGLE_BASE_URL)
            ApiProvider.OPENROUTER -> Triple(OPENROUTER_API_KEY, OPENROUTER_MODEL, OPENROUTER_BASE_URL)
            ApiProvider.NIM -> Triple(NIM_API_KEY, NIM_MODEL, NIM_BASE_URL)
        }
        
        context.dataStore.edit { preferences ->
            preferences.remove(apiKeyKey)
            preferences.remove(modelKey)
            preferences.remove(baseUrlKey)
        }
    }
    
    suspend fun hasAnyConfiguredProvider(): Boolean {
        val configs = getProviderConfigurationsSync()
        return configs.hasAnyConfiguredProvider()
    }

    // ==================== Legacy API Key Methods (for migration) ====================
    
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_API_KEY] = apiKey
            // Also set as active if not set
            if (preferences[ACTIVE_PROVIDER] == null) {
                preferences[ACTIVE_PROVIDER] = ApiProvider.GOOGLE.name
            }
        }
    }

    fun getApiKey(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_API_KEY] ?: preferences[GEMINI_API_KEY]
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(GEMINI_API_KEY)
            preferences.remove(GOOGLE_API_KEY)
        }
    }

    // ==================== Default Suggest Options ====================
    
    suspend fun setDefaultSuggestOptions(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SUGGEST_OPTIONS] = enabled
        }
    }

    fun getDefaultSuggestOptions(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_SUGGEST_OPTIONS] ?: true
    }

    // ==================== TTS Settings ====================
    
    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TTS_ENABLED] = enabled
        }
    }

    fun getTtsEnabled(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TTS_ENABLED] ?: true
    }

    suspend fun setTtsVoiceProfile(profileId: String) {
        context.dataStore.edit { preferences ->
            preferences[TTS_VOICE_PROFILE] = profileId
        }
    }

    fun getTtsVoiceProfile(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TTS_VOICE_PROFILE] ?: "default"
    }

    suspend fun setTtsMuted(muted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TTS_MUTED] = muted
        }
    }

    fun getTtsMuted(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TTS_MUTED] ?: false
    }

    // ==================== AI Temperature ====================
    
    suspend fun setAiTemperature(temperature: Double) {
        context.dataStore.edit { preferences ->
            preferences[AI_TEMPERATURE] = temperature.coerceIn(0.0, 1.0)
        }
    }

    fun getAiTemperature(): Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[AI_TEMPERATURE] ?: 0.7
    }

    // ==================== Chat Theme ====================
    
    suspend fun setChatTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_THEME] = theme
        }
    }

    fun getChatTheme(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHAT_THEME] ?: "system"
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = mode
        }
    }

    fun getDarkMode(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: "system"
    }

    // ==================== Genre Theme ====================
    
    suspend fun setGenreThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GENRE_THEME_ENABLED] = enabled
        }
    }

    fun getGenreThemeEnabled(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GENRE_THEME_ENABLED] ?: true
    }

    // ==================== Age Gate ====================
    
    suspend fun setAgeGateConfirmed(confirmed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AGE_GATE_CONFIRMED] = confirmed
        }
    }

    fun getAgeGateConfirmed(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AGE_GATE_CONFIRMED] ?: false
    }

    // ==================== Last Active Story ====================
    
    suspend fun setLastActiveStoryId(storyId: String?) {
        context.dataStore.edit { preferences ->
            if (storyId != null) {
                preferences[LAST_ACTIVE_STORY_ID] = storyId
            } else {
                preferences.remove(LAST_ACTIVE_STORY_ID)
            }
        }
    }

    fun getLastActiveStoryId(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_ACTIVE_STORY_ID]
    }

    // ==================== Haptic ====================
    
    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED] = enabled
        }
    }

    fun getHapticEnabled(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED] ?: true
    }

    // ==================== Clear all settings ====================
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
