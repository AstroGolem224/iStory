package com.storybuilder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "story_beats",
    indices = [Index("storyId")]
)
data class StoryBeatEntity(
    @PrimaryKey
    val id: String,
    val storyId: String,
    val sequenceOrder: Int,
    val narratorText: String,
    val suggestedOptionsJson: String? = null,
    val selectedOptionIndex: Int? = null,
    val freeTextInput: String? = null,
    val createdAt: Long
)
