package com.storybuilder.feature.optionselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaNeonPurple
import com.storybuilder.core.ui.theme.AetheriaTextMain
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

@Composable
fun OptionCards(
    options: List<String>,
    onOptionSelected: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose your action:",
            style = MaterialTheme.typography.titleMedium,
            color = AetheriaNeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        options.forEachIndexed { index, option ->
            OptionCard(
                optionIndex = index,
                option = option,
                onClick = { onOptionSelected(index, option) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionCard(
    optionIndex: Int,
    option: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = AetheriaNeonPurple.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, AetheriaNeonPurple),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = "${optionIndex + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AetheriaNeonPurple,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                color = AetheriaTextMain
            )
        }
    }
}
