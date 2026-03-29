package com.storybuilder.domain.repository

import com.storybuilder.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getAllStories(): Flow<List<Story>>
    fun getStoryById(storyId: String): Flow<Story?>
    suspend fun insertStory(story: Story)
    suspend fun updateStory(story: Story)
    suspend fun deleteStory(storyId: String)
}
