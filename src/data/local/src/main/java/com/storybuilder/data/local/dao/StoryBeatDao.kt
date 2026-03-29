package com.storybuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storybuilder.data.local.entity.StoryBeatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryBeatDao {
    @Query("SELECT * FROM story_beats WHERE storyId = :storyId ORDER BY sequenceOrder ASC")
    fun getBeatsForStory(storyId: String): Flow<List<StoryBeatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeat(beat: StoryBeatEntity)

    @Update
    suspend fun updateBeat(beat: StoryBeatEntity)

    @Query("DELETE FROM story_beats WHERE id = :beatId")
    suspend fun deleteBeat(beatId: String)

    @Query("DELETE FROM story_beats WHERE storyId = :storyId")
    suspend fun deleteBeatsForStory(storyId: String)
}
