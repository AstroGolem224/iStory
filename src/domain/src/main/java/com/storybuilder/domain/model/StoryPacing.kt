package com.storybuilder.domain.model

enum class StoryPacing(val displayName: String, val description: String) {
    SLOW("Slow", "Deep exploration, rich descriptions, gradual development"),
    MEDIUM("Medium", "Balanced pace with mix of action and reflection"),
    FAST("Fast", "Quick progression, action-focused, rapid developments");

    companion object {
        fun fromId(id: String): StoryPacing =
            entries.find { it.name.equals(id, ignoreCase = true) } ?: MEDIUM
    }
}
