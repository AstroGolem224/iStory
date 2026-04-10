@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
package com.storybuilder.feature.chatplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.InputMode
import com.storybuilder.domain.model.SenderType
import com.storybuilder.core.ui.theme.AetheriaDeepSpace
import com.storybuilder.core.ui.theme.GenreThemedBackground
import com.storybuilder.feature.chatplayer.components.NarratorBubble
import com.storybuilder.feature.chatplayer.components.SystemMessage
import com.storybuilder.feature.chatplayer.components.UserBubble
import com.storybuilder.feature.optionselection.OptionCards
import com.storybuilder.feature.textinput.TextInputBar

@Composable
fun ChatPlayerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ChatPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to bottom when messages change
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Auto-scroll when keyboard appears
    LaunchedEffect(uiState.isKeyboardVisible) {
        if (uiState.isKeyboardVisible && uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = Modifier
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

                    // Show loading indicator at the bottom when generating
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        color = Color.Black.copy(alpha = 0.4f) // Glassy input section
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Mode Toggle
            InputModeToggle(
                currentMode = inputMode,
                onModeChange = onModeChange
            )

            Divider()

            // Input Content based on mode
            AnimatedContent(
                targetState = inputMode,
                label = "input_mode_content",
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } togetherWith
                    fadeOut() + slideOutVertically { it / 2 }
                }
            ) { mode ->
                when (mode) {
                    InputMode.SUGGESTED_OPTIONS -> {
                        // Show option cards if available and not loading
                        AnimatedVisibility(
                            visible = currentOptions.isNotEmpty() && !isLoading,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
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
                contentDescription = null,
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
