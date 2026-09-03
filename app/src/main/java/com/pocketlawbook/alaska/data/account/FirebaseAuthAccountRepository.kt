package com.pocketlawbook.alaska.data.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real Firebase Auth implementation.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * NOT WIRED IN BY DEFAULT. AppContainer only constructs this once a Firebase
 * project is configured — see AppContainer.firebaseConfigured. Untested against
 * a real build — this repo has no Android SDK to compile or run against — so
 * treat this as a careful first draft to verify locally, not proven-working code.
 *
 * subscribe() deliberately refuses to do anything: entitlement is never set by
 * this class or by the client at all. It is read live from `entitlements/{uid}`,
 * a document only the validatePurchase Cloud Function may write (see
 * functions/index.js and firestore.rules — client writes are denied outright).
 * Buy through BillingRepository.launchPurchaseFlow(); cancel through
 * BillingRepository.manageSubscriptionUrl(). This class only ever reflects
 * what the server already decided.
 * ─────────────────────────────────────────────────────────────────────────────
 */
class FirebaseAuthAccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AccountRepository {

    private val _state = MutableStateFlow(accountStateFor(auth.currentUser?.email))
    override val state: StateFlow<AccountState> = _state

    private var entitlementListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            entitlementListener?.remove()
            entitlementListener = null

            val user = firebaseAuth.currentUser
            if (user == null) {
                _state.value = AccountState.SignedOut
                return@addAuthStateListener
            }

            _state.value = accountStateFor(user.email)
            entitlementListener = firestore.collection("entitlements").document(user.uid)
                .addSnapshotListener { snapshot, _ ->
                    val current = _state.value
                    if (current !is AccountState.SignedIn) return@addSnapshotListener

                    val active = snapshot?.getBoolean("caseLaw") == true
                    val expiresAt = snapshot?.getTimestamp("expiresAt")?.toDate()
                    _state.value = current.copy(
                        subscription = if (active) {
                            SubscriptionStatus.Active(renewsOn = expiresAt?.let(::formatRenewal) ?: "soon")
                        } else {
                            SubscriptionStatus.None
                        }
                    )
                }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        Unit
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun subscribe(): Result<Unit> = Result.failure(
        UnsupportedOperationException(
            "Entitlement is decided server-side after Play Billing validates a purchase. " +
                "Use BillingRepository.launchPurchaseFlow() instead of AccountRepository.subscribe()."
        )
    )

    private fun accountStateFor(email: String?): AccountState =
        email?.let { AccountState.SignedIn(email = it, subscription = SubscriptionStatus.None) }
            ?: AccountState.SignedOut

    private fun formatRenewal(date: Date): String =
        SimpleDateFormat("MMMM d", Locale.US).format(date)
}
