package com.pocketlegal.advice.domain.repository

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface IAiRepository {
    fun getMessages(sessionId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun sendMessage(sessionId: String, history: List<ChatMessage>): NetworkResult<ChatMessage>
    suspend fun clearSession(sessionId: String)
}
