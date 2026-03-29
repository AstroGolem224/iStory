package com.storybuilder.domain.model

data class Genre(
    val id: String,
    val name: String,
    val description: String,
    val toneGuidelines: String,
    val themeColor: String,
    val iconAsset: String
) {
    companion object {
        fun getDefaultGenres(): List<Genre> = listOf(
            Genre(
                id = "horror",
                name = "Horror",
                description = "Cosmic dread, psychological terror",
                toneGuidelines = "Create an atmosphere of dread and unease. Use sensory details that evoke fear. " +
                    "Emphasize the unknown, isolation, and the fragility of human existence. " +
                    "Hints of cosmic horror and things beyond human comprehension.",
                themeColor = "#8B0000",
                iconAsset = "ic_horror"
            ),
            Genre(
                id = "fantasy",
                name = "Fantasy",
                description = "High magic, epic quests",
                toneGuidelines = "Emphasize wonder, magic, and heroism. Create rich, immersive world-building. " +
                    "Include elements of the extraordinary and mythical. Balance light and dark elements.",
                themeColor = "#4B0082",
                iconAsset = "ic_fantasy"
            ),
            Genre(
                id = "scifi",
                name = "Sci-Fi",
                description = "Future tech, space exploration",
                toneGuidelines = "Focus on technological advancement, space exploration, and futuristic societies. " +
                    "Explore themes of humanity's place in the universe. Use scientific concepts creatively.",
                themeColor = "#0066CC",
                iconAsset = "ic_scifi"
            ),
            Genre(
                id = "thriller",
                name = "Thriller",
                description = "Crime, mystery, suspense",
                toneGuidelines = "Build tension and suspense. Keep stakes high and time pressure real. " +
                    "Use twists and revelations. Focus on psychological tension and cat-and-mouse dynamics.",
                themeColor = "#2F4F4F",
                iconAsset = "ic_thriller"
            ),
            Genre(
                id = "adventure",
                name = "Adventure",
                description = "Exploration, survival",
                toneGuidelines = "Emphasize action, discovery, and overcoming challenges. Create vivid exotic locations. " +
                    "Focus on resourcefulness and courage. Include elements of danger and exploration.",
                themeColor = "#228B22",
                iconAsset = "ic_adventure"
            ),
            Genre(
                id = "romance",
                name = "Romance",
                description = "Relationships, emotional journeys",
                toneGuidelines = "Focus on emotional depth and character relationships. Build meaningful connections. " +
                    "Explore themes of love, trust, and personal growth. Balance conflict with tender moments.",
                themeColor = "#FF69B4",
                iconAsset = "ic_romance"
            )
        )
    }
}
