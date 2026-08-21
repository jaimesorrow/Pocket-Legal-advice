package com.pocketlawbook.alaska.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pocketlawbook.alaska.data.account.AccountRepository
import com.pocketlawbook.alaska.data.account.AccountState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    val state: StateFlow<AccountState> = repository.state

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) = submit(onSuccess) {
        repository.signIn(email, password)
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) = submit(onSuccess) {
        repository.signUp(email, password)
    }

    fun subscribe(onSuccess: () -> Unit = {}) = submit(onSuccess) { repository.subscribe() }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _errorMessage.value = null
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch { repository.cancelSubscription() }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block()
                .onSuccess {
                    _errorMessage.value = null
                    onSuccess()
                }
                .onFailure { _errorMessage.value = it.message ?: "Something went wrong." }
        }
    }

    class Factory(private val repository: AccountRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountViewModel(repository) as T
    }
}
