package com.pocketlegal.advice.data.remote.api

import com.pocketlegal.advice.data.remote.model.LegalViolationApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LegalApiService {
    @POST("analyze")
    suspend fun analyzeLegalSituation(@Body request: AnalysisRequest): LegalViolationApiResponse
}

data class AnalysisRequest(
    val query: String
)
