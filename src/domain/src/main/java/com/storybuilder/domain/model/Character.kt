package com.storybuilder.domain.model

import java.util.UUID

data class Character(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val archetype: String,
    val traits: List<String>,
    val backstory: String?,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CharacterArchetype(val displayName: String, val description: String) {
    HERO("Hero", "Brave protagonist destined for greatness"),
    ROGUE("Rogue", "Cunning trickster with hidden depths"),
    SCHOLAR("Scholar", "Wise seeker of knowledge and truth"),
    WARRIOR("Warrior", "Skilled fighter driven by honor"),
    MYSTIC("Mystic", "Keeper of ancient secrets and magic"),
    EXPLORER("Explorer", "Curious adventurer seeking discovery"),
    SURVIVOR("Survivor", "Resilient individual overcoming adversity");

    companion object {
        fun fromId(id: String): CharacterArchetype = 
            entries.find { it.name.equals(id, ignoreCase = true) } ?: HERO
    }
}

enum class CharacterTrait(val displayName: String) {
    BRAVE("Brave"),
    CLEVER("Clever"),
    CAUTIOUS("Cautious"),
    AGGRESSIVE("Aggressive"),
    KIND("Kind"),
    CUNNING("Cunning"),
    LOYAL("Loyal"),
    INDEPENDENT("Independent"),
    CHARISMATIC("Charismatic"),
    STOIC("Stoic"),
    CURIOUS("Curious"),
    DETERMINED("Determined");

    companion object {
        fun fromDisplayName(name: String): CharacterTrait? =
            entries.find { it.displayName.equals(name, ignoreCase = true) }
    }
}
