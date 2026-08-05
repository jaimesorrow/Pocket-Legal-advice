package com.pocketlegal.advice.data.repository

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.core.util.safeApiCall
import com.pocketlegal.advice.data.local.dao.ChatMessageDao
import com.pocketlegal.advice.data.local.entity.ChatMessageEntity
import com.pocketlegal.advice.data.model.ChatMessage
import com.pocketlegal.advice.data.model.MessageRole
import com.pocketlegal.advice.data.remote.AiApiService
import com.pocketlegal.advice.data.remote.AiChatRequest
import com.pocketlegal.advice.data.remote.AiMessage
import com.pocketlegal.advice.domain.repository.IAiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val aiApiService: AiApiService,
    private val chatMessageDao: ChatMessageDao
) : IAiRepository {

    override fun getMessages(sessionId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun sendMessage(
        sessionId: String,
        history: List<ChatMessage>
    ): NetworkResult<ChatMessage> {
        val aiMessages = history.map { AiMessage(role = it.role.name.lowercase(), content = it.content) }
        val result = safeApiCall {
            aiApiService.sendMessage(AiChatRequest(messages = aiMessages))
        }
        return when (result) {
            is NetworkResult.Success -> {
                val content = result.data.choices.firstOrNull()?.message?.content ?: ""
                val response = ChatMessage(
                    id = result.data.id,
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    content = content
                )
                chatMessageDao.insertMessage(response.toEntity())
                NetworkResult.Success(response)
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun clearSession(sessionId: String) {
        chatMessageDao.deleteSession(sessionId)
    }

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role.name,
        content = content,
        timestamp = timestamp,
        isLoading = isLoading
    )

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        sessionId = sessionId,
        role = MessageRole.valueOf(role),
        content = content,
        timestamp = timestamp,
        isLoading = isLoading
    )
}
