package com.pocketlawbook.alaska.data.account

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val EMAIL = "someone@example.com"
private const val PASSWORD = "correct horse battery"

private val signedOut = AccountState.SignedOut
private val signedInFree = AccountState.SignedIn(EMAIL, SubscriptionStatus.None)
private val signedInPaid = AccountState.SignedIn(EMAIL, SubscriptionStatus.Active("in 30 days"))

/**
 * The rules the paywall rests on.
 *
 * These are pure and have no Android dependency, which is why they are worth
 * pinning here: the gate is easy to get subtly wrong (an account alone unlocking
 * things is the classic slip) and a regression would not fail any other test.
 */
@RunWith(JUnit4::class)
class EntitlementTest {

    // =======================================================================
    // 1. BOTH CONDITIONS REQUIRED
    // =======================================================================

    @Test
    fun `signed out has no premium access`() {
        assertFalse(signedOut.hasPremiumAccess)
    }

    @Test
    fun `an account alone does not unlock anything`() {
        assertFalse(
            "signing in without subscribing must not unlock the paid tier",
            signedInFree.hasPremiumAccess
        )
    }

    @Test
    fun `signed in and subscribed unlocks premium`() {
        assertTrue(signedInPaid.hasPremiumAccess)
    }

    // =======================================================================
    // 2. LOCK REASON - the screens show different next actions
    // =======================================================================

    @Test
    fun `signed out is locked for want of an account`() {
        assertEquals(LockReason.NEEDS_ACCOUNT, signedOut.lockReason)
    }

    @Test
    fun `signed in without a subscription is locked for want of payment`() {
        assertEquals(LockReason.NEEDS_SUBSCRIPTION, signedInFree.lockReason)
    }

    @Test
    fun `a subscriber has no lock reason`() {
        assertNull(signedInPaid.lockReason)
    }

    @Test
    fun `lock reason is absent exactly when premium access is granted`() {
        listOf(signedOut, signedInFree, signedInPaid).forEach { state ->
            assertEquals(
                "lockReason and hasPremiumAccess disagreed for $state",
                state.hasPremiumAccess,
                state.lockReason == null
            )
        }
    }

    // =======================================================================
    // 3. EVERY PREMIUM FEATURE IS GATED BY THE SAME RULE
    // =======================================================================

    @Test
    fun `all three premium features are covered by the gate`() {
        // If a feature is added to the enum without being gated, this is the
        // test that should start looking wrong.
        assertEquals(
            setOf(
                PremiumFeature.ALASKA_CASE_LAW,
                PremiumFeature.FEDERAL_CASE_LAW,
                PremiumFeature.AI_CHAT
            ),
            PremiumFeature.entries.toSet()
        )
    }

    // =======================================================================
    // 4. THE STUB REPOSITORY'S STATE TRANSITIONS
    // =======================================================================

    @Test
    fun `subscribing without an account fails`() = runTest {
        val repo = InMemoryAccountRepository()

        val result = repo.subscribe()

        assertTrue("subscribe must refuse when signed out", result.isFailure)
        assertFalse(repo.state.value.hasPremiumAccess)
    }

    @Test
    fun `signing up then subscribing unlocks and cancelling re-locks`() = runTest {
        val repo = InMemoryAccountRepository()

        repo.signUp(EMAIL, PASSWORD)
        assertFalse("a new account starts unsubscribed", repo.state.value.hasPremiumAccess)

        repo.subscribe()
        assertTrue(repo.state.value.hasPremiumAccess)

        repo.cancelSubscription()
        assertFalse("cancelling must re-lock the paid tier", repo.state.value.hasPremiumAccess)
    }

    @Test
    fun `signing out drops premium access`() = runTest {
        val repo = InMemoryAccountRepository()
        repo.signUp(EMAIL, PASSWORD)
        repo.subscribe()
        assertTrue(repo.state.value.hasPremiumAccess)

        repo.signOut()

        assertFalse(repo.state.value.hasPremiumAccess)
        assertEquals(AccountState.SignedOut, repo.state.value)
    }

    @Test
    fun `credentials are rejected before an account is created`() = runTest {
        val repo = InMemoryAccountRepository()

        assertTrue("an address with no @ is not an email", repo.signUp("nope", PASSWORD).isFailure)
        assertTrue("a short password is rejected", repo.signUp(EMAIL, "short").isFailure)
        assertEquals(AccountState.SignedOut, repo.state.value)
    }
}
