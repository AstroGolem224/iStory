package com.storybuilder.feature.charactercreate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaNeonPurple
import com.storybuilder.domain.model.CharacterArchetype
import com.storybuilder.domain.model.CharacterTrait

@Composable
fun CharacterPreviewCard(
    name: String,
    archetype: CharacterArchetype?,
    traits: List<CharacterTrait>
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder with glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = AetheriaNeonPurple.copy(alpha = 0.1f),
                    border = BorderStroke(2.dp, AetheriaNeonPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = AetheriaNeonPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (archetype != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = archetype.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetheriaNeonCyan
                )
            }

            if (traits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (trait in traits.take(3)) {
                        TraitChip(trait = trait)
                    }
                }
            }
        }
    }
}

@Composable
fun TraitChip(trait: CharacterTrait) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = trait.displayName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
