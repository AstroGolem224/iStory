package com.storybuilder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index("storyId")]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val storyId: String,
    val senderType: String, // NARRATOR, USER, SYSTEM
    val content: String,
    val timestamp: Long,
    val suggestedOptions: List<String>? = null,
    val selectedOptionIndex: Int? = null
)
