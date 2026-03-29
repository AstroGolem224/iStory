package com.storybuilder.data.tts

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client facade for Text-to-Speech functionality
 * Provides a simplified API for TTS operations
 */
@Singleton
class TTSClient @Inject constructor(
    private val ttsManager: TtsManager
) {
    /**
     * Speak the given text
     */
    fun speak(text: String) {
        ttsManager.speak(text)
    }

    /**
     * Stop current speech
     */
    fun stop() {
        ttsManager.stop()
    }

    /**
     * Enable or disable TTS globally
     */
    fun setEnabled(enabled: Boolean) {
        ttsManager.setEnabled(enabled)
    }

    /**
     * Check if TTS is enabled
     */
    fun isEnabled(): Boolean {
        return ttsManager.getIsEnabledValue()
    }

    /**
     * Set voice profile by genre ID
     */
    fun setVoiceProfileForGenre(genreId: String) {
        ttsManager.setVoiceProfileForGenre(genreId)
    }

    /**
     * Initialize the TTS engine
     */
    suspend fun initialize(): Boolean {
        return ttsManager.initialize()
    }

    /**
     * Shutdown TTS engine
     */
    fun shutdown() {
        ttsManager.shutdown()
    }
}
