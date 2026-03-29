package com.storybuilder.domain.repository

import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import kotlinx.coroutines.flow.Flow

interface StoryAIRepository {
    /**
     * Generate the opening beat for a new story
     */
    suspend fun generateOpeningBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String
    ): Result<StoryBeat>

    /**
     * Generate the next beat based on selected option
     */
    suspend fun generateNextBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): Result<StoryBeat>

    /**
     * Generate the next beat based on free text user input
     */
    suspend fun generateFreeTextBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>,
        userInput: String
    ): Result<StoryBeat>

    /**
     * Stream of generation status for UI feedback
     */
    val isGenerating: Flow<Boolean>
    
    /**
     * Set the active AI provider
     */
    suspend fun setActiveProvider(provider: ApiProvider)
    
    /**
     * Get the currently active AI provider
     */
    fun getActiveProvider(): ApiProvider
    
    /**
     * Get flow of active provider changes
     */
    fun getActiveProviderFlow(): Flow<ApiProvider>
    
    /**
     * Test connection to a provider
     */
    suspend fun testConnection(provider: ApiProvider): Result<Boolean>
}
