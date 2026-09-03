package com.pocketlawbook.alaska.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.pocketlawbook.alaska.data.account.AccountRepository
import com.pocketlawbook.alaska.data.account.FirebaseAuthAccountRepository
import com.pocketlawbook.alaska.data.account.InMemoryAccountRepository
import com.pocketlawbook.alaska.data.billing.BillingRepository
import com.pocketlawbook.alaska.data.billing.PlayBillingRepository
import com.pocketlawbook.alaska.data.billing.StubBillingRepository
import com.pocketlawbook.alaska.data.legal.ConsentRepository
import com.pocketlawbook.alaska.data.legal.SharedPrefsConsentRepository
import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.local.dao.ActionStepDao
import com.pocketlawbook.alaska.data.local.dao.RoomActionStepDao
import com.pocketlawbook.alaska.data.local.db.PocketLawbookDatabase
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
 * in one place is that the interesting swaps are all here: [ActionStepDao] is
 * Room-backed, [LegalApiService] may gain a remote implementation, and
 * [AccountRepository]/[BillingRepository] switch automatically between their
 * local stubs and the real Firebase Auth / Play Billing implementations based
 * on [firebaseConfigured] — see that property.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database = PocketLawbookDatabase.getInstance(appContext)

    val actionStepDao: ActionStepDao = RoomActionStepDao(database.actionStepDao())

    val legalApiService: LegalApiService = OnDeviceLegalAnalyzer()

    val legalAnalysisRepository = LegalAnalysisRepository(
        apiService = legalApiService,
        actionStepDao = actionStepDao
    )

    /**
     * True once google-services.json has been dropped in and the
     * com.google.gms.google-services plugin applied in app/build.gradle.kts —
     * that combination is what registers a default FirebaseApp. Until then this
     * is false and the app runs entirely on the local stubs below: no build
     * step, no manual flag, no code change needed either way.
     */
    private val firebaseConfigured: Boolean = FirebaseApp.getApps(appContext).isNotEmpty()

    val accountRepository: AccountRepository = if (firebaseConfigured) {
        FirebaseAuthAccountRepository()
    } else {
        InMemoryAccountRepository()
    }

    val consentRepository: ConsentRepository = SharedPrefsConsentRepository(appContext)

    /**
     * Real Play Billing once Firebase is configured — see [firebaseConfigured].
     * Still needs a Play Console subscription product to have anything to sell;
     * connect() reports that clearly rather than pretending to work. Before
     * Firebase is configured, this stays the local stub: a completed "purchase"
     * just marks the account subscribed locally, which is exactly the shortcut
     * that must never happen once this is the real implementation.
     */
    val billingRepository: BillingRepository = if (firebaseConfigured) {
        PlayBillingRepository(context = appContext, packageName = appContext.packageName)
    } else {
        StubBillingRepository(
            packageName = appContext.packageName,
            onPurchaseCompleted = { accountRepository.subscribe() }
        )
    }

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
