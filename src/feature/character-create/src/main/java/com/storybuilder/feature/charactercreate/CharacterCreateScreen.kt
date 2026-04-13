@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.storybuilder.feature.charactercreate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaNeonPurple
import com.storybuilder.core.ui.theme.AetheriaTextMain
import com.storybuilder.core.ui.theme.GenreThemedBackground
import com.storybuilder.domain.model.Character
import com.storybuilder.domain.model.CharacterArchetype
import com.storybuilder.domain.model.CharacterTrait
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun CharacterCreateScreen(
    onStoryCreated: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CharacterCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var selectedArchetype by remember { mutableStateOf<CharacterArchetype?>(null) }
    var archetypeExpanded by remember { mutableStateOf(false) }
    var backstory by remember { mutableStateOf("") }
    val selectedTraits = remember { mutableStateListOf<CharacterTrait>() }

    val isValid = name.isNotBlank() && selectedArchetype != null && !uiState.isLoading

    GenreThemedBackground(genreId = uiState.genre?.id ?: "fantasy") {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Character Profile", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Character Preview Card
                CharacterPreviewCard(
                    name = name.ifBlank { "Unnamed Hero" },
                    archetype = selectedArchetype,
                    traits = selectedTraits
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Character Name", color = Color.Gray) },
                            placeholder = { Text("Enter a name...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AetheriaNeonCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AetheriaNeonCyan,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Archetype Dropdown
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        OutlinedTextField(
                            value = selectedArchetype?.displayName ?: "",
                            onValueChange = { },
                            label = { Text("Archetype", color = Color.Gray) },
                            placeholder = { Text("Select an archetype...", color = Color.Gray) },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AetheriaNeonCyan,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            trailingIcon = {
                                IconButton(onClick = { archetypeExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select archetype", tint = AetheriaNeonCyan)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        DropdownMenu(
                            expanded = archetypeExpanded,
                            onDismissRequest = { archetypeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f).background(Color(0xFF101018))
                        ) {
                            CharacterArchetype.entries.forEach { archetype ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = archetype.displayName, color = Color.White)
                                            Text(text = archetype.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedArchetype = archetype
                                        archetypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Personality Traits",
                            style = MaterialTheme.typography.titleMedium,
                            color = AetheriaNeonPurple,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CharacterTrait.entries.forEach { trait ->
                                val isSelected = selectedTraits.contains(trait)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            selectedTraits.remove(trait)
                                        } else if (selectedTraits.size < 4) {
                                            selectedTraits.add(trait)
                                        }
                                    },
                                    label = { Text(trait.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AetheriaNeonPurple.copy(alpha = 0.3f),
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.Gray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) AetheriaNeonPurple else Color.Gray.copy(alpha = 0.5f),
                                        selectedBorderColor = AetheriaNeonPurple,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp
                                    )
                                )
                            }
                        }
                        
                        Text(
                            text = "Select up to 4 traits",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Backstory Input
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = backstory,
                            onValueChange = { backstory = it },
                            label = { Text("Backstory (Optional)", color = Color.Gray) },
                            placeholder = { Text("Tell us about your character's past...", color = Color.Gray) },
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AetheriaNeonCyan,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedArchetype?.let { archetype ->
                            val character = Character(
                                name = name.trim(),
                                archetype = archetype.name,
                                traits = selectedTraits.map { it.displayName },
                                backstory = backstory.takeIf { it.isNotBlank() }
                            )
                            viewModel.createStory(character, onStoryCreated)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AetheriaNeonCyan,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Create Destiny", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
