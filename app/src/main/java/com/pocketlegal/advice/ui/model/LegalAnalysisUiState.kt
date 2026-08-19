package com.pocketlegal.advice.ui.model

/**
 * The only shape allowed to carry violation copy into Compose UI. Every
 * field is sourced from the local verified database, never from the raw
 * API response.
 */
data class VerifiedActionStep(
    val violationKey: String,
    val steps: List<String>,
    val description: String
)

sealed class LegalAnalysisUiState {
    data object Idle : LegalAnalysisUiState()
    data object Loading : LegalAnalysisUiState()
    data class Success(val verifiedActionSteps: List<VerifiedActionStep>) : LegalAnalysisUiState()
    data class Error(val message: String) : LegalAnalysisUiState()
    data object NoVerifiedData : LegalAnalysisUiState()
}
