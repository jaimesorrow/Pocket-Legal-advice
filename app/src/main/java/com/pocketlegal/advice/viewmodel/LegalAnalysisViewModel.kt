package com.pocketlegal.advice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlegal.advice.data.repository.LegalAnalysisRepository
import com.pocketlegal.advice.ui.model.LegalAnalysisUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LegalAnalysisViewModel(
    private val repository: LegalAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LegalAnalysisUiState>(LegalAnalysisUiState.Idle)
    val uiState: StateFlow<LegalAnalysisUiState> = _uiState.asStateFlow()

    fun analyzeSituation(query: String) {
        _uiState.value = LegalAnalysisUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val verifiedActionSteps = repository.analyzeSituation(query)
                LegalAnalysisUiState.Success(verifiedActionSteps)
            } catch (e: Exception) {
                LegalAnalysisUiState.Error("Unable to analyze your situation. Please try again.")
            }
        }
    }
}
