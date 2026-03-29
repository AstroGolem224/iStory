package com.storybuilder.app.navigation

sealed class Screen(val route: String) {
    data object AgeGate : Screen("age_gate")
    data object ApiKeySetup : Screen("api_key_setup")
    data object ApiProviderSetup : Screen("api_provider_setup")
    data object StoryLibrary : Screen("story_library")
    data object GenreSelect : Screen("genre_select")
    data object CharacterCreate : Screen("character_create")
    data object Settings : Screen("settings")
    data object ChatPlayer : Screen("chat_player/{storyId}") {
        fun createRoute(storyId: String) = "chat_player/$storyId"
    }
}
