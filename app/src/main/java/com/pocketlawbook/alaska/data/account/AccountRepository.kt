package com.pocketlawbook.alaska.data.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds who is signed in and whether they are subscribed.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * THIS IMPLEMENTATION IS A LOCAL STUB. It keeps state in memory, accepts any
 * credentials, and grants a subscription on request. It exists so the gating and
 * the paywall can be built and demonstrated; it authenticates nobody and charges
 * nobody.
 *
 * Shipping this requires two things it deliberately does not attempt:
 *   1. Real authentication against a backend that issues a verifiable session.
 *   2. Google Play Billing for the subscription, with entitlement checked
 *      server-side. Never let the client be the authority on whether someone
 *      has paid — a client-side boolean is trivially patched out.
 *
 * The interface below is the seam those implementations plug into, so screens
 * and view models do not change when they arrive.
 * ─────────────────────────────────────────────────────────────────────────────
 */
interface AccountRepository {
    val state: StateFlow<AccountState>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut()

    /**
     * Grants a subscription locally. Only meaningful for a stub/demo
     * implementation — a real implementation must refuse this (see
     * FirebaseAuthAccountRepository) because entitlement is never something the
     * client gets to set. Buy through [com.pocketlawbook.alaska.data.billing.BillingRepository];
     * cancel through [com.pocketlawbook.alaska.data.billing.BillingRepository.manageSubscriptionUrl].
     */
    suspend fun subscribe(): Result<Unit>
}

class InMemoryAccountRepository : AccountRepository {

    private val _state = MutableStateFlow<AccountState>(AccountState.SignedOut)
    override val state: StateFlow<AccountState> = _state.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        validate(email, password).onSuccess {
            _state.value = AccountState.SignedIn(
                email = email.trim(),
                subscription = SubscriptionStatus.None
            )
        }

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        validate(email, password).onSuccess {
            _state.value = AccountState.SignedIn(
                email = email.trim(),
                subscription = SubscriptionStatus.None
            )
        }

    override suspend fun signOut() {
        _state.value = AccountState.SignedOut
    }

    override suspend fun subscribe(): Result<Unit> {
        val current = _state.value
        if (current !is AccountState.SignedIn) {
            return Result.failure(IllegalStateException("Sign in before subscribing."))
        }
        _state.value = current.copy(subscription = SubscriptionStatus.Active(renewsOn = "in 30 days"))
        return Result.success(Unit)
    }

    private fun validate(email: String, password: String): Result<Unit> = when {
        !email.contains("@") || email.trim().length < 3 ->
            Result.failure(IllegalArgumentException("Enter an email address."))
        password.length < 8 ->
            Result.failure(IllegalArgumentException("Use at least 8 characters."))
        else -> Result.success(Unit)
    }
}
