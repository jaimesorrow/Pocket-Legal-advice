package com.pocketlawbook.alaska.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketlawbook.alaska.BuildConfig
import com.pocketlawbook.alaska.data.account.AccountRepository
import com.pocketlawbook.alaska.data.account.InMemoryAccountRepository
import com.pocketlawbook.alaska.data.billing.BillingRepository
import com.pocketlawbook.alaska.data.billing.StubBillingRepository
import com.pocketlawbook.alaska.data.chat.AnthropicFramingService
import com.pocketlawbook.alaska.data.chat.LanguageModelFramingService
import com.pocketlawbook.alaska.data.chat.NullFramingService
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
import com.pocketlawbook.alaska.data.repository.ChatRepository
import com.pocketlawbook.alaska.data.repository.LegalAnalysisRepository
import com.pocketlawbook.alaska.viewmodel.ChatViewModel
import com.pocketlawbook.alaska.viewmodel.LegalAnalysisViewModel

/**
 * Hand-rolled dependency container.
 *
 * Small enough that Hilt would be overhead today. The reason to keep construction
 * in one place is that the interesting swaps are all here: [ActionStepDao] is
 * Room-backed, [LegalApiService] may gain a remote implementation, [AccountRepository]
 * becomes Firebase Auth, and [BillingRepository] becomes Play Billing backed by
 * server-side entitlement.
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
     * Falls back to [NullFramingService] whenever no key is configured (the
     * default for every build that doesn't set `anthropicApiKey` in a local,
     * gitignored `local.properties`) - the chat still works, it just always
     * shows every retrieved candidate with no framing sentence. See
     * [AnthropicFramingService]'s doc comment before ever shipping this with a
     * real key: a client-bundled key is not a safe way to hold this secret.
     */
    private val framingService: LanguageModelFramingService =
        if (BuildConfig.ANTHROPIC_API_KEY.isNotBlank()) {
            AnthropicFramingService(apiKey = BuildConfig.ANTHROPIC_API_KEY)
        } else {
            NullFramingService()
        }

    val chatRepository = ChatRepository(
        retriever = legalApiService,
        actionStepDao = actionStepDao,
        framingService = framingService
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

    val chatViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChatViewModel(chatRepository) as T
        }
}
