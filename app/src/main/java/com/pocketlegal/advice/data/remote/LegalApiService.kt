package com.pocketlegal.advice.data.remote

import com.pocketlegal.advice.data.model.LegalCategory
import com.pocketlegal.advice.data.model.LegalLaw
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LegalApiService {

    @GET("legal/categories")
    suspend fun getCategories(): Response<List<LegalCategory>>

    @GET("legal/categories/{categoryId}/laws")
    suspend fun getLawsByCategory(
        @Path("categoryId") categoryId: String
    ): Response<List<LegalLaw>>

    @GET("legal/laws/{lawId}")
    suspend fun getLawById(@Path("lawId") lawId: String): Response<LegalLaw>

    @GET("legal/search")
    suspend fun searchLaws(@Query("q") query: String): Response<List<LegalLaw>>
}
