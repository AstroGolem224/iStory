package com.storybuilder.core.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storybuilder.core.ui.theme.AetheriaGlassBase
import com.storybuilder.core.ui.theme.AetheriaGlassBorder

/**
 * A glassmorphic surface with frosted blur effect.
 * Note: Blur effect requires Android 12+ (API 31).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(blurRadius)
                } else {
                    Modifier
                }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AetheriaGlassBase.copy(alpha = 0.4f),
                        AetheriaGlassBase.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AetheriaGlassBorder.copy(alpha = 0.5f),
                        Color.Transparent,
                        AetheriaGlassBorder.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
    ) {
        // We reuse the same shape for the content box to ensure padding doesn't break the glass look
        Box(modifier = Modifier.padding(1.dp)) {
            content()
        }
    }
}
