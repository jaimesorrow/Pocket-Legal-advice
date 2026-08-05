package com.pocketlegal.advice.domain.usecase.legal

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.LegalLaw
import com.pocketlegal.advice.domain.repository.ILegalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchLawsUseCase @Inject constructor(
    private val legalRepository: ILegalRepository
) {
    suspend operator fun invoke(query: String): NetworkResult<List<LegalLaw>> =
        legalRepository.searchLaws(query)

    fun searchCached(query: String): Flow<List<LegalLaw>> =
        legalRepository.searchCachedLaws(query)
}
