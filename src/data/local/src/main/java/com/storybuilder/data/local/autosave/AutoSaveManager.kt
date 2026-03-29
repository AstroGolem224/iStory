package com.storybuilder.data.local.autosave

import com.storybuilder.data.local.dao.StoryBeatDao
import com.storybuilder.data.local.dao.StoryDao
import com.storybuilder.data.local.entity.StoryBeatEntity
import com.storybuilder.data.local.entity.StoryEntity
import com.storybuilder.data.local.preferences.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages auto-save functionality and crash recovery
 */
@Singleton
class AutoSaveManager @Inject constructor(
    private val storyDao: StoryDao,
    private val storyBeatDao: StoryBeatDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var autoSaveJob: Job? = null

    private val _lastSavedStoryId = MutableStateFlow<String?>(null)
    val lastSavedStoryId: StateFlow<String?> = _lastSavedStoryId.asStateFlow()

    private val _isAutoSaving = MutableStateFlow(false)
    val isAutoSaving: StateFlow<Boolean> = _isAutoSaving.asStateFlow()

    private val _pendingSaves = MutableStateFlow(0)
    val pendingSaves: StateFlow<Int> = _pendingSaves.asStateFlow()

    companion object {
        private const val AUTO_SAVE_DELAY_MS = 2000L // 2 seconds
    }

    /**
     * Schedule auto-save for a story
     */
    fun scheduleStorySave(story: StoryEntity) {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            saveStory(story)
        }
    }

    /**
     * Schedule auto-save for a story beat
     */
    fun scheduleBeatSave(storyBeat: StoryBeatEntity) {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            saveBeat(storyBeat)
        }
    }

    /**
     * Save story immediately
     */
    suspend fun saveStory(story: StoryEntity) {
        _isAutoSaving.value = true
        _pendingSaves.value++
        try {
            storyDao.insertStory(story)
            settingsDataStore.setLastActiveStoryId(story.id)
            _lastSavedStoryId.value = story.id
        } finally {
            _pendingSaves.value--
            if (_pendingSaves.value == 0) {
                _isAutoSaving.value = false
            }
        }
    }

    /**
     * Save story beat immediately
     */
    suspend fun saveBeat(storyBeat: StoryBeatEntity) {
        _isAutoSaving.value = true
        _pendingSaves.value++
        try {
            storyBeatDao.insertBeat(storyBeat)
            settingsDataStore.setLastActiveStoryId(storyBeat.storyId)
        } finally {
            _pendingSaves.value--
            if (_pendingSaves.value == 0) {
                _isAutoSaving.value = false
            }
        }
    }

    /**
     * Force immediate save (for app going to background)
     */
    suspend fun forceSave(story: StoryEntity?, beats: List<StoryBeatEntity> = emptyList()) {
        autoSaveJob?.cancel()
        _isAutoSaving.value = true
        _pendingSaves.value += beats.size + if (story != null) 1 else 0
        
        try {
            story?.let {
                storyDao.insertStory(it)
                settingsDataStore.setLastActiveStoryId(it.id)
                _lastSavedStoryId.value = it.id
            }
            beats.forEach { beat ->
                storyBeatDao.insertBeat(beat)
            }
        } finally {
            _pendingSaves.value = 0
            _isAutoSaving.value = false
        }
    }

    /**
     * Get the last active story ID for crash recovery
     */
    suspend fun getLastActiveStoryId(): String? {
        return settingsDataStore.getLastActiveStoryId()
            .let { flow ->
                var result: String? = null
                flow.collect { result = it }
                result
            }
    }

    /**
     * Clear last active story (e.g., when story is completed or deleted)
     */
    suspend fun clearLastActiveStory() {
        settingsDataStore.setLastActiveStoryId(null)
        _lastSavedStoryId.value = null
    }

    /**
     * Check if there's a story to recover
     */
    suspend fun hasRecoverableStory(): Boolean {
        val lastStoryId = getLastActiveStoryId()
        return lastStoryId != null && storyDao.getStoryById(lastStoryId) != null
    }

    /**
     * Get the story to recover
     */
    suspend fun getRecoverableStory(): StoryEntity? {
        val lastStoryId = getLastActiveStoryId() ?: return null
        return storyDao.getStoryById(lastStoryId)
    }

    /**
     * Cancel pending auto-saves
     */
    fun cancelPendingSaves() {
        autoSaveJob?.cancel()
    }
}
