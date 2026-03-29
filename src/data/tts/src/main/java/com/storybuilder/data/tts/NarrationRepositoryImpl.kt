package com.storybuilder.data.tts

import com.storybuilder.domain.model.VoiceProfile
import com.storybuilder.domain.repository.NarrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NarrationRepository using Android TTS
 */
@Singleton
class NarrationRepositoryImpl @Inject constructor(
    private val ttsManager: TtsManager
) : NarrationRepository {

    private val _isEnabled = MutableStateFlow(true)
    private val _voiceProfile = MutableStateFlow(VoiceProfile.DEFAULT)
    private val _isMuted = MutableStateFlow(false)

    override suspend fun speak(text: String) {
        if (_isEnabled.value && !_isMuted.value) {
            ttsManager.speak(text)
        }
    }

    override fun stop() {
        ttsManager.stop()
    }

    override fun isSpeaking(): Boolean {
        return ttsManager.getIsSpeakingValue()
    }

    override suspend fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            ttsManager.stop()
        }
    }

    override fun isEnabled(): Flow<Boolean> = _isEnabled.asStateFlow()

    override suspend fun setVoiceProfile(profile: VoiceProfile) {
        _voiceProfile.value = profile
        ttsManager.setVoiceProfile(profile)
    }

    override fun getVoiceProfile(): Flow<VoiceProfile> = _voiceProfile.asStateFlow()

    override suspend fun setVoiceProfileForGenre(genreId: String) {
        val profile = VoiceProfile.forGenre(genreId)
        setVoiceProfile(profile)
    }

    override suspend fun queueSpeech(texts: List<String>) {
        if (_isEnabled.value && !_isMuted.value) {
            ttsManager.queueSpeech(texts)
        }
    }

    override fun clearQueue() {
        ttsManager.clearQueue()
    }

    override suspend fun initialize(): Boolean {
        return ttsManager.initialize()
    }

    override fun shutdown() {
        ttsManager.shutdown()
    }

    override suspend fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        ttsManager.setMuted(muted)
    }

    override fun isMuted(): Flow<Boolean> = _isMuted.asStateFlow()
}
