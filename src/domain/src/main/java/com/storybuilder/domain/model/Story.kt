package com.storybuilder.domain.model

import java.util.UUID

data class Story(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val genreId: String,
    val characterId: String,
    val darknessLevel: Int = 5,
    val pacing: StoryPacing = StoryPacing.MEDIUM,
    val suggestOptionsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val isComplete: Boolean = false
)
