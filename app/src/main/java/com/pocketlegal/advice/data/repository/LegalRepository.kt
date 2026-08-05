package com.pocketlegal.advice.data.repository

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.core.util.safeApiCall
import com.pocketlegal.advice.data.local.dao.LegalLawDao
import com.pocketlegal.advice.data.local.entity.LegalLawEntity
import com.pocketlegal.advice.data.model.LegalCategory
import com.pocketlegal.advice.data.model.LegalLaw
import com.pocketlegal.advice.data.remote.LegalApiService
import com.pocketlegal.advice.domain.repository.ILegalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LegalRepositoryImpl @Inject constructor(
    private val legalApiService: LegalApiService,
    private val legalLawDao: LegalLawDao
) : ILegalRepository {

    override suspend fun getCategories(): NetworkResult<List<LegalCategory>> =
        safeApiCall { legalApiService.getCategories() }

    override suspend fun getLawsByCategory(categoryId: String): NetworkResult<List<LegalLaw>> {
        val result = safeApiCall { legalApiService.getLawsByCategory(categoryId) }
        if (result is NetworkResult.Success) {
            legalLawDao.insertAll(result.data.map { it.toEntity() })
        }
        return result
    }

    override fun getCachedLawsByCategory(categoryId: String): Flow<List<LegalLaw>> =
        legalLawDao.getLawsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchCachedLaws(query: String): Flow<List<LegalLaw>> =
        legalLawDao.searchLaws(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun searchLaws(query: String): NetworkResult<List<LegalLaw>> =
        safeApiCall { legalApiService.searchLaws(query) }

    override suspend fun getLawById(lawId: String): NetworkResult<LegalLaw> =
        safeApiCall { legalApiService.getLawById(lawId) }

    private fun LegalLaw.toEntity() = LegalLawEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        code = code,
        summary = summary,
        fullText = fullText,
        tags = tags.joinToString(","),
        lastUpdated = lastUpdated
    )

    private fun LegalLawEntity.toDomain() = LegalLaw(
        id = id,
        categoryId = categoryId,
        title = title,
        code = code,
        summary = summary,
        fullText = fullText,
        tags = if (tags.isBlank()) emptyList() else tags.split(","),
        lastUpdated = lastUpdated
    )
}
