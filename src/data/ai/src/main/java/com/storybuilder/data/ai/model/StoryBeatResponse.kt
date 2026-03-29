package com.storybuilder.data.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Expected JSON response from AI for story beat generation
 */
data class StoryBeatResponse(
    @SerializedName("narratorText")
    val narratorText: String,
    @SerializedName("suggestedOptions")
    val suggestedOptions: List<String>? = null,
    @SerializedName("choicePrompt")
    val choicePrompt: String? = null,
    @SerializedName("storyComplete")
    val storyComplete: Boolean = false
)
