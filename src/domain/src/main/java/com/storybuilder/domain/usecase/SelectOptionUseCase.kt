package com.storybuilder.domain.usecase

import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.MessageMetadata
import com.storybuilder.domain.model.SenderType
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.ChatMessageRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import javax.inject.Inject

class SelectOptionUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository,
    private val storyBeatRepository: StoryBeatRepository,
    private val generateNextBeatUseCase: GenerateNextBeatUseCase
) {
    data class OptionSelectionResult(
        val userMessage: ChatMessage,
        val nextBeat: StoryBeat? = null,
        val error: Throwable? = null
    )

    suspend operator fun invoke(
        story: Story,
        currentBeat: StoryBeat,
        optionIndex: Int,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): OptionSelectionResult {
        val optionText = currentBeat.suggestedOptions?.getOrNull(optionIndex)
            ?: return OptionSelectionResult(
                userMessage = createErrorMessage(story.id),
                error = IllegalArgumentException("Invalid option index: $optionIndex")
            )

        // Create and save user message
        val userMessage = ChatMessage(
            storyId = story.id,
            senderType = SenderType.USER,
            content = optionText,
            metadata = MessageMetadata(
                suggestedOptions = currentBeat.suggestedOptions,
                selectedOptionIndex = optionIndex
            )
        )
        chatMessageRepository.insertMessage(userMessage)

        // Update the current beat with selected option
        val updatedBeat = currentBeat.copy(selectedOptionIndex = optionIndex)
        storyBeatRepository.updateBeat(updatedBeat)

        // Generate next beat
        return try {
            val result = generateNextBeatUseCase(
                story = story,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
                previousBeats = previousBeats + updatedBeat
            )

            result.fold(
                onSuccess = { nextBeat ->
                    OptionSelectionResult(
                        userMessage = userMessage,
                        nextBeat = nextBeat
                    )
                },
                onFailure = { error ->
                    OptionSelectionResult(
                        userMessage = userMessage,
                        error = error
                    )
                }
            )
        } catch (e: Exception) {
            OptionSelectionResult(
                userMessage = userMessage,
                error = e
            )
        }
    }

    private fun createErrorMessage(storyId: String): ChatMessage {
        return ChatMessage(
            storyId = storyId,
            senderType = SenderType.SYSTEM,
            content = "Error processing selection"
        )
    }
}
