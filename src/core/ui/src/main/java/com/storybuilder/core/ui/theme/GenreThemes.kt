package com.storybuilder.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

// Horror Colors - Dark red accents
private val HorrorPrimary = Color(0xFF8B0000)
private val HorrorSecondary = Color(0xFF4A0000)
private val HorrorTertiary = Color(0xFF660000)
private val HorrorBackground = Color(0xFF0D0D0D)
private val HorrorSurface = Color(0xFF1A0F0F)
private val HorrorOnPrimary = Color(0xFFFFE4E4)

// Fantasy Colors - Gold/amber
private val FantasyPrimary = Color(0xFFD4AF37)
private val FantasySecondary = Color(0xFF8B6914)
private val FantasyTertiary = Color(0xFFB8860B)
private val FantasyBackground = Color(0xFF1A1510)
private val FantasySurface = Color(0xFF2D2419)
private val FantasyOnPrimary = Color(0xFF2A1F00)

// Sci-Fi Colors - Cyan/blue
private val SciFiPrimary = Color(0xFF00CED1)
private val SciFiSecondary = Color(0xFF008B8B)
private val SciFiTertiary = Color(0xFF20B2AA)
private val SciFiBackground = Color(0xFF0A0A1A)
private val SciFiSurface = Color(0xFF101028)
private val SciFiOnPrimary = Color(0xFFE0FFFF)

// Thriller Colors - Noir black/white
private val ThrillerPrimary = Color(0xFFE0E0E0)
private val ThrillerSecondary = Color(0xFF808080)
private val ThrillerTertiary = Color(0xFF404040)
private val ThrillerBackground = Color(0xFF050505)
private val ThrillerSurface = Color(0xFF111111)
private val ThrillerOnPrimary = Color(0xFF000000)

// Adventure Colors - Earth tones
private val AdventurePrimary = Color(0xFF8B4513)
private val AdventureSecondary = Color(0xFF556B2F)
private val AdventureTertiary = Color(0xFFCD853F)
private val AdventureBackground = Color(0xFF1C1814)
private val AdventureSurface = Color(0xFF2A2420)
private val AdventureOnPrimary = Color(0xFFF5DEB3)

// Romance Colors - Rose/pink
private val RomancePrimary = Color(0xFFFF69B4)
private val RomanceSecondary = Color(0xFFFF1493)
private val RomanceTertiary = Color(0xFFFFC0CB)
private val RomanceBackground = Color(0xFF1A1218)
private val RomanceSurface = Color(0xFF2A1E24)
private val RomanceOnPrimary = Color(0xFFFFF0F5)

// Genre Color Schemes
val HorrorDarkColorScheme = darkColorScheme(
    primary = HorrorPrimary,
    secondary = HorrorSecondary,
    tertiary = HorrorTertiary,
    background = HorrorBackground,
    surface = HorrorSurface,
    onPrimary = HorrorOnPrimary,
    onBackground = Color(0xFFE8D5D5),
    onSurface = Color(0xFFE8D5D5)
)

val FantasyDarkColorScheme = darkColorScheme(
    primary = FantasyPrimary,
    secondary = FantasySecondary,
    tertiary = FantasyTertiary,
    background = FantasyBackground,
    surface = FantasySurface,
    onPrimary = FantasyOnPrimary,
    onBackground = Color(0xFFE8DCC8),
    onSurface = Color(0xFFE8DCC8)
)

val SciFiDarkColorScheme = darkColorScheme(
    primary = SciFiPrimary,
    secondary = SciFiSecondary,
    tertiary = SciFiTertiary,
    background = SciFiBackground,
    surface = SciFiSurface,
    onPrimary = SciFiOnPrimary,
    onBackground = Color(0xFFE0F7FA),
    onSurface = Color(0xFFE0F7FA)
)

