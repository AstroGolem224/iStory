package com.storybuilder.feature.storylibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.domain.model.Character
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.CharacterRepository
import com.storybuilder.domain.repository.GenreRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import com.storybuilder.domain.repository.StoryRepository
import com.storybuilder.domain.usecase.ExportResult
import com.storybuilder.domain.usecase.ExportStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryWithDetails(
    val story: Story,
    val genre: Genre?,
    val character: Character?,
    val beatCount: Int,
    val progress: Float
)

data class StoryLibraryUiState(
    val stories: List<StoryWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportContent: ExportResult? = null
)

@HiltViewModel
class StoryLibraryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val genreRepository: GenreRepository,
    private val characterRepository: CharacterRepository,
    private val storyBeatRepository: StoryBeatRepository,
    private val exportStoryUseCase: ExportStoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryLibraryUiState())
    val uiState: StateFlow<StoryLibraryUiState> = _uiState.asStateFlow()

    fun loadStories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                storyRepository.getAllStories().collect { stories ->
                    val storiesWithDetails = stories.map { story ->
                        val genre = genreRepository.getGenreById(story.genreId)
                        val character = characterRepository.getCharacterById(story.characterId)
                        val beats = storyBeatRepository.getBeatsForStoryOnce(story.id)
                        
                        StoryWithDetails(
                            story = story,
                            genre = genre,
                            character = character,
                            beatCount = beats.size,
                            progress = calculateProgress(beats)
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        stories = storiesWithDetails,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun calculateProgress(beats: List<StoryBeat>): Float {
        if (beats.isEmpty()) return 0f
        val completedBeats = beats.count { it.selectedOptionIndex != null || it.freeTextInput != null }
        return (completedBeats.toFloat() / beats.size).coerceIn(0f, 1f)
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(storyId)
        }
    }

    fun exportStory(storyId: String) {
        viewModelScope.launch {
            val storyWithDetails = _uiState.value.stories.find { it.story.id == storyId }
            storyWithDetails?.let { details ->
                val beats = storyBeatRepository.getBeatsForStoryOnce(storyId)
                val exportResult = exportStoryUseCase(
                    story = details.story,
                    genre = details.genre,
                    character = details.character,
                    beats = beats
                )
                _uiState.value = _uiState.value.copy(exportContent = exportResult)
            }
        }
    }

    fun shareExport(exportResult: ExportResult) {
        // This would typically trigger a platform-specific share intent
        // For now, we just clear the export state
    }

    fun clearExport() {
        _uiState.value = _uiState.value.copy(exportContent = null)
    }
}

// Extension function to get beats once (not as a flow)
private suspend fun StoryBeatRepository.getBeatsForStoryOnce(storyId: String): List<StoryBeat> {
    var beats: List<StoryBeat> = emptyList()
    getBeatsForStory(storyId).collect { beats = it }
    return beats
}
