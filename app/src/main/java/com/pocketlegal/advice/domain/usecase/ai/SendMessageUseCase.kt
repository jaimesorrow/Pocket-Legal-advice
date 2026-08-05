package com.pocketlegal.advice.domain.usecase.ai

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.ChatMessage
import com.pocketlegal.advice.domain.repository.IAiRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val aiRepository: IAiRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        history: List<ChatMessage>
    ): NetworkResult<ChatMessage> = aiRepository.sendMessage(sessionId, history)
}
