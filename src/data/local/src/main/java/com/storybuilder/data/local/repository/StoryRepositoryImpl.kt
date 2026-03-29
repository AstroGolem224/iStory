package com.storybuilder.data.local.repository

import com.storybuilder.data.local.dao.StoryDao
import com.storybuilder.data.local.entity.StoryEntity
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryPacing
import com.storybuilder.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao
) : StoryRepository {

    override fun getAllStories(): Flow<List<Story>> {
        return storyDao.getAllStories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStoryById(storyId: String): Flow<Story?> {
        return storyDao.getStoryByIdFlow(storyId).map { it?.toDomain() }
    }

    override suspend fun insertStory(story: Story) {
        storyDao.insertStory(story.toEntity())
    }

    override suspend fun updateStory(story: Story) {
        storyDao.updateStory(story.toEntity())
    }

    override suspend fun deleteStory(storyId: String) {
        storyDao.deleteStory(storyId)
    }

    private fun StoryEntity.toDomain(): Story {
        return Story(
            id = id,
            title = title,
            genreId = genreId,
            characterId = characterId,
            darknessLevel = darknessLevel,
            pacing = StoryPacing.fromId(pacing),
            suggestOptionsEnabled = suggestOptionsEnabled,
            createdAt = createdAt,
            lastPlayedAt = lastPlayedAt,
            isComplete = isComplete
        )
    }

    private fun Story.toEntity(): StoryEntity {
        return StoryEntity(
            id = id,
            title = title,
            genreId = genreId,
            characterId = characterId,
            darknessLevel = darknessLevel,
            pacing = pacing.name,
            suggestOptionsEnabled = suggestOptionsEnabled,
            createdAt = createdAt,
            lastPlayedAt = lastPlayedAt,
            isComplete = isComplete
        )
    }
}
