@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.storybuilder.feature.genreselect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaNeonPurple
import com.storybuilder.core.ui.theme.AetheriaTextMain
import com.storybuilder.core.ui.theme.GenreThemedBackground
import com.storybuilder.domain.model.Genre
import com.storybuilder.feature.genres.GenreSelectViewModel

@Composable
fun GenreSelectScreen(
    onGenreSelected: (String) -> Unit = {},
    viewModel: GenreSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadGenres()
    }

    GenreThemedBackground(genreId = uiState.selectedGenre?.id ?: "fantasy") {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Select Genre", color = Color.White) },
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
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Choose the mood of your adventure",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.genres) { genre ->
                        GenreCard(
                            genre = genre,
                            isSelected = uiState.selectedGenre?.id == genre.id,
                            onClick = { viewModel.selectGenre(genre) }
                        )
                    }
                }

                // Configuration Section (Glassy)
                if (uiState.selectedGenre != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Darkness Level", color = AetheriaTextMain)
                            Slider(
                                value = uiState.darknessLevel.toFloat(),
                                onValueChange = { viewModel.setDarknessLevel(it.toInt()) },
                                valueRange = 1f..10f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AetheriaNeonPurple,
                                    activeTrackColor = AetheriaNeonPurple.copy(alpha = 0.5f)
                                )
                            )
                            
                            Button(
                                onClick = { onGenreSelected(uiState.selectedGenre!!.id) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AetheriaNeonCyan, contentColor = Color.Black)
                            ) {
                                Text("Continue with ${uiState.selectedGenre!!.name}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    genre: Genre,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AetheriaNeonCyan else Color.Transparent
    
    GlassSurface(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Background icon or visual placeholder
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) AetheriaNeonCyan else Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = genre.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AetheriaNeonCyan.copy(alpha = 0.05f))
                )
            }
        }
    }
}
