package com.storybuilder.feature.userdashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.VoiceProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApiKey: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // AI Provider Section
            SettingsSection(title = "AI Provider") {
                // Active provider card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Provider",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.activeProviderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Model: ${uiState.activeModelName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Change provider button
                ListItem(
                    headlineContent = { Text("Change AI Provider") },
                    supportingContent = { Text("Configure providers and select active one") },
                    leadingContent = { Icon(Icons.Default.Build, null) },
                    trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                    modifier = Modifier.clickable { onNavigateToApiKey() }
                )
                
                // Show configured providers
                if (uiState.configuredProviders.isNotEmpty()) {
                    Text(
                        text = "Configured Providers:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    uiState.configuredProviders.forEach { provider ->
                        val isActive = provider == uiState.activeProvider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isActive) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.Check
                                },
                                contentDescription = null,
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.getProviderDisplayName(provider),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(Active)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Divider()

            // Legacy API Key Section (for backward compatibility, hidden if using new system)
            if (uiState.showLegacyApiKey) {
                SettingsSection(title = "API Configuration") {
                    ListItem(
                        headlineContent = { Text("Gemini API Key") },
                        supportingContent = { Text(uiState.apiKeyMasked) },
                        leadingContent = { Icon(Icons.Default.Build, null) },
                        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { viewModel.showApiKeyDialog() }
                    )
                }

                Divider()
            }

            // Default Mode Section
            SettingsSection(title = "Default Preferences") {
                ListItem(
                    headlineContent = { Text("Suggest Options by Default") },
                    supportingContent = { Text("Automatically show AI-generated choices") },
                    leadingContent = { Icon(Icons.Default.Create, null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.defaultSuggestOptions,
                            onCheckedChange = { viewModel.setDefaultSuggestOptions(it) }
                        )
                    }
                )
            }

            Divider()

            // TTS Settings
            SettingsSection(title = "Text-to-Speech") {
                ListItem(
                    headlineContent = { Text("Enable TTS") },
                    supportingContent = { Text("Read narrator messages aloud") },
                    leadingContent = { Icon(Icons.Default.Menu, null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.ttsEnabled,
                            onCheckedChange = { viewModel.setTtsEnabled(it) }
                        )
                    }
                )

                if (uiState.ttsEnabled) {
                    ListItem(
                        headlineContent = { Text("Voice Profile") },
                        supportingContent = { 
                            Text(
                                viewModel.getVoiceProfiles()
                                    .find { it.id == uiState.selectedVoiceProfile }
                                    ?.name ?: "Default"
                            ) 
                        },
                        modifier = Modifier.clickable { /* Show voice selection dialog */ }
                    )

                    // Voice Profile Selection
                    VoiceProfileSelector(
                        selectedProfile = uiState.selectedVoiceProfile,
                        profiles = viewModel.getVoiceProfiles(),
                        onProfileSelected = { viewModel.setVoiceProfile(it.id) }
                    )
                }
            }

            Divider()

            // AI Creativity
            SettingsSection(title = "AI Creativity") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Temperature: ${String.format("%.1f", uiState.aiTemperature)}")
                        Text(
                            when {
                                uiState.aiTemperature < 0.3 -> "Focused"
                                uiState.aiTemperature < 0.7 -> "Balanced"
                                else -> "Creative"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.aiTemperature,
                        onValueChange = { viewModel.setAiTemperature(it) },
                        valueRange = 0.0f..1.0f,
                        steps = 9
                    )
                    Text(
                        "Lower values produce more focused, predictable stories. Higher values increase creativity and randomness.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider()

            // Chat Theme
            SettingsSection(title = "Appearance") {
                ListItem(
                    headlineContent = { Text("Dark Mode") },
                    leadingContent = { Icon(Icons.Default.Settings, null) },
                    trailingContent = {
                        DarkModeSelector(
                            currentMode = uiState.darkMode,
                            onModeSelected = { viewModel.setDarkMode(it) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Genre-Themed UI") },
                    supportingContent = { Text("Apply theme colors based on story genre") },
                    trailingContent = {
                        Switch(
                            checked = uiState.genreThemeEnabled,
                            onCheckedChange = { viewModel.setGenreThemeEnabled(it) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Haptic Feedback") },
                    supportingContent = { Text("Vibration on interactions") },
                    trailingContent = {
                        Switch(
                            checked = uiState.hapticEnabled,
                            onCheckedChange = { viewModel.setHapticEnabled(it) }
                        )
                    }
                )
            }

            Divider()

            // Data Management
            SettingsSection(title = "Data Management") {
                ListItem(
                    headlineContent = { Text("Clear All Data") },
                    supportingContent = { Text("Delete all stories and settings") },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Delete, 
                            null,
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    modifier = Modifier.clickable { viewModel.showClearDataConfirmation() }
                )
            }

            Divider()

            // About
            SettingsSection(title = "About") {
                ListItem(
                    headlineContent = { Text("App Version") },
                    supportingContent = { Text("StoryBuilder ${SettingsViewModel.VERSION_NAME}") },
                    leadingContent = { Icon(Icons.Default.Info, null) }
                )

                ListItem(
                    headlineContent = { Text("AI Provider") },
                    supportingContent = { Text(uiState.activeProviderName) },
                    leadingContent = { Icon(Icons.Default.Star, null) }
                )
            }
        }
    }

    // API Key Dialog (legacy)
    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = uiState.apiKey,
            onDismiss = { viewModel.hideApiKeyDialog() },
            onConfirm = { newKey ->
                viewModel.updateApiKey(newKey)
                viewModel.hideApiKeyDialog()
                scope.launch {
                    snackbarHostState.showSnackbar("API Key updated successfully")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (uiState.showClearDataConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataConfirmation() },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Clear All Data?") },
            text = { 
                Text("This will permanently delete all your stories, characters, and settings. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData {
                            scope.launch {
                                snackbarHostState.showSnackbar("All data cleared")
                            }
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun VoiceProfileSelector(
    selectedProfile: String,
    profiles: List<VoiceProfile>,
    onProfileSelected: (VoiceProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .selectableGroup()
    ) {
        profiles.forEach { profile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (profile.id == selectedProfile),
                        onClick = { onProfileSelected(profile) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (profile.id == selectedProfile),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkModeSelector(
    currentMode: String,
    onModeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val modes = listOf(
        "system" to "System",
        "light" to "Light",
        "dark" to "Dark"
    )

    TextButton(onClick = { expanded = !expanded }) {
        Text(modes.find { it.first == currentMode }?.second ?: "System")
    }

    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    modes.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onModeSelected(value)
                                    expanded = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentMode == value,
                                onClick = {
                                    onModeSelected(value)
                                    expanded = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update API Key") },
        text = {
            Column {
                Text(
                    "Enter your API key. You can get one from your AI provider.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
