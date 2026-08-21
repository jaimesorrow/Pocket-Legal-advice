package com.pocketlawbook.alaska.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketlawbook.alaska.data.account.AccountRepository
import com.pocketlawbook.alaska.data.account.InMemoryAccountRepository
import com.pocketlawbook.alaska.data.billing.BillingRepository
import com.pocketlawbook.alaska.data.billing.StubBillingRepository
import com.pocketlawbook.alaska.data.legal.ConsentRepository
import com.pocketlawbook.alaska.data.legal.SharedPrefsConsentRepository
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
 * Room, [LegalApiService] may gain a remote implementation, [AccountRepository]
 * becomes Firebase Auth, and [BillingRepository] becomes Play Billing backed by
 * server-side entitlement.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val actionStepDao: ActionStepDao = SeededActionStepDao()

    val legalApiService: LegalApiService = OnDeviceLegalAnalyzer()

    val legalAnalysisRepository = LegalAnalysisRepository(
        apiService = legalApiService,
        actionStepDao = actionStepDao
    )

    val accountRepository: AccountRepository = InMemoryAccountRepository()

    val consentRepository: ConsentRepository = SharedPrefsConsentRepository(appContext)

    /**
     * Stubbed until a Play Console subscription product exists. On a completed
     * purchase it marks the account subscribed locally — which is exactly the
     * shortcut that must be replaced by server-side validation before release.
     */
    val billingRepository: BillingRepository = StubBillingRepository(
        packageName = appContext.packageName,
        onPurchaseCompleted = { accountRepository.subscribe() }
    )

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
