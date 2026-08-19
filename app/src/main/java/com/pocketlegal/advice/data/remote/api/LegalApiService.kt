package com.pocketlegal.advice.data.remote.api

import com.pocketlegal.advice.data.remote.model.LegalViolationApiResponse

interface LegalApiService {
    suspend fun analyzeLegalSituation(query: String): LegalViolationApiResponse
}
