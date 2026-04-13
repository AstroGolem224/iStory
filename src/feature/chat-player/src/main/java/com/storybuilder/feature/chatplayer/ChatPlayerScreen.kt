@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.storybuilder.feature.chatplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storybuilder.core.ui.theme.GenreThemedBackground
import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.InputMode
import com.storybuilder.domain.model.SenderType
import com.storybuilder.feature.chatplayer.components.*
import com.storybuilder.feature.optionselection.OptionCards
import com.storybuilder.feature.textinput.TextInputBar
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.*

@Composable
fun ChatPlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to bottom of chat when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding(),
        containerColor = Color.Transparent, // Make Scaffold transparent to see GenreThemedBackground
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.story?.title ?: "Story", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Black.copy(alpha = 0.5f)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        GenreThemedBackground(
            genreId = uiState.story?.genreId ?: "fantasy",
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.story == null && !uiState.isLoading && uiState.error != null) {
                    FullScreenError(
                        message = uiState.error!!,
                        onRetry = { viewModel.retry() },
                        onBack = onNavigateBack
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                MessageItem(message = message)
                            }

                            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                                item {
                                    EmptyChatPlaceholder()
                                }
                            }

                            // Show loading indicator at the bottom when generating
                            if (uiState.isLoading) {
                                item {
                                    NarratorBubbleSkeleton()
                                }
                            }
                        }

                        // Input Section with Mode Toggle
                        InputSection(
                            inputMode = uiState.inputMode,
                            currentOptions = uiState.currentOptions,
                            isLoading = uiState.isLoading,
                            onModeChange = { viewModel.setInputMode(it) },
                            onOptionSelected = { index -> viewModel.selectOption(index) },
                            onFreeTextSubmitted = { text -> 
                                viewModel.submitFreeText(text)
                            },
                            onVoiceInputRequested = { viewModel.onVoiceInputRequested() }
                        )
                    }
                    
                    // Voice Listening Overlay
                    AnimatedVisibility(
                        visible = uiState.isListening,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        VoiceListeningOverlay(
                            speechInput = uiState.speechInput,
                            onCancel = { viewModel.onVoiceInputRequested() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Wait, Traveler...",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Venture Again")
            }
            TextButton(onClick = onBack) {
                Text("Return to Library", color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun InputSection(
    inputMode: InputMode,
    currentOptions: List<String>,
    isLoading: Boolean,
    onModeChange: (InputMode) -> Unit,
    onOptionSelected: (Int) -> Unit,
    onFreeTextSubmitted: (String) -> Unit,
    onVoiceInputRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        InputModeToggle(
            currentMode = inputMode,
            onModeChange = onModeChange
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                // Input is disabled during generation
            } else {
                when (inputMode) {
                    InputMode.SUGGESTED_OPTIONS -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Transparent
                        ) {
                            OptionCards(
                                options = currentOptions,
                                onOptionSelected = { index, _ ->
                                    onOptionSelected(index)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    InputMode.FREE_TEXT -> {
                        TextInputBar(
                            onSend = { text ->
                                onFreeTextSubmitted(text)
                            },
                            onVoiceInputRequested = onVoiceInputRequested,
                            requestFocus = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputModeToggle(
    currentMode: InputMode,
    onModeChange: (InputMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Input Mode:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        TextButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = when (currentMode) {
                    InputMode.SUGGESTED_OPTIONS -> Icons.Default.DashboardCustomize
                    InputMode.FREE_TEXT -> Icons.Default.Edit
                },
                contentDescription = "Change Input Mode",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = when (currentMode) {
                    InputMode.SUGGESTED_OPTIONS -> "Suggested Options"
                    InputMode.FREE_TEXT -> "Free Text"
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Suggested Options") },
                leadingIcon = {
                    Icon(
                        Icons.Default.DashboardCustomize,
                        contentDescription = null
                    )
                },
                onClick = {
                    onModeChange(InputMode.SUGGESTED_OPTIONS)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Free Text") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                onClick = {
                    onModeChange(InputMode.FREE_TEXT)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage) {
    when (message.senderType) {
        SenderType.NARRATOR -> {
            NarratorBubble(
                text = message.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
        SenderType.USER -> {
            UserBubble(
                text = message.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
        SenderType.SYSTEM -> {
            SystemMessage(
                text = message.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun NarratorBubbleSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(20.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(20.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun EmptyChatPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your adventure is about to begin...",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun VoiceListeningOverlay(
    speechInput: String,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.8f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (speechInput.isBlank()) "Listening..." else speechInput,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}
