package com.storybuilder.data.tts

import android.content.Context
import com.storybuilder.domain.model.VoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages TTS playback queue, voice selection, and settings
 */
@Singleton
class TtsManager @Inject constructor(
    private val context: Context
) {
    private val ttsEngine: TTSEngine by lazy { TTSEngine(context) }

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: Flow<Boolean> = _isEnabled.asStateFlow()
    fun getIsEnabledValue(): Boolean = _isEnabled.value

    private val _currentVoiceProfile = MutableStateFlow(VoiceProfile.DEFAULT)
    val currentVoiceProfile: Flow<VoiceProfile> = _currentVoiceProfile.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: Flow<Boolean> = _isSpeaking.asStateFlow()
    fun getIsSpeakingValue(): Boolean = _isSpeaking.value

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: Flow<Boolean> = _isInitialized.asStateFlow()
    fun getIsInitializedValue(): Boolean = _isInitialized.value

    suspend fun initialize(): Boolean {
        if (_isInitialized.value) return true

        val result = ttsEngine.initialize().await()
        if (result) {
            _isInitialized.value = true
            observeTtsState()
        }
        return result
    }

    private fun observeTtsState() {
        // Collect speaking state
        CoroutineScope(Dispatchers.Main).launch {
            ttsEngine.isSpeaking.collect { speaking ->
                _isSpeaking.value = speaking
            }
        }
    }

    /**
     * Speak text using the current voice profile
     */
    fun speak(text: String) {
        if (!_isEnabled.value) return
        ttsEngine.speak(text)
    }

    /**
     * Queue multiple texts for sequential playback
     */
    fun queueSpeech(texts: List<String>) {
        if (!_isEnabled.value) return
        ttsEngine.queueSpeech(texts)
    }

    /**
     * Stop all speech and clear queue
     */
    fun stop() {
        ttsEngine.stop()
    }

    /**
     * Clear the speech queue
     */
    fun clearQueue() {
        ttsEngine.clearQueue()
    }

    /**
     * Enable or disable TTS
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            stop()
        }
    }

    /**
     * Set voice profile with automatic pitch and rate adjustment
     */
    fun setVoiceProfile(profile: VoiceProfile) {
        _currentVoiceProfile.value = profile
        ttsEngine.setPitch(profile.pitch)
        ttsEngine.setSpeechRate(profile.speechRate)
    }

    /**
     * Set voice profile based on genre
     */
    fun setVoiceProfileForGenre(genreId: String) {
        val profile = VoiceProfile.forGenre(genreId)
        setVoiceProfile(profile)
    }

    /**
     * Set pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        ttsEngine.setPitch(pitch)
    }

    /**
     * Set speech rate (0.25 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        ttsEngine.setSpeechRate(rate)
    }

    /**
     * Mute/unmute TTS temporarily
     */
    fun setMuted(muted: Boolean) {
        ttsEngine.setMuted(muted)
    }

    /**
     * Check if TTS is muted
     */
    fun isMuted(): Flow<Boolean> = ttsEngine.isMuted

    /**
     * Shutdown TTS engine
     */
    fun shutdown() {
        ttsEngine.shutdown()
        _isInitialized.value = false
    }
}
