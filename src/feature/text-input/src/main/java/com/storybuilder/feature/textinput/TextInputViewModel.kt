package com.storybuilder.feature.textinput

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val MAX_CHARS = 500

/**
 * UI State for text input
 */
data class TextInputUiState(
    val text: String = "",
    val isVoiceListening: Boolean = false,
    val validationError: String? = null
) {
    val charCount: Int get() = text.length
    val isNearLimit: Boolean get() = charCount > MAX_CHARS * 0.9
    val isAtLimit: Boolean get() = charCount >= MAX_CHARS
    val canSend: Boolean get() = text.isNotBlank() && !isAtLimit && validationError == null
}

/**
 * ViewModel for managing text input state and validation
 */
@HiltViewModel
class TextInputViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TextInputUiState())
    val uiState: StateFlow<TextInputUiState> = _uiState.asStateFlow()

    /**
     * Update the text input with validation
     */
    fun onTextChanged(newText: String) {
        val trimmedText = if (newText.length > MAX_CHARS) {
            newText.take(MAX_CHARS)
        } else {
            newText
        }

        val error = validateInput(trimmedText)
        
        _uiState.update { state ->
            state.copy(
                text = trimmedText,
                validationError = error
            )
        }
    }

    /**
     * Clear the text input
     */
    fun clearText() {
        _uiState.update { it.copy(text = "", validationError = null) }
    }

    /**
     * Set voice listening state
     */
    fun setVoiceListening(isListening: Boolean) {
        _uiState.update { it.copy(isVoiceListening = isListening) }
    }

    /**
     * Validate input and return error message if invalid
     */
    private fun validateInput(text: String): String? {
        return when {
            text.isBlank() -> "Text cannot be empty"
            text.length > MAX_CHARS -> "Text exceeds maximum length of $MAX_CHARS characters"
            else -> null
        }
    }

    /**
     * Check if input is valid for submission
     */
    fun isValid(): Boolean {
        return _uiState.value.canSend
    }

    /**
     * Get current text value
     */
    fun getCurrentText(): String = _uiState.value.text
}
