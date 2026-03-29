package com.storybuilder.domain.usecase

import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.MessageMetadata
import com.storybuilder.domain.model.SenderType
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.ChatMessageRepository
import com.storybuilder.domain.repository.StoryAIRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import javax.inject.Inject

class SubmitTextChoiceUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository,
    private val storyBeatRepository: StoryBeatRepository,
    private val storyAIRepository: StoryAIRepository
) {
    data class TextSubmissionResult(
        val userMessage: ChatMessage,
        val nextBeat: StoryBeat? = null,
        val error: Throwable? = null
    )

    suspend operator fun invoke(
        story: Story,
        currentBeat: StoryBeat?,
        userInput: String,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): TextSubmissionResult {
        // Create and save user message
        val userMessage = ChatMessage(
            storyId = story.id,
            senderType = SenderType.USER,
            content = userInput,
            metadata = MessageMetadata(
                freeTextInput = userInput
            )
        )
        chatMessageRepository.insertMessage(userMessage)

        // Update current beat with free text input if exists
        currentBeat?.let { beat ->
            val updatedBeat = beat.copy(
                freeTextInput = userInput,
                selectedOptionIndex = -1 // Mark as free text mode
            )
            storyBeatRepository.updateBeat(updatedBeat)
        }

        // Generate next beat with free text context
        return try {
            val result = storyAIRepository.generateFreeTextBeat(
                story = story,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
                previousBeats = previousBeats,
                userInput = userInput
            )

            result.fold(
                onSuccess = { nextBeat ->
                    storyBeatRepository.insertBeat(nextBeat)
                    TextSubmissionResult(
                        userMessage = userMessage,
                        nextBeat = nextBeat
                    )
                },
                onFailure = { error ->
                    TextSubmissionResult(
                        userMessage = userMessage,
                        error = error
                    )
                }
            )
        } catch (e: Exception) {
            TextSubmissionResult(
                userMessage = userMessage,
                error = e
            )
        }
    }
}
