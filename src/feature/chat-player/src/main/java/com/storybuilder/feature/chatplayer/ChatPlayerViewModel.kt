package com.storybuilder.feature.chatplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.InputMode
import com.storybuilder.domain.model.SenderType
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.ChatMessageRepository
import com.storybuilder.domain.repository.GenreRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import com.storybuilder.domain.usecase.GenerateNextBeatUseCase
import com.storybuilder.domain.usecase.SelectOptionUseCase
import com.storybuilder.domain.usecase.SubmitTextChoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentOptions: List<String> = emptyList(),
    val currentBeat: StoryBeat? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val story: Story? = null,
    val inputMode: InputMode = InputMode.SUGGESTED_OPTIONS,
    val isKeyboardVisible: Boolean = false
)

@HiltViewModel
class ChatPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatMessageRepository: ChatMessageRepository,
    private val storyBeatRepository: StoryBeatRepository,
    private val genreRepository: GenreRepository,
    private val generateNextBeatUseCase: GenerateNextBeatUseCase,
    private val selectOptionUseCase: SelectOptionUseCase,
    private val submitTextChoiceUseCase: SubmitTextChoiceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // For Phase 4: Use storyId from navigation or create new story
    private val storyId: String = savedStateHandle.get<String>("storyId") ?: "test-story-001"
    
    // Hardcoded test data for now - in production, these would come from the story creation flow
    private val characterName = "Detective Sarah Blackwood"
    private val characterDescription = "A sharp-witted private investigator with a troubled past"
    private val genreName = "Mystery/Noir"
    private val genreToneGuidelines = "Create a noir atmosphere with shadows, rain, and moral ambiguity. " +
        "Focus on the gritty underbelly of the city and complex characters with hidden motives."
    
    private val story = Story(
        id = storyId,
        title = "The Mystery of Blackwood Manor",
        genreId = "mystery",
        characterId = "detective-001",
        darknessLevel = 7,
        suggestOptionsEnabled = true
    )

    init {
        _uiState.value = ChatUiState(story = story)
        initializeStory()
    }

    private fun initializeStory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Try to get existing beats first
            storyBeatRepository.getBeatsForStory(storyId).collect { beats ->
                if (beats.isEmpty()) {
                    // Generate opening beat
                    generateOpeningBeat()
                } else {
                    // Load existing beats
                    loadBeats(beats)
                }
            }
        }
    }

    private suspend fun generateOpeningBeat() {
        generateNextBeatUseCase.generateOpeningBeat(
            story = story,
            characterName = characterName,
            characterDescription = characterDescription,
            genreName = genreName,
            genreToneGuidelines = genreToneGuidelines
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

    private fun loadBeats(beats: List<StoryBeat>) {
        val messages = mutableListOf<ChatMessage>()
        val sortedBeats = beats.sortedBy { it.sequenceOrder }
        
        sortedBeats.forEach { beat ->
            // Add narrator message
            messages.add(
                ChatMessage(
                    storyId = storyId,
                    senderType = SenderType.NARRATOR,
                    content = beat.narratorText
                )
            )
            
            // Add user message if option was selected or free text was entered
            val freeText = beat.freeTextInput
            val selectedIndex = beat.selectedOptionIndex
            
            when {
                freeText != null -> {
                    messages.add(
                        ChatMessage(
                            storyId = storyId,
                            senderType = SenderType.USER,
                            content = freeText
                        )
                    )
                }
                selectedIndex != null && selectedIndex >= 0 -> {
                    beat.suggestedOptions?.getOrNull(selectedIndex)?.let { option ->
                        messages.add(
                            ChatMessage(
                                storyId = storyId,
                                senderType = SenderType.USER,
                                content = option
                            )
                        )
                    }
                }
            }
        }

        val currentBeat = sortedBeats.lastOrNull { 
            it.selectedOptionIndex != null || it.freeTextInput != null 
        } ?: sortedBeats.lastOrNull()

        _uiState.value = _uiState.value.copy(
            messages = messages,
            currentBeat = currentBeat,
            currentOptions = currentBeat?.suggestedOptions ?: emptyList(),
            isLoading = false
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

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message,
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val currentBeat = _uiState.value.currentBeat
            
            val previousBeats = storyBeatRepository.getBeatsForStory(storyId)
                .let { flow ->
                    var beats: List<StoryBeat> = emptyList()
                    flow.collect { beats = it }
                    beats.sortedBy { it.sequenceOrder }
                }

            val result = submitTextChoiceUseCase(
                story = story,
                currentBeat = currentBeat,
                userInput = userInput,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
                previousBeats = previousBeats.filter { 
                    currentBeat?.let { current -> it.sequenceOrder < current.sequenceOrder } ?: true 
                }
            )

            val nextBeat = result.nextBeat
            val error = result.error

            when {
                nextBeat != null -> {
                    // Add user message to chat
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + result.userMessage
                    )
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
     * Handle voice input (placeholder for STT integration)
     */
    fun onVoiceInputRequested() {
        // Placeholder for STT integration
        println("[ChatPlayerViewModel] Voice input requested - STT integration placeholder")
    }

    fun selectOption(optionIndex: Int) {
        val currentBeat = _uiState.value.currentBeat ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val previousBeats = storyBeatRepository.getBeatsForStory(storyId)
                .let { flow ->
                    var beats: List<StoryBeat> = emptyList()
                    flow.collect { beats = it }
                    beats.sortedBy { it.sequenceOrder }
                }

            val result = selectOptionUseCase(
                story = story,
                currentBeat = currentBeat,
                optionIndex = optionIndex,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
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
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + message
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Called when keyboard visibility changes
     */
    fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        _uiState.value = _uiState.value.copy(isKeyboardVisible = isVisible)
    }
}
