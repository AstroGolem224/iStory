package com.storybuilder.feature.chatplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.InputMode
import com.storybuilder.domain.model.SenderType
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.CharacterRepository
import com.storybuilder.domain.repository.ChatMessageRepository
import com.storybuilder.domain.repository.GenreRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import com.storybuilder.domain.repository.StoryRepository
import com.storybuilder.data.stt.STTClient
import com.storybuilder.data.stt.STTResult
import com.storybuilder.domain.usecase.GenerateNextBeatUseCase
import com.storybuilder.domain.usecase.SelectOptionUseCase
import com.storybuilder.domain.usecase.SubmitTextChoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentOptions: List<String> = emptyList(),
    val currentBeat: StoryBeat? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val story: Story? = null,
    val characterName: String = "",
    val characterDescription: String = "",
    val genreName: String = "",
    val genreToneGuidelines: String = "",
    val inputMode: InputMode = InputMode.SUGGESTED_OPTIONS,
    val isKeyboardVisible: Boolean = false,
    val isListening: Boolean = false,
    val speechInput: String = ""
)

@HiltViewModel
class ChatPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatMessageRepository: ChatMessageRepository,
    private val storyBeatRepository: StoryBeatRepository,
    private val storyRepository: StoryRepository,
    private val characterRepository: CharacterRepository,
    private val genreRepository: GenreRepository,
    private val sttClient: STTClient,
    private val generateNextBeatUseCase: GenerateNextBeatUseCase,
    private val selectOptionUseCase: SelectOptionUseCase,
    private val submitTextChoiceUseCase: SubmitTextChoiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // For Phase 4: Use storyId from navigation or create new story
    private val storyId: String = savedStateHandle.get<String>("storyId") ?: "test-story-001"
    
    init {
        initializeStory()
    }

    private fun initializeStory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 1. Load Story metadata
            storyRepository.getStoryById(storyId).collect { story ->
                if (story == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Story not found",
                        isLoading = false
                    )
                    return@collect
                }
                
                // 2. Load Character and Genre details
                val character = characterRepository.getCharacterById(story.characterId)
                val genre = genreRepository.getGenreById(story.genreId)
                
                _uiState.value = _uiState.value.copy(
                    story = story,
                    characterName = character?.name ?: "Unknown Character",
                    characterDescription = character?.let { "${it.archetype} - ${it.traits.joinToString(", ")}. ${it.backstory ?: ""}" } ?: "No description provided",
                    genreName = genre?.name ?: "Unknown Genre",
                    genreToneGuidelines = genre?.toneGuidelines ?: "Traditional storytelling"
                )

                // 3. Collect messages for UI
                launch {
                    chatMessageRepository.getMessagesForStory(storyId).collect { messages ->
                        _uiState.value = _uiState.value.copy(
                            messages = messages,
                            isLoading = false
                        )
                    }
                }

                // 4. Collect beats for logic
                launch {
                    storyBeatRepository.getBeatsForStory(storyId).collect { beats ->
                        if (beats.isEmpty()) {
                            generateOpeningBeat(story)
                        } else {
                            val sortedBeats = beats.sortedBy { it.sequenceOrder }
                            val currentBeat = sortedBeats.lastOrNull { 
                                it.selectedOptionIndex != null || it.freeTextInput != null 
                            } ?: sortedBeats.lastOrNull()
                            
                            _uiState.value = _uiState.value.copy(
                                currentBeat = currentBeat,
                                currentOptions = currentBeat?.suggestedOptions ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun generateOpeningBeat(story: Story) {
        generateNextBeatUseCase.generateOpeningBeat(
            story = story,
            characterName = _uiState.value.characterName,
            characterDescription = _uiState.value.characterDescription,
            genreName = _uiState.value.genreName,
            genreToneGuidelines = _uiState.value.genreToneGuidelines
        ).fold(
            onSuccess = { beat ->
                addNarratorMessage(beat)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Failed to generate story",
                    isLoading = false
                )
            }
        )
    }


    private fun addNarratorMessage(beat: StoryBeat) {
        val message = ChatMessage(
            storyId = storyId,
            senderType = SenderType.NARRATOR,
            content = beat.narratorText,
            metadata = com.storybuilder.domain.model.MessageMetadata(
                suggestedOptions = beat.suggestedOptions
            )
        )

        viewModelScope.launch {
            chatMessageRepository.insertMessage(message)
        }
        
        // We no longer update uiState.messages manually here as it's collected from repo
        _uiState.value = _uiState.value.copy(
            currentBeat = beat,
            currentOptions = beat.suggestedOptions ?: emptyList(),
            isLoading = false
        )
    }

    /**
     * Switch between input modes (Suggested Options vs Free Text)
     */
    fun setInputMode(mode: InputMode) {
        _uiState.value = _uiState.value.copy(
            inputMode = mode,
            isKeyboardVisible = mode == InputMode.FREE_TEXT
        )
    }

    /**
     * Toggle between input modes
     */
    fun toggleInputMode() {
        val newMode = if (_uiState.value.inputMode == InputMode.SUGGESTED_OPTIONS) {
            InputMode.FREE_TEXT
        } else {
            InputMode.SUGGESTED_OPTIONS
        }
        setInputMode(newMode)
    }

    /**
     * Submit free text input from the user
     */
    fun submitFreeText(userInput: String) {
        if (userInput.isBlank()) return
        
        viewModelScope.launch {
            val story = _uiState.value.story ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val currentBeat = _uiState.value.currentBeat
            
            val previousBeats = storyBeatRepository.getBeatsForStory(storyId).first()
                .sortedBy { it.sequenceOrder }

            val result = submitTextChoiceUseCase(
                story = story,
                currentBeat = currentBeat,
                userInput = userInput,
                characterName = _uiState.value.characterName,
                characterDescription = _uiState.value.characterDescription,
                genreName = _uiState.value.genreName,
                genreToneGuidelines = _uiState.value.genreToneGuidelines,
                previousBeats = previousBeats.filter { 
                    currentBeat?.let { current -> it.sequenceOrder < current.sequenceOrder } ?: true 
                }
            )

            val nextBeat = result.nextBeat
            val error = result.error

            when {
                nextBeat != null -> {
                    // No need to manually update messages, repo collection handles it
                    addNarratorMessage(nextBeat)
                }
                error != null -> {
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to generate next beat",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handle voice input using STTClient
     */
    fun onVoiceInputRequested() {
        if (_uiState.value.isListening) {
            sttClient.stopListening()
            _uiState.value = _uiState.value.copy(isListening = false)
            return
        }

        viewModelScope.launch {
            sttClient.startListening().collect { result ->
                when (result) {
                    is STTResult.Ready -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            speechInput = ""
                        )
                    }
                    is STTResult.Partial -> {
                        _uiState.value = _uiState.value.copy(
                            speechInput = result.text
                        )
                    }
                    is STTResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            speechInput = ""
                        )
                        if (result.text.isNotBlank()) {
                            submitFreeText(result.text)
                        }
                    }
                    is STTResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val currentBeat = _uiState.value.currentBeat ?: return
        
        viewModelScope.launch {
            val story = _uiState.value.story ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val previousBeats = storyBeatRepository.getBeatsForStory(storyId).first()
                .sortedBy { it.sequenceOrder }

            val result = selectOptionUseCase(
                story = story,
                currentBeat = currentBeat,
                optionIndex = optionIndex,
                characterName = _uiState.value.characterName,
                characterDescription = _uiState.value.characterDescription,
                genreName = _uiState.value.genreName,
                genreToneGuidelines = _uiState.value.genreToneGuidelines,
                previousBeats = previousBeats.filter { it.sequenceOrder < currentBeat.sequenceOrder }
            )

            val nextBeat = result.nextBeat
            val error = result.error

            when {
                nextBeat != null -> {
                    addNarratorMessage(nextBeat)
                }
                error != null -> {
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to generate next beat",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val message = ChatMessage(
                storyId = storyId,
                senderType = SenderType.USER,
                content = content
            )
            chatMessageRepository.insertMessage(message)
            // No manual UI update, collected from flow
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retry() {
        dismissError()
        initializeStory()
    }

    /**
     * Called when keyboard visibility changes
     */
    fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        _uiState.value = _uiState.value.copy(isKeyboardVisible = isVisible)
    }
}
