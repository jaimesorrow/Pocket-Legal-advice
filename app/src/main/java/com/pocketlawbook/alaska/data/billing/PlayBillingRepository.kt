package com.pocketlawbook.alaska.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Real Play Billing Library (v9) implementation.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * NOT WIRED IN BY DEFAULT. AppContainer only constructs this once a Firebase
 * project is configured — see AppContainer.firebaseConfigured. Compiles and
 * links with no Play Console setup at all; connect() simply reports no product
 * found until a subscription product with id [BillingConstants.SUBSCRIPTION_PRODUCT_ID]
 * exists there. Untested against a real build — this repo has no Android SDK
 * to compile or run against — so treat this as a careful first draft to verify
 * against a real Play Console product, not as proven-working code.
 *
 * This class only ever reports "a purchase flow completed here". It never
 * grants entitlement itself: every completed purchase is handed to the
 * `validatePurchase` Cloud Function (functions/index.js), which is the only
 * thing that writes to `entitlements/{uid}`. FirebaseAuthAccountRepository
 * reads that document to decide what the UI unlocks — never this class.
 * ─────────────────────────────────────────────────────────────────────────────
 */
class PlayBillingRepository(
    context: Context,
    private val packageName: String,
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
) : BillingRepository {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NotConnected)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _formattedPrice = MutableStateFlow<String?>(null)
    override val formattedPrice: StateFlow<String?> = _formattedPrice

    private var productDetails: ProductDetails? = null
    private var offerToken: String? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingResponseCode.OK -> purchases?.forEach(::handlePurchase)
            BillingResponseCode.USER_CANCELED -> _purchaseState.value = PurchaseState.Ready
            else -> _purchaseState.value = PurchaseState.Failed(
                result.debugMessage.ifBlank { "Purchase failed (${result.responseCode})." }
            )
        }
    }

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    override suspend fun connect() {
        val connected = suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
        if (!connected) {
            _purchaseState.value = PurchaseState.Failed("Couldn't connect to Google Play.")
            return
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingConstants.SUBSCRIPTION_PRODUCT_ID)
                        .setProductType(ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = client.queryProductDetails(params)
        val details = result.productDetailsList.firstOrNull()
        if (details == null) {
            _purchaseState.value = PurchaseState.Failed(
                "No Play Console product with id ${BillingConstants.SUBSCRIPTION_PRODUCT_ID} " +
                    "was found. Create the subscription product before this can sell anything."
            )
            return
        }

        val offer = details.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == BillingConstants.BASE_PLAN_ID }
            ?: details.subscriptionOfferDetails?.firstOrNull()

        productDetails = details
        offerToken = offer?.offerToken
        _formattedPrice.value = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        _purchaseState.value = PurchaseState.Ready

        restorePurchases()
    }

    override suspend fun launchPurchaseFlow(activity: Activity): Result<Unit> {
        val details = productDetails
        val token = offerToken
            ?: return Result.failure(IllegalStateException("Call connect() before launchPurchaseFlow()."))
        if (details == null) {
            return Result.failure(IllegalStateException("Call connect() before launchPurchaseFlow()."))
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(token)
                        .build()
                )
            )
            .build()

        val result = client.launchBillingFlow(activity, flowParams)
        return if (result.responseCode == BillingResponseCode.OK) {
            _purchaseState.value = PurchaseState.Pending
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalStateException(result.debugMessage.ifBlank { "Couldn't launch the purchase flow." })
            )
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingResponseCode.OK) {
            return Result.failure(
                IllegalStateException(
                    result.billingResult.debugMessage.ifBlank { "Couldn't restore purchases." }
                )
            )
        }
        result.purchasesList.forEach(::handlePurchase)
        return Result.success(Unit)
    }

    override fun manageSubscriptionUrl(): String = BillingConstants.manageUrl(packageName)

    /**
     * Only reports the purchase to the server for validation. Acknowledgement
     * also happens server-side (functions/index.js, after Google confirms the
     * token) — this class never acknowledges or grants anything itself.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        _purchaseState.value = PurchaseState.PurchasedAwaitingValidation
        functions.getHttpsCallable("validatePurchase")
            .call(mapOf("purchaseToken" to purchase.purchaseToken))
            .addOnSuccessListener { _purchaseState.value = PurchaseState.Ready }
            .addOnFailureListener { error ->
                val message = (error as? FirebaseFunctionsException)?.message
                    ?: "Couldn't verify the purchase with the server."
                _purchaseState.value = PurchaseState.Failed(message)
            }
    }
}
