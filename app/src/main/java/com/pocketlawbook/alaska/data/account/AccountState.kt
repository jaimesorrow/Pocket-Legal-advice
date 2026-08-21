package com.pocketlawbook.alaska.data.account

/**
 * Features that are not part of the free tier.
 *
 * The free tier — Alaska statutes, federal statutes, and the situation analyzer —
 * stays reachable with no account at all, so someone can look up their rights
 * without identifying themselves. Only these extras sit behind the paywall.
 */
enum class PremiumFeature(val label: String) {
    ALASKA_CASE_LAW("Alaska case law"),
    FEDERAL_CASE_LAW("Federal case law"),
    AI_CHAT("AI chat")
}

/** What the subscription costs, in one place so screens cannot disagree. */
object SubscriptionPlan {
    const val PRICE_DISPLAY = "$10"
    const val PERIOD_DISPLAY = "month"
    const val FULL_DISPLAY = "$10 / month"
}

sealed interface SubscriptionStatus {
    /** Signed in, but not paying. Case law and AI chat stay locked. */
    data object None : SubscriptionStatus

    /** Paying. [renewsOn] is display text, e.g. "March 4". */
    data class Active(val renewsOn: String) : SubscriptionStatus
}

sealed interface AccountState {
    data object SignedOut : AccountState

    data class SignedIn(
        val email: String,
        val subscription: SubscriptionStatus
    ) : AccountState
}

/**
 * The single question every gated screen asks. Both conditions must hold: an
 * account alone does not unlock anything, and there is no subscription without
 * an account.
 */
val AccountState.hasPremiumAccess: Boolean
    get() = this is AccountState.SignedIn && subscription is SubscriptionStatus.Active

/** Why a locked screen is locked, so it can tell the user the useful thing. */
enum class LockReason { NEEDS_ACCOUNT, NEEDS_SUBSCRIPTION }

val AccountState.lockReason: LockReason?
    get() = when {
        this !is AccountState.SignedIn -> LockReason.NEEDS_ACCOUNT
        subscription !is SubscriptionStatus.Active -> LockReason.NEEDS_SUBSCRIPTION
        else -> null
    }
