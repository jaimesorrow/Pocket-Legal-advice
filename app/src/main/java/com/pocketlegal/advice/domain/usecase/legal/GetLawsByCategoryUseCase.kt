package com.pocketlegal.advice.domain.usecase.legal

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.LegalLaw
import com.pocketlegal.advice.domain.repository.ILegalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLawsByCategoryUseCase @Inject constructor(
    private val legalRepository: ILegalRepository
) {
    suspend operator fun invoke(categoryId: String): NetworkResult<List<LegalLaw>> =
        legalRepository.getLawsByCategory(categoryId)

    fun cached(categoryId: String): Flow<List<LegalLaw>> =
        legalRepository.getCachedLawsByCategory(categoryId)
}
