package com.storybuilder.data.local.repository

import com.storybuilder.data.local.converter.Converters
import com.storybuilder.data.local.dao.StoryBeatDao
import com.storybuilder.data.local.entity.StoryBeatEntity
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.StoryBeatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoryBeatRepositoryImpl @Inject constructor(
    private val storyBeatDao: StoryBeatDao
) : StoryBeatRepository {

    private val converters = Converters()

    override fun getBeatsForStory(storyId: String): Flow<List<StoryBeat>> {
        return storyBeatDao.getBeatsForStory(storyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBeat(beat: StoryBeat) {
        storyBeatDao.insertBeat(beat.toEntity())
    }

    override suspend fun updateBeat(beat: StoryBeat) {
        storyBeatDao.updateBeat(beat.toEntity())
    }

    override suspend fun deleteBeat(beatId: String) {
        storyBeatDao.deleteBeat(beatId)
    }

    override suspend fun deleteBeatsForStory(storyId: String) {
        storyBeatDao.deleteBeatsForStory(storyId)
    }

    private fun StoryBeatEntity.toDomain(): StoryBeat {
        return StoryBeat(
            id = id,
            storyId = storyId,
            sequenceOrder = sequenceOrder,
            narratorText = narratorText,
            suggestedOptions = converters.fromStringList(suggestedOptionsJson),
            selectedOptionIndex = selectedOptionIndex,
            freeTextInput = freeTextInput,
            createdAt = createdAt
        )
    }

    private fun StoryBeat.toEntity(): StoryBeatEntity {
        return StoryBeatEntity(
            id = id,
            storyId = storyId,
            sequenceOrder = sequenceOrder,
            narratorText = narratorText,
            suggestedOptionsJson = converters.toStringList(suggestedOptions),
            selectedOptionIndex = selectedOptionIndex,
            freeTextInput = freeTextInput,
            createdAt = createdAt
        )
    }
}
