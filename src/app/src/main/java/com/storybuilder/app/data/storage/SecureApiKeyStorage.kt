package com.storybuilder.app.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureApiKeyStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_api_prefs_v3"
        
        // Legacy keys
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        
        // Provider-specific keys
        private const val KEY_OPENAI_API_KEY = "openai_api_key"
        private const val KEY_ANTHROPIC_API_KEY = "anthropic_api_key"
        private const val KEY_GOOGLE_API_KEY = "google_api_key"
        private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
        private const val KEY_NIM_API_KEY = "nim_api_key"
        
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
        
        private fun getApiKeyKey(provider: ApiProvider): String = when (provider) {
            ApiProvider.OPENAI -> KEY_OPENAI_API_KEY
            ApiProvider.ANTHROPIC -> KEY_ANTHROPIC_API_KEY
            ApiProvider.GOOGLE -> KEY_GOOGLE_API_KEY
            ApiProvider.OPENROUTER -> KEY_OPENROUTER_API_KEY
            ApiProvider.NIM -> KEY_NIM_API_KEY
        }
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            // If creation fails due to Keystore corruption, clear the prefs and try again
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            try {
                createEncryptedPrefs()
            } catch (e2: Exception) {
                // Return a non-encrypted fallback or let it crash if it's truly broken
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPrefs() = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ==================== Multi-Provider Methods ====================
    
    fun saveApiCredentials(credentials: ApiCredentials) {
        encryptedPrefs.edit()
            .putString(getApiKeyKey(credentials.provider), credentials.apiKey)
            .apply()
    }
    
    fun getApiKey(provider: ApiProvider): String? {
        return encryptedPrefs.getString(getApiKeyKey(provider), null)
    }
    
    fun hasApiKey(provider: ApiProvider): Boolean {
        return !getApiKey(provider).isNullOrBlank()
    }
    
    fun hasAnyApiKey(): Boolean {
        return ApiProvider.entries.any { hasApiKey(it) }
    }
    
    fun clearApiKey(provider: ApiProvider) {
        val editor = encryptedPrefs.edit()
            .remove(getApiKeyKey(provider))
            
        if (provider == ApiProvider.GOOGLE) {
            editor.remove(KEY_GEMINI_API_KEY)
        }
        
        editor.apply()
    }
    
    fun setActiveProvider(provider: ApiProvider) {
        encryptedPrefs.edit()
            .putString(KEY_ACTIVE_PROVIDER, provider.name)
            .apply()
    }
    
    fun getActiveProvider(): ApiProvider {
        val providerName = encryptedPrefs.getString(KEY_ACTIVE_PROVIDER, null)
        return ApiProvider.fromString(providerName ?: "") ?: ApiProvider.GOOGLE
    }

    // ==================== Legacy Methods (for backward compatibility) ====================
    
    fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_GOOGLE_API_KEY, apiKey)
            .apply()
    }

    fun getApiKey(): String? {
        // Check for Google key first, then legacy Gemini key
        return encryptedPrefs.getString(KEY_GOOGLE_API_KEY, null)
            ?: encryptedPrefs.getString(KEY_GEMINI_API_KEY, null)
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    fun clearApiKey() {
        encryptedPrefs.edit()
            .remove(KEY_GEMINI_API_KEY)
            .remove(KEY_GOOGLE_API_KEY)
            .apply()
    }
    
    // ==================== Clear All ====================
    
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }
}