val ThrillerDarkColorScheme = darkColorScheme(
    primary = ThrillerPrimary,
    secondary = ThrillerSecondary,
    tertiary = ThrillerTertiary,
    background = ThrillerBackground,
    surface = ThrillerSurface,
    onPrimary = ThrillerOnPrimary,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

val AdventureDarkColorScheme = darkColorScheme(
    primary = AdventurePrimary,
    secondary = AdventureSecondary,
    tertiary = AdventureTertiary,
    background = AdventureBackground,
    surface = AdventureSurface,
    onPrimary = AdventureOnPrimary,
    onBackground = Color(0xFFF5DEB3),
    onSurface = Color(0xFFF5DEB3)
)

val RomanceDarkColorScheme = darkColorScheme(
    primary = RomancePrimary,
    secondary = RomanceSecondary,
    tertiary = RomanceTertiary,
    background = RomanceBackground,
    surface = RomanceSurface,
    onPrimary = RomanceOnPrimary,
    onBackground = Color(0xFFFFF0F5),
    onSurface = Color(0xFFFFF0F5)
)

// Light variants
val HorrorLightColorScheme = lightColorScheme(
    primary = HorrorPrimary,
    secondary = HorrorSecondary,
    tertiary = HorrorTertiary,
    background = Color(0xFF2D1F1F),
    surface = Color(0xFF3D2F2F),
    onPrimary = HorrorOnPrimary
)

val FantasyLightColorScheme = lightColorScheme(
    primary = FantasyPrimary,
    secondary = FantasySecondary,
    tertiary = FantasyTertiary,
    background = Color(0xFFF5E6C8),
    surface = Color(0xFFFAF0E0),
    onPrimary = Color(0xFF2A1F00)
)

val SciFiLightColorScheme = lightColorScheme(
    primary = SciFiPrimary,
    secondary = SciFiSecondary,
    tertiary = SciFiTertiary,
    background = Color(0xFFE8F4F8),
    surface = Color(0xFFF0F8FF),
    onPrimary = Color(0xFF004D4D)
)

val ThrillerLightColorScheme = lightColorScheme(
    primary = Color(0xFF333333),
    secondary = ThrillerSecondary,
    tertiary = ThrillerTertiary,
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF)
)

val AdventureLightColorScheme = lightColorScheme(
    primary = AdventurePrimary,
    secondary = AdventureSecondary,
    tertiary = AdventureTertiary,
    background = Color(0xFFF5F0E8),
    surface = Color(0xFFFAF8F5),
    onPrimary = Color(0xFF3D2914)
)

val RomanceLightColorScheme = lightColorScheme(
    primary = RomancePrimary,
    secondary = RomanceSecondary,
    tertiary = RomanceTertiary,
    background = Color(0xFFFFF5F8),
    surface = Color(0xFFFFFAFC),
    onPrimary = Color(0xFF4A0E2E)
)

// Genre Chat Bubble Colors
object GenreChatColors {
    val HorrorNarrator = Color(0xFF3D1515)
    val HorrorUser = Color(0xFF5C1A1A)
    val HorrorOption = Color(0xFF2A0A0A)

    val FantasyNarrator = Color(0xFF3D3015)
    val FantasyUser = Color(0xFF5C4A1A)
    val FantasyOption = Color(0xFF2A200A)

    val SciFiNarrator = Color(0xFF0A1A3D)
    val SciFiUser = Color(0xFF1A2A5C)
    val SciFiOption = Color(0xFF0A102A)

    val ThrillerNarrator = Color(0xFF1A1A1A)
    val ThrillerUser = Color(0xFF2A2A2A)
    val ThrillerOption = Color(0xFF0A0A0A)

    val AdventureNarrator = Color(0xFF2D2419)
    val AdventureUser = Color(0xFF3D3429)
    val AdventureOption = Color(0xFF1A1410)

    val RomanceNarrator = Color(0xFF3D1529)
    val RomanceUser = Color(0xFF5C1A3D)
    val RomanceOption = Color(0xFF2A0A1A)
}

data class GenreTheme(
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme,
    val narratorBubbleColor: Color,
    val userBubbleColor: Color,
    val optionCardColor: Color,
    val accentColor: Color,
    val backgroundGradient: List<Color>
)

