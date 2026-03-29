package com.storybuilder.feature.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.model.StoryPacing
import com.storybuilder.domain.repository.GenreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenreSelectUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenre: Genre? = null,
    val darknessLevel: Int = 5,
    val pacing: StoryPacing = StoryPacing.MEDIUM,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenreSelectViewModel @Inject constructor(
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenreSelectUiState())
    val uiState: StateFlow<GenreSelectUiState> = _uiState.asStateFlow()

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                genreRepository.getAllGenres().collect { genres ->
                    _uiState.value = _uiState.value.copy(
                        genres = genres,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Fallback to default genres if database fails
                _uiState.value = _uiState.value.copy(
                    genres = Genre.getDefaultGenres(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectGenre(genre: Genre) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre)
    }

    fun setDarknessLevel(level: Int) {
        _uiState.value = _uiState.value.copy(darknessLevel = level.coerceIn(1, 10))
    }

    fun setPacing(pacing: StoryPacing) {
        _uiState.value = _uiState.value.copy(pacing = pacing)
    }
}
