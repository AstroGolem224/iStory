@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.storybuilder.feature.userdashboard

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaNeonPurple
import com.storybuilder.core.ui.theme.AetheriaTextMain
import com.storybuilder.core.ui.theme.GenreThemedBackground

@Composable
fun UserDashboardScreen(
    onNewStory: () -> Unit = {},
    onContinueStory: (String) -> Unit = {}
) {
    GenreThemedBackground(genreId = "fantasy") {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("iStory Dashboard", color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Welcome back, Storyteller",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }

                item {
                    ActionCard(
                        title = "Start New Adventure",
                        subtitle = "Let the AI guide your next epic",
                        icon = Icons.Default.Add,
                        accentColor = AetheriaNeonCyan,
                        onClick = onNewStory
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            label = "Stories",
                            value = "12",
                            icon = Icons.Default.AutoStories,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "HoursSpent",
                            value = "48",
                            icon = Icons.Default.History,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "Recent Stories",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Placeholder for recent stories
                items(3) { index ->
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { Text("The Lost Kingdom Part ${index + 1}", color = AetheriaTextMain) },
                            supportingContent = { Text("Last played 2 hours ago", color = Color.Gray) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = accentColor.copy(alpha = 0.8f),
            contentColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    GlassSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = AetheriaNeonPurple)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}
