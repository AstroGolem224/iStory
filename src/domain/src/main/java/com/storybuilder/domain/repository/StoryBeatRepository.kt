package com.storybuilder.domain.repository

import com.storybuilder.domain.model.StoryBeat
import kotlinx.coroutines.flow.Flow

interface StoryBeatRepository {
    fun getBeatsForStory(storyId: String): Flow<List<StoryBeat>>
    suspend fun insertBeat(beat: StoryBeat)
    suspend fun updateBeat(beat: StoryBeat)
    suspend fun deleteBeat(beatId: String)
    suspend fun deleteBeatsForStory(storyId: String)
}
