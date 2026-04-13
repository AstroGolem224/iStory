package com.storybuilder.feature.chatplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.components.GlassSurface
import com.storybuilder.core.ui.theme.AetheriaNeonCyan
import com.storybuilder.core.ui.theme.AetheriaTextMain
import com.storybuilder.core.ui.theme.NarratorBubbleColor

@Composable
fun NarratorBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        GlassSurface(
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Narrator",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetheriaNeonCyan.copy(alpha = 0.8f)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AetheriaTextMain,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
