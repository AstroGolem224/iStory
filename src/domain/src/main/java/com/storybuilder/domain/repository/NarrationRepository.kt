package com.storybuilder.domain.repository

import com.storybuilder.domain.model.VoiceProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Text-to-Speech narration functionality
 */
interface NarrationRepository {
    /**
     * Speak the given text using the current voice profile
     */
    suspend fun speak(text: String)

    /**
     * Stop current speech
     */
    fun stop()

    /**
     * Check if TTS is currently speaking
     */
    fun isSpeaking(): Boolean

    /**
     * Set TTS enabled/disabled
     */
    suspend fun setEnabled(enabled: Boolean)

    /**
     * Check if TTS is enabled
     */
    fun isEnabled(): Flow<Boolean>

    /**
     * Set the voice profile for narration
     */
    suspend fun setVoiceProfile(profile: VoiceProfile)

    /**
     * Get current voice profile
     */
    fun getVoiceProfile(): Flow<VoiceProfile>

    /**
     * Set voice profile by genre ID
     */
    suspend fun setVoiceProfileForGenre(genreId: String)

    /**
     * Queue multiple texts for sequential playback
     */
    suspend fun queueSpeech(texts: List<String>)

    /**
     * Clear the speech queue
     */
    fun clearQueue()

    /**
     * Initialize TTS engine
     */
    suspend fun initialize(): Boolean

    /**
     * Shutdown TTS engine
     */
    fun shutdown()

    /**
     * Mute/unmute TTS temporarily (doesn't change enabled setting)
     */
    suspend fun setMuted(muted: Boolean)

    /**
     * Check if currently muted
     */
    fun isMuted(): Flow<Boolean>
}
