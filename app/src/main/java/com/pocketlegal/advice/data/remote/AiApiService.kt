package com.pocketlegal.advice.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AiChatRequest(
    val model: String = "gpt-4o",
    val messages: List<AiMessage>,
    val temperature: Double = 0.7
)

data class AiMessage(
    val role: String,
    val content: String
)

data class AiChatResponse(
    val id: String,
    val choices: List<AiChoice>
)

data class AiChoice(
    val message: AiMessage,
    val finishReason: String?
)

interface AiApiService {

    @POST("v1/chat/completions")
    suspend fun sendMessage(@Body request: AiChatRequest): Response<AiChatResponse>
}
