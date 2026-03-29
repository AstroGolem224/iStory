package com.storybuilder.domain.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val storyId: String,
    val senderType: SenderType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: MessageMetadata? = null
)

enum class SenderType {
    NARRATOR,
    USER,
    SYSTEM
}

data class MessageMetadata(
    val suggestedOptions: List<String>? = null,
    val selectedOptionIndex: Int? = null,
    val freeTextInput: String? = null,
    val inputMode: InputMode? = null
)
