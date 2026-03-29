package com.storybuilder.domain.usecase

import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.StoryAIRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import javax.inject.Inject

class GenerateNextBeatUseCase @Inject constructor(
    private val storyAIRepository: StoryAIRepository,
    private val storyBeatRepository: StoryBeatRepository
) {
    suspend operator fun invoke(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): Result<StoryBeat> {
        return storyAIRepository.generateNextBeat(
            story = story,
            characterName = characterName,
            characterDescription = characterDescription,
            genreName = genreName,
            genreToneGuidelines = genreToneGuidelines,
            previousBeats = previousBeats
        ).onSuccess { beat ->
            storyBeatRepository.insertBeat(beat)
        }
    }

    suspend fun generateOpeningBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String
    ): Result<StoryBeat> {
        return storyAIRepository.generateOpeningBeat(
            story = story,
            characterName = characterName,
            characterDescription = characterDescription,
            genreName = genreName,
            genreToneGuidelines = genreToneGuidelines
        ).onSuccess { beat ->
            storyBeatRepository.insertBeat(beat)
        }
    }
}
