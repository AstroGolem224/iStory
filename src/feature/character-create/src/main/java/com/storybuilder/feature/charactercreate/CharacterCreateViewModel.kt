package com.storybuilder.feature.charactercreate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.domain.model.Character
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.usecase.CreateNewStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterCreateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val genre: Genre? = null,
    val darknessLevel: Int = 5,
    val pacing: String = "MEDIUM"
)

@HiltViewModel
class CharacterCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createNewStoryUseCase: CreateNewStoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterCreateUiState())
    val uiState: StateFlow<CharacterCreateUiState> = _uiState.asStateFlow()

    init {
        val genre = savedStateHandle.get<Genre>("selectedGenre")
        val darknessLevel = savedStateHandle.get<Int>("darknessLevel") ?: 5
        val pacing = savedStateHandle.get<String>("pacing") ?: "MEDIUM"
        
        _uiState.value = _uiState.value.copy(
            genre = genre,
            darknessLevel = darknessLevel,
            pacing = pacing
        )
    }

    fun createStory(character: Character, onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val genre = state.genre ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            createNewStoryUseCase(
                genre = genre,
                character = character,
                darknessLevel = state.darknessLevel,
                pacing = state.pacing
            ).fold(
                onSuccess = { storyId ->
                    onSuccess(storyId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create story"
                    )
                }
            )
        }
    }
}
