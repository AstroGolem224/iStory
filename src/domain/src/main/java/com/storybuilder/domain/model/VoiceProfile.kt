package com.storybuilder.domain.model

/**
 * Represents a voice profile for TTS with specific characteristics
 */
data class VoiceProfile(
    val id: String,
    val name: String,
    val pitch: Float,
    val speechRate: Float,
    val description: String
) {
    companion object {
        /**
         * Deep, slow, ominous voice for horror
         */
        val HORROR = VoiceProfile(
            id = "horror",
            name = "Horror Narrator",
            pitch = 0.7f,
            speechRate = 0.75f,
            description = "Deep, slow, and ominous for horror stories"
        )

        /**
         * Warm, melodic voice for fantasy
         */
        val FANTASY = VoiceProfile(
            id = "fantasy",
            name = "Fantasy Bard",
            pitch = 1.0f,
            speechRate = 0.9f,
            description = "Warm and melodic for fantasy adventures"
        )

        /**
         * Neutral, precise voice for sci-fi
         */
        val SCIFI = VoiceProfile(
            id = "scifi",
            name = "Sci-Fi Announcer",
            pitch = 0.95f,
            speechRate = 1.0f,
            description = "Neutral and precise for sci-fi narratives"
        )

        /**
         * Tense, urgent voice for thriller
         */
        val THRILLER = VoiceProfile(
            id = "thriller",
            name = "Thriller Agent",
            pitch = 0.9f,
            speechRate = 1.1f,
            description = "Tense and urgent for thriller stories"
        )

        /**
         * Energetic, adventurous voice
         */
        val ADVENTURE = VoiceProfile(
            id = "adventure",
            name = "Adventure Guide",
            pitch = 1.05f,
            speechRate = 1.05f,
            description = "Energetic and adventurous"
        )

        /**
         * Soft, gentle voice for romance
         */
        val ROMANCE = VoiceProfile(
            id = "romance",
            name = "Romance Storyteller",
            pitch = 1.1f,
            speechRate = 0.85f,
            description = "Soft and gentle for romantic tales"
        )

        /**
         * Default voice profile
         */
        val DEFAULT = VoiceProfile(
            id = "default",
            name = "Default",
            pitch = 1.0f,
            speechRate = 1.0f,
            description = "Standard narration voice"
        )

        /**
         * Get voice profile for a specific genre
         */
        fun forGenre(genreId: String): VoiceProfile {
            return when (genreId.lowercase()) {
                "horror" -> HORROR
                "fantasy" -> FANTASY
                "scifi", "sci-fi" -> SCIFI
                "thriller" -> THRILLER
                "adventure" -> ADVENTURE
                "romance" -> ROMANCE
                else -> DEFAULT
            }
        }

        /**
         * Get all available voice profiles
         */
        fun allProfiles(): List<VoiceProfile> = listOf(
            DEFAULT, HORROR, FANTASY, SCIFI, THRILLER, ADVENTURE, ROMANCE
        )
    }
}
