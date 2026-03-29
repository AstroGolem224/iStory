package com.storybuilder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.storybuilder.app.data.storage.SecureApiKeyStorage
import com.storybuilder.app.navigation.StoryBuilderNavHost
import com.storybuilder.app.ui.theme.StoryBuilderTheme
import com.storybuilder.data.ai.di.ApiKeyProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var secureApiKeyStorage: SecureApiKeyStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize API key from secure storage
        initializeApiKey()
        
        enableEdgeToEdge()
        setContent {
            StoryBuilderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    StoryBuilderNavHost(
                        navController = navController,
                        secureApiKeyStorage = secureApiKeyStorage
                    )
                }
            }
        }
    }

    private fun initializeApiKey() {
        secureApiKeyStorage.getApiKey()?.let { apiKey ->
            ApiKeyProvider.setApiKey(apiKey)
        }
    }
}
