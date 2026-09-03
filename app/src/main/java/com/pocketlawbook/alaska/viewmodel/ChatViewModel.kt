package com.pocketlawbook.alaska.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlawbook.alaska.data.repository.ChatRepository
import com.pocketlawbook.alaska.ui.model.ChatTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _turns = MutableStateFlow<List<ChatTurn>>(emptyList())
    val turns: StateFlow<List<ChatTurn>> = _turns.asStateFlow()

    private val _isAsking = MutableStateFlow(false)
    val isAsking: StateFlow<Boolean> = _isAsking.asStateFlow()

    fun ask(query: String) {
        if (query.isBlank() || _isAsking.value) return
        _isAsking.value = true
        viewModelScope.launch {
            val turn = try {
                ChatTurn(query = query, answer = repository.ask(query))
            } catch (e: Exception) {
                ChatTurn(query = query, error = "Unable to answer right now. Please try again.")
            }
            _turns.value = _turns.value + turn
            _isAsking.value = false
        }
    }
}
