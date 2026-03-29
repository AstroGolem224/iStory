package com.storybuilder.domain.repository

import com.storybuilder.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    fun getMessagesForStory(storyId: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteMessagesForStory(storyId: String)
}
