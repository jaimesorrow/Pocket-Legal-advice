package com.pocketlawbook.alaska.data.remote.api

import com.pocketlawbook.alaska.data.remote.model.LegalViolationApiResponse

interface LegalApiService {
    suspend fun analyzeLegalSituation(query: String): LegalViolationApiResponse
}
