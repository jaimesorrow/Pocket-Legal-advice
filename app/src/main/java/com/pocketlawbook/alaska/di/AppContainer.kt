package com.pocketlawbook.alaska.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketlawbook.alaska.data.account.AccountRepository
import com.pocketlawbook.alaska.data.account.InMemoryAccountRepository
import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.local.dao.ActionStepDao
import com.pocketlawbook.alaska.data.local.dao.SeededActionStepDao
import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.data.remote.api.LegalApiService
import com.pocketlawbook.alaska.data.remote.api.OnDeviceLegalAnalyzer
import com.pocketlawbook.alaska.data.repository.LegalAnalysisRepository
import com.pocketlawbook.alaska.viewmodel.LegalAnalysisViewModel

/**
 * Hand-rolled dependency container.
 *
 * Small enough that Hilt would be overhead today. The reason to keep construction
 * in one place is that the interesting swaps are all here: [ActionStepDao] becomes
 * Room, [LegalApiService] may gain a remote implementation, and
 * [AccountRepository] becomes real auth plus Play Billing.
 */
class AppContainer {

    val actionStepDao: ActionStepDao = SeededActionStepDao()

    val legalApiService: LegalApiService = OnDeviceLegalAnalyzer()

    val legalAnalysisRepository = LegalAnalysisRepository(
        apiService = legalApiService,
        actionStepDao = actionStepDao
    )

    val accountRepository: AccountRepository = InMemoryAccountRepository()

    /** Direct lookup for screens that render one stored entry. */
    fun verifiedEntry(violationKey: String): ActionStepEntity? =
        VerifiedContentSeed.entries[violationKey]

    fun jurisdictionOf(violationKey: String): Jurisdiction =
        VerifiedContentSeed.entries[violationKey]?.jurisdiction ?: Jurisdiction.ALASKA

    val legalAnalysisViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LegalAnalysisViewModel(legalAnalysisRepository) as T
        }
}
