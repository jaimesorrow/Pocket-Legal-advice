package com.pocketlegal.advice.presentation.legal.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.LegalLaw
import com.pocketlegal.advice.domain.usecase.legal.GetLawsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val laws: List<LegalLaw> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LegalCategoryViewModel @Inject constructor(
    private val getLawsByCategoryUseCase: GetLawsByCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    fun loadCategory(categoryId: String) {
        getLawsByCategoryUseCase.cached(categoryId).onEach { laws ->
            _uiState.update { it.copy(laws = laws) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getLawsByCategoryUseCase(categoryId)) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}
