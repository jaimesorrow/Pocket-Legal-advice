package com.pocketlegal.advice.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.ChatMessage
import com.pocketlegal.advice.data.model.MessageRole
import com.pocketlegal.advice.domain.repository.IAiRepository
import com.pocketlegal.advice.domain.usecase.ai.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val aiRepository: IAiRepository
) : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        aiRepository.getMessages(sessionId).onEach { messages ->
            _uiState.update { it.copy(messages = messages) }
        }.launchIn(viewModelScope)
    }

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text, error = null) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = MessageRole.USER,
            content = text
        )
        viewModelScope.launch {
            aiRepository.saveMessage(userMessage)
            _uiState.update { it.copy(inputText = "", isLoading = true, error = null) }

            val history = _uiState.value.messages
            when (val result = sendMessageUseCase(sessionId, history)) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { aiRepository.clearSession(sessionId) }
    }
}
