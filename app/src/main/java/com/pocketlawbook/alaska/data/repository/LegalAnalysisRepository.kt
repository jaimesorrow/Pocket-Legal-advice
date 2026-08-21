package com.pocketlawbook.alaska.data.repository

import com.pocketlawbook.alaska.data.local.dao.ActionStepDao
import com.pocketlawbook.alaska.data.remote.api.LegalApiService
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep

/**
 * Orchestrates the zero-hallucination pipeline: calls the remote API, extracts
 * violation keys, and looks up each key's verified entity in the local DAO.
 * Any key with no DB match is dropped rather than falling back to raw API text.
 */
class LegalAnalysisRepository(
    private val apiService: LegalApiService,
    private val actionStepDao: ActionStepDao
) {
    suspend fun analyzeSituation(query: String): List<VerifiedActionStep> {
        val response = apiService.analyzeLegalSituation(query)
        return response.violations.mapNotNull { violation ->
            actionStepDao.getActionStepsByKey(violation.key)?.let { entity ->
                VerifiedActionStep(
                    violationKey = violation.key,
                    steps = entity.actionSteps,
                    description = entity.description
                )
            }
        }
    }
}
