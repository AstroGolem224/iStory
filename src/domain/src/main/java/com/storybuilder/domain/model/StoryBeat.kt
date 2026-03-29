package com.storybuilder.domain.model

import java.util.UUID

data class StoryBeat(
    val id: String = UUID.randomUUID().toString(),
    val storyId: String,
    val sequenceOrder: Int,
    val narratorText: String,
    val suggestedOptions: List<String>? = null,
    val selectedOptionIndex: Int? = null,
    val freeTextInput: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
