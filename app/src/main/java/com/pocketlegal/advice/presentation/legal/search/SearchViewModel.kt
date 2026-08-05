package com.pocketlegal.advice.presentation.legal.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlegal.advice.data.model.LegalLaw
import com.pocketlegal.advice.domain.usecase.legal.SearchLawsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<LegalLaw> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLawsUseCase: SearchLawsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        _uiState
            .debounce(300)
            .filter { it.query.length >= 2 }
            .distinctUntilChanged { old, new -> old.query == new.query }
            .onEach { state ->
                searchLawsUseCase.searchCached(state.query).collect { results ->
                    _uiState.update { it.copy(results = results, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, isLoading = query.length >= 2) }
    }

    fun setInitialQuery(query: String) {
        if (query.isNotEmpty()) onQueryChange(query)
    }
}
