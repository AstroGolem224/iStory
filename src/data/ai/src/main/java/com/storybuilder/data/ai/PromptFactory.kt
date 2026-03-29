package com.storybuilder.data.ai

import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.model.StoryPacing
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptFactory @Inject constructor() {

    fun buildOptionsModePrompt(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): String {
        val recentContext = buildRecentContext(previousBeats)
        val pacingInstruction = getPacingInstruction(story.pacing)
        
        return """
            You are a skilled ${genreName} storyteller.
            Protagonist: ${characterName}, ${characterDescription}
            Tone: ${story.darknessLevel}/10 darkness
            
            Genre Guidelines: ${genreToneGuidelines}
            
            Pacing: ${pacingInstruction}

            Generate immersive story content with exactly 3 options.

            Rules:
            - 3-5 atmospheric sentences for narrator text
            - Exactly 3 options, 5-10 words each, action-oriented
            - Options must genuinely branch in different directions
            - Respond in valid JSON only

            Context: ${recentContext}

            Continue the story.
        """.trimIndent()
    }

    fun buildOpeningBeatPrompt(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String
    ): String {
        val pacingInstruction = getPacingInstruction(story.pacing)
        
        return """
            You are a skilled ${genreName} storyteller.
            Protagonist: ${characterName}, ${characterDescription}
            Tone: ${story.darknessLevel}/10 darkness
            
            Genre Guidelines: ${genreToneGuidelines}
            
            Pacing: ${pacingInstruction}

            Generate the opening scene for this interactive story.

            Rules:
            - Start with an engaging hook
            - 3-5 atmospheric sentences for narrator text
            - Exactly 3 options, 5-10 words each, action-oriented
            - Options must genuinely branch in different directions
            - Respond in valid JSON only

            Begin the story.
        """.trimIndent()
    }

    fun buildFreeTextModePrompt(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>,
        userInput: String
    ): String {
        val recentContext = buildRecentContext(previousBeats)
        val pacingInstruction = getPacingInstruction(story.pacing)
        
        return """
            You are a skilled ${genreName} storyteller.
            Protagonist: ${characterName}, ${characterDescription}
            Tone: ${story.darknessLevel}/10 darkness
            
            Genre Guidelines: ${genreToneGuidelines}
            
            Pacing: ${pacingInstruction}

            The player will type their own actions. Adapt to ANY input creatively.
            No wrong answers - incorporate unexpected input interestingly.

            Rules:
            - 4-6 atmospheric sentences
            - End with open question for next action
            - Maintain narrative consistency
            - Respond in valid JSON only

            Context: ${recentContext}

            Player's action: "${userInput}"

            Continue the story adapting to the player's action.
        """.trimIndent()
    }

    private fun getPacingInstruction(pacing: StoryPacing): String {
        return when (pacing) {
            StoryPacing.SLOW -> "Take your time with rich descriptions and character development."
            StoryPacing.MEDIUM -> "Balance action and reflection at a moderate pace."
            StoryPacing.FAST -> "Keep the action moving quickly with rapid developments."
        }
    }

    private fun buildRecentContext(previousBeats: List<StoryBeat>): String {
        if (previousBeats.isEmpty()) {
            return "This is the beginning of the story."
        }

        val recentBeats = previousBeats.takeLast(3)
        return buildString {
            appendLine("Recent events:")
            recentBeats.forEach { beat ->
                appendLine("- ${beat.narratorText}")
                when {
                    beat.freeTextInput != null -> {
                        appendLine("  Player chose: ${beat.freeTextInput}")
                    }
                    else -> {
                        val selectedIndex = beat.selectedOptionIndex
                        if (selectedIndex != null && selectedIndex >= 0) {
                            beat.suggestedOptions?.getOrNull(selectedIndex)?.let { option ->
                                appendLine("  Player chose: $option")
                            }
                        }
                    }
                }
            }
        }.trim()
    }
}
