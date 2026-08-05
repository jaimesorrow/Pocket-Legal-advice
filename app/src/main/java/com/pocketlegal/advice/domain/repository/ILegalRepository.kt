package com.pocketlegal.advice.domain.repository

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.LegalCategory
import com.pocketlegal.advice.data.model.LegalLaw
import kotlinx.coroutines.flow.Flow

interface ILegalRepository {
    suspend fun getCategories(): NetworkResult<List<LegalCategory>>
    suspend fun getLawsByCategory(categoryId: String): NetworkResult<List<LegalLaw>>
    fun getCachedLawsByCategory(categoryId: String): Flow<List<LegalLaw>>
    fun searchCachedLaws(query: String): Flow<List<LegalLaw>>
    suspend fun searchLaws(query: String): NetworkResult<List<LegalLaw>>
    suspend fun getLawById(lawId: String): NetworkResult<LegalLaw>
}