// Genre Themes
val HorrorTheme = GenreTheme(
    darkColorScheme = HorrorDarkColorScheme,
    lightColorScheme = HorrorLightColorScheme,
    narratorBubbleColor = GenreChatColors.HorrorNarrator,
    userBubbleColor = GenreChatColors.HorrorUser,
    optionCardColor = GenreChatColors.HorrorOption,
    accentColor = HorrorPrimary,
    backgroundGradient = listOf(HorrorBackground, Color(0xFF1A0000))
)

val FantasyTheme = GenreTheme(
    darkColorScheme = FantasyDarkColorScheme,
    lightColorScheme = FantasyLightColorScheme,
    narratorBubbleColor = GenreChatColors.FantasyNarrator,
    userBubbleColor = GenreChatColors.FantasyUser,
    optionCardColor = GenreChatColors.FantasyOption,
    accentColor = FantasyPrimary,
    backgroundGradient = listOf(FantasyBackground, Color(0xFF1A1200))
)

val SciFiTheme = GenreTheme(
    darkColorScheme = SciFiDarkColorScheme,
    lightColorScheme = SciFiLightColorScheme,
    narratorBubbleColor = GenreChatColors.SciFiNarrator,
    userBubbleColor = GenreChatColors.SciFiUser,
    optionCardColor = GenreChatColors.SciFiOption,
    accentColor = SciFiPrimary,
    backgroundGradient = listOf(SciFiBackground, Color(0xFF00001A))
)

val ThrillerTheme = GenreTheme(
    darkColorScheme = ThrillerDarkColorScheme,
    lightColorScheme = ThrillerLightColorScheme,
    narratorBubbleColor = GenreChatColors.ThrillerNarrator,
    userBubbleColor = GenreChatColors.ThrillerUser,
    optionCardColor = GenreChatColors.ThrillerOption,
    accentColor = ThrillerPrimary,
    backgroundGradient = listOf(ThrillerBackground, Color(0xFF050505))
)

val AdventureTheme = GenreTheme(
    darkColorScheme = AdventureDarkColorScheme,
    lightColorScheme = AdventureLightColorScheme,
    narratorBubbleColor = GenreChatColors.AdventureNarrator,
    userBubbleColor = GenreChatColors.AdventureUser,
    optionCardColor = GenreChatColors.AdventureOption,
    accentColor = AdventurePrimary,
    backgroundGradient = listOf(AdventureBackground, Color(0xFF1A1410))
)

val RomanceTheme = GenreTheme(
    darkColorScheme = RomanceDarkColorScheme,
    lightColorScheme = RomanceLightColorScheme,
    narratorBubbleColor = GenreChatColors.RomanceNarrator,
    userBubbleColor = GenreChatColors.RomanceUser,
    optionCardColor = GenreChatColors.RomanceOption,
    accentColor = RomancePrimary,
    backgroundGradient = listOf(RomanceBackground, Color(0xFF1A1018))
)

fun getGenreTheme(genreId: String): GenreTheme {
    return when (genreId.lowercase()) {
        "horror" -> HorrorTheme
        "fantasy" -> FantasyTheme
        "scifi", "sci-fi" -> SciFiTheme
        "thriller" -> ThrillerTheme
        "adventure" -> AdventureTheme
        "romance" -> RomanceTheme
        else -> FantasyTheme // Default
    }
}

// CompositionLocal for genre theme
val LocalGenreTheme = staticCompositionLocalOf { FantasyTheme }

@Composable
fun GenreThemedBackground(
    genreId: String,
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = getGenreTheme(genreId)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = theme.backgroundGradient,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY),
                    tileMode = TileMode.Clamp
                )
            )
    ) {
        content()
    }
}

@Composable
fun GenreThemedContent(
    genreId: String,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val theme = getGenreTheme(genreId)
    val colorScheme = if (darkTheme) theme.darkColorScheme else theme.lightColorScheme
    
    CompositionLocalProvider(LocalGenreTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
