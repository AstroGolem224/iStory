package com.storybuilder.data.local.repository

import com.storybuilder.data.local.converter.Converters
import com.storybuilder.data.local.dao.ChatMessageDao
import com.storybuilder.data.local.entity.ChatMessageEntity
import com.storybuilder.domain.model.ChatMessage
import com.storybuilder.domain.model.MessageMetadata
import com.storybuilder.domain.model.SenderType
import com.storybuilder.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatMessageRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ChatMessageRepository {

    private val converters = Converters()

    override fun getMessagesForStory(storyId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForStory(storyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun deleteMessage(messageId: String) {
        chatMessageDao.deleteMessage(messageId)
    }

    override suspend fun deleteMessagesForStory(storyId: String) {
        chatMessageDao.deleteMessagesForStory(storyId)
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            storyId = storyId,
            senderType = SenderType.valueOf(senderType),
            content = content,
            timestamp = timestamp,
            metadata = if (suggestedOptionsJson != null) {
                MessageMetadata(
                    suggestedOptions = converters.fromStringList(suggestedOptionsJson),
                    selectedOptionIndex = selectedOptionIndex
                )
            } else null
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            storyId = storyId,
            senderType = senderType.name,
            content = content,
            timestamp = timestamp,
            suggestedOptionsJson = converters.toStringList(metadata?.suggestedOptions),
            selectedOptionIndex = metadata?.selectedOptionIndex
        )
    }
}
