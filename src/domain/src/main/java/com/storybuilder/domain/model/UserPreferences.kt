package com.storybuilder.domain.model

data class UserPreferences(
    val userId: String,
    val suggestOptionsEnabled: Boolean = true,
    val defaultDarknessLevel: Int = 5,
    val ttsEnabled: Boolean = true
)
