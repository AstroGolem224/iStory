package com.storybuilder.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Wrapper around Android TextToSpeech with advanced features
 */
class TTSEngine(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val speechQueue = ConcurrentLinkedQueue<SpeechItem>()
    private var currentUtteranceId: String? = null
    private var onSpeechCompleteListener: (() -> Unit)? = null

    private var currentPitch = 1.0f
    private var currentSpeechRate = 1.0f

    private val initDeferred = CompletableDeferred<Boolean>()

    data class SpeechItem(
        val text: String,
        val utteranceId: String = System.currentTimeMillis().toString()
    )

    fun initialize(): CompletableDeferred<Boolean> {
        if (textToSpeech != null) {
            initDeferred.complete(true)
            return initDeferred
        }

        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupTTS()
                _isInitialized.value = true
                initDeferred.complete(true)
            } else {
                initDeferred.complete(false)
            }
        }
        return initDeferred
    }

    private fun setupTTS() {
        textToSpeech?.apply {
            language = Locale.getDefault()
            setPitch(currentPitch)
            setSpeechRate(currentSpeechRate)

            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    currentUtteranceId = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == currentUtteranceId) {
                        _isSpeaking.value = false
                        currentUtteranceId = null
                        processQueue()
                        onSpeechCompleteListener?.invoke()
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    currentUtteranceId = null
                    processQueue()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    currentUtteranceId = null
                    processQueue()
                }
            })
        }
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (_isMuted.value || textToSpeech == null) return

        val item = SpeechItem(text, utteranceId)
        
        if (_isSpeaking.value) {
            // Queue the speech
            speechQueue.offer(item)
        } else {
            // Speak immediately
            speakInternal(item)
        }
    }

    fun queueSpeech(texts: List<String>) {
        texts.forEach { text ->
            speechQueue.offer(SpeechItem(text))
        }
        if (!_isSpeaking.value) {
            processQueue()
        }
    }

    private fun processQueue() {
        if (_isMuted.value) {
            speechQueue.clear()
            return
        }

        val nextItem = speechQueue.poll()
        if (nextItem != null) {
            speakInternal(nextItem)
        }
    }

    private fun speakInternal(item: SpeechItem) {
        textToSpeech?.let { tts ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(
                    item.text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    item.utteranceId
                )
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = item.utteranceId
                tts.speak(item.text, TextToSpeech.QUEUE_FLUSH, params)
            }
        }
    }

    fun stop() {
        textToSpeech?.stop()
        speechQueue.clear()
        _isSpeaking.value = false
        currentUtteranceId = null
    }

    fun clearQueue() {
        speechQueue.clear()
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(currentPitch)
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.25f, 2.0f)
        textToSpeech?.setSpeechRate(currentSpeechRate)
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stop()
        }
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isInitialized.value = false
    }

    fun setOnSpeechCompleteListener(listener: () -> Unit) {
        onSpeechCompleteListener = listener
    }

    /**
     * Get available voices (requires API 21+)
     */
    fun getAvailableVoices(): Set<Voice>? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.voices
        } else {
            null
        }
    }

    /**
     * Set a specific voice (requires API 21+)
     */
    fun setVoice(voice: Voice): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.setVoice(voice) == TextToSpeech.SUCCESS
        } else {
            false
        }
    }
}
