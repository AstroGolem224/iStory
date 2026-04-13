package com.storybuilder.domain.usecase

import com.storybuilder.domain.model.Character
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.repository.CharacterRepository
import com.storybuilder.domain.repository.GenreRepository
import com.storybuilder.domain.repository.StoryRepository
import java.util.UUID
import javax.inject.Inject

class CreateNewStoryUseCase @Inject constructor(
    private val storyRepository: StoryRepository,
    private val characterRepository: CharacterRepository,
    private val genreRepository: GenreRepository
) {
    suspend operator fun invoke(
        genre: Genre,
        character: Character,
        darknessLevel: Int,
        pacing: String
    ): Result<String> {
        return try {
            val storyId = UUID.randomUUID().toString()
            val characterId = UUID.randomUUID().toString()
            
            // 1. Save character
            val newCharacter = character.copy(id = characterId)
            characterRepository.insertCharacter(newCharacter)
            
            // 2. Save genre (if not already present)
            genreRepository.insertGenre(genre)
            
            // 3. Create and save story
            val newStory = Story(
                id = storyId,
                title = "Adventure in ${genre.name}",
                genreId = genre.id,
                characterId = characterId,
                darknessLevel = darknessLevel,
                suggestOptionsEnabled = true
            )
            storyRepository.insertStory(newStory)
            
            Result.success(storyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
