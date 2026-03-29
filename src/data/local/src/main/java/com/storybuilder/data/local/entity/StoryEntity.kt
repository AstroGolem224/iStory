package com.storybuilder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val genreId: String,
    val characterId: String,
    val darknessLevel: Int,
    val pacing: String, // StoryPacing enum name
    val suggestOptionsEnabled: Boolean,
    val createdAt: Long,
    val lastPlayedAt: Long,
    val isComplete: Boolean
)
