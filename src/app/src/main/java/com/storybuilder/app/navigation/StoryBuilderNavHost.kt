package com.storybuilder.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.storybuilder.app.data.storage.SecureApiKeyStorage
import com.storybuilder.app.screens.agegate.AgeGateScreen
import com.storybuilder.app.screens.agegate.AgeGateViewModel
import com.storybuilder.app.screens.apiprovider.ApiProviderSetupScreen
import com.storybuilder.feature.charactercreate.CharacterCreateScreen
import com.storybuilder.feature.chatplayer.ChatPlayerScreen
import com.storybuilder.feature.genres.GenreSelectScreen
import com.storybuilder.feature.storylibrary.StoryLibraryScreen
import com.storybuilder.feature.userdashboard.SettingsScreen

@Composable
fun StoryBuilderNavHost(
    navController: NavHostController,
    secureApiKeyStorage: SecureApiKeyStorage,
    ageGateViewModel: AgeGateViewModel = hiltViewModel()
) {
    var startDestination by remember { mutableStateOf<String?>(null) }
    var hasAgeConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check age confirmation
        ageGateViewModel.hasConfirmedAge { confirmed ->
            hasAgeConfirmation = confirmed
        }
    }

    LaunchedEffect(hasAgeConfirmation) {
        startDestination = when {
            !hasAgeConfirmation -> Screen.AgeGate.route
            !secureApiKeyStorage.hasAnyApiKey() -> Screen.ApiProviderSetup.route
            else -> Screen.StoryLibrary.route
        }
    }

    startDestination?.let { destination ->
        NavHost(
            navController = navController,
            startDestination = destination
        ) {
            composable(Screen.AgeGate.route) {
                AgeGateScreen(
                    onAgeConfirmed = {
                        navController.navigate(Screen.ApiProviderSetup.route) {
                            popUpTo(Screen.AgeGate.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ApiProviderSetup.route) {
                ApiProviderSetupScreen(
                    onSetupComplete = {
                        navController.navigate(Screen.StoryLibrary.route) {
                            popUpTo(Screen.ApiProviderSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            // Legacy route - redirects to new multi-provider setup
            composable(Screen.ApiKeySetup.route) {
                ApiProviderSetupScreen(
                    onSetupComplete = {
                        navController.navigate(Screen.StoryLibrary.route) {
                            popUpTo(Screen.ApiKeySetup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.StoryLibrary.route) {
                StoryLibraryScreen(
                    onStorySelected = { storyId ->
                        navController.navigate(Screen.ChatPlayer.createRoute(storyId))
                    },
                    onCreateNewStory = {
                        navController.navigate(Screen.GenreSelect.route)
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToApiKey = {
                        navController.navigate(Screen.ApiProviderSetup.route)
                    }
                )
            }

            composable(Screen.GenreSelect.route) {
                GenreSelectScreen(
                    onGenreSelected = { genre, darknessLevel, pacing ->
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("selectedGenre", genre)
                            set("darknessLevel", darknessLevel)
                            set("pacing", pacing.name)
                        }
                        navController.navigate(Screen.CharacterCreate.route)
                    }
                )
            }

            composable(Screen.CharacterCreate.route) {
                CharacterCreateScreen(
                    onStoryCreated = { storyId ->
                        navController.navigate(Screen.ChatPlayer.createRoute(storyId)) {
                            popUpTo(Screen.GenreSelect.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ChatPlayer.route,
                arguments = listOf(
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
                ChatPlayerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
