package com.storybuilder.app.screens.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.app.data.storage.SecureApiKeyStorage
import com.storybuilder.data.ai.di.ApiKeyProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiKeySetupViewModel @Inject constructor(
    private val secureApiKeyStorage: SecureApiKeyStorage
) : ViewModel() {

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            secureApiKeyStorage.saveApiKey(apiKey)
            ApiKeyProvider.setApiKey(apiKey)
        }
    }
}
