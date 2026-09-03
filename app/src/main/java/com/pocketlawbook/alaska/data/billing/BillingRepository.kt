package com.pocketlawbook.alaska.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Subscription purchasing, kept separate from entitlement on purpose.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * THE RULE THIS FILE EXISTS TO ENFORCE
 *
 * Purchasing and entitlement are different questions with different authorities:
 *
 *   • "Did a purchase flow complete on this device?"  → Play Billing, client-side.
 *     Useful only for driving the UI. Trivially faked.
 *
 *   • "May this user read case law?"                  → the server, always.
 *     Decided from a purchase token validated against the Google Play Developer
 *     API, recorded server-side against the user's uid.
 *
 * A client-side boolean is not a paywall. Anyone can patch the APK. So gated
 * CONTENT is fetched from the server with the session token and is never bundled
 * in the APK behind a flag — otherwise the lock is decoration over data the user
 * already has on disk.
 * ─────────────────────────────────────────────────────────────────────────────
 */
interface BillingRepository {

    /** What the store says about the local purchase flow. Drives UI only. */
    val purchaseState: StateFlow<PurchaseState>

    /** Localised price from the store, e.g. "$10.00". Never hardcode this in UI. */
    val formattedPrice: StateFlow<String?>

    /** Connect to the billing service and load product details. */
    suspend fun connect()

    /**
     * Launch the Play purchase flow from [activity]. Play's launchBillingFlow
     * requires the foreground Activity, so this can't be launched headlessly.
     * Success here means the purchase completed locally — it does NOT mean the
     * user is entitled. The purchase token still has to be validated server-side.
     */
    suspend fun launchPurchaseFlow(activity: Activity): Result<Unit>

    /**
     * Re-query Play for existing purchases and re-submit their tokens for
     * server validation. Play requires a restore path: a user who reinstalls, or
     * signs in on a new device, must be able to recover access without paying
     * again.
     */
    suspend fun restorePurchases(): Result<Unit>

    /** Deep link to the Play subscription-management screen for cancellation. */
    fun manageSubscriptionUrl(): String
}

sealed interface PurchaseState {
    data object NotConnected : PurchaseState
    data object Ready : PurchaseState
    data object Pending : PurchaseState
    data object PurchasedAwaitingValidation : PurchaseState
    data class Failed(val message: String) : PurchaseState
}

object BillingConstants {
    /** Must match the subscription product created in Play Console. */
    const val SUBSCRIPTION_PRODUCT_ID = "pocket_lawbook_monthly"
    const val BASE_PLAN_ID = "monthly-autorenewing"

    fun manageUrl(packageName: String): String =
        "https://play.google.com/store/account/subscriptions" +
            "?sku=$SUBSCRIPTION_PRODUCT_ID&package=$packageName"
}

/**
 * Stand-in until the Play Billing library and a Play Console product exist.
 *
 * It cannot be wired to a real store from here: Play Billing needs a Play Console
 * account, a published subscription product, and a signed build uploaded to a
 * test track. This keeps the app buildable and the paywall demonstrable until
 * those exist.
 */
class StubBillingRepository(
    private val packageName: String,
    private val onPurchaseCompleted: suspend () -> Unit
) : BillingRepository {

    private val _purchaseState =
        kotlinx.coroutines.flow.MutableStateFlow<PurchaseState>(PurchaseState.NotConnected)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _formattedPrice = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    override val formattedPrice: StateFlow<String?> = _formattedPrice

    override suspend fun connect() {
        _purchaseState.value = PurchaseState.Ready
        _formattedPrice.value = "$10.00"
    }

    override suspend fun launchPurchaseFlow(activity: Activity): Result<Unit> {
        _purchaseState.value = PurchaseState.PurchasedAwaitingValidation
        onPurchaseCompleted()
        _purchaseState.value = PurchaseState.Ready
        return Result.success(Unit)
    }

    override suspend fun restorePurchases(): Result<Unit> = Result.success(Unit)

    override fun manageSubscriptionUrl(): String = BillingConstants.manageUrl(packageName)
}
