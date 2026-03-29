package com.storybuilder.domain.usecase

import com.storybuilder.domain.model.Character
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportStoryUseCase @Inject constructor() {

    operator fun invoke(
        story: Story,
        genre: Genre?,
        character: Character?,
        beats: List<StoryBeat>
    ): ExportResult {
        val markdown = buildMarkdown(story, genre, character, beats)
        return ExportResult(
            markdown = markdown,
            fileName = generateFileName(story)
        )
    }

    private fun buildMarkdown(
        story: Story,
        genre: Genre?,
        character: Character?,
        beats: List<StoryBeat>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val createdDate = dateFormat.format(Date(story.createdAt))

        val sb = StringBuilder()

        // Title and metadata
        sb.appendLine("# ${story.title}")
        sb.appendLine()
        sb.appendLine("**Genre:** ${genre?.name ?: "Unknown"} | **Darkness:** ${story.darknessLevel}/10 | **Created:** $createdDate")
        
        if (character != null) {
            sb.appendLine()
            sb.appendLine("## Character")
            sb.appendLine("**Name:** ${character.name}")
            sb.appendLine("**Archetype:** ${character.archetype}")
            if (character.traits.isNotEmpty()) {
                sb.appendLine("**Traits:** ${character.traits.joinToString(", ")}")
            }
            if (!character.backstory.isNullOrBlank()) {
                sb.appendLine()
                sb.appendLine("**Backstory:**")
                sb.appendLine(character.backstory)
            }
        }
        
        sb.appendLine()
        sb.appendLine("---")

        // Story beats
        val sortedBeats = beats.sortedBy { it.sequenceOrder }
        
        sortedBeats.forEachIndexed { index, beat ->
            sb.appendLine()
            sb.appendLine("## Chapter ${index + 1}")
            sb.appendLine()
            sb.appendLine(beat.narratorText)
            
            when {
                beat.freeTextInput != null -> {
                    sb.appendLine()
                    sb.appendLine("**You wrote:** ${beat.freeTextInput}")
                }
                beat.selectedOptionIndex != null && beat.selectedOptionIndex >= 0 -> {
                    beat.suggestedOptions?.getOrNull(beat.selectedOptionIndex)?.let { option ->
                        sb.appendLine()
                        sb.appendLine("**You chose:** $option")
                    }
                }
            }
            
            sb.appendLine()
            sb.appendLine("---")
        }

        sb.appendLine()
        sb.appendLine("*Exported from Story Builder*")

        return sb.toString()
    }

    private fun generateFileName(story: Story): String {
        val safeTitle = story.title.replace(Regex("[^a-zA-Z0-9\\s]"), "").replace(Regex("\\s+"), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${safeTitle}_$timestamp.md"
    }
}

data class ExportResult(
    val markdown: String,
    val fileName: String
)
