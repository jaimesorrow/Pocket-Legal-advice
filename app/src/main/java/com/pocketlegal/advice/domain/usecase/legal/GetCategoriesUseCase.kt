package com.pocketlegal.advice.domain.usecase.legal

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.LegalCategory
import com.pocketlegal.advice.domain.repository.ILegalRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val legalRepository: ILegalRepository
) {
    suspend operator fun invoke(): NetworkResult<List<LegalCategory>> =
        legalRepository.getCategories()
}
