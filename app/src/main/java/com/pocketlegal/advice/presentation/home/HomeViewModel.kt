package com.pocketlegal.advice.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlegal.advice.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.authToken
        .map { !it.isNullOrEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
