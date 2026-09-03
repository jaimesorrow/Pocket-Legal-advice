package com.pocketlawbook.alaska.ui.navigation

import com.pocketlawbook.alaska.data.account.PremiumFeature
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction

/** Every route in the app, in one place. */
object Routes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "signIn"
    const val SIGN_UP = "signUp"
    const val ACCOUNT = "account"
    const val ALASKA_LAW = "law/alaska"
    const val FEDERAL_LAW = "law/federal"
    const val ALASKA_CASE_LAW = "caseLaw/alaska"
    const val FEDERAL_CASE_LAW = "caseLaw/federal"
    const val AI_CHAT = "aiChat"
    const val ANALYZE = "analyze"
    const val PAYWALL = "paywall"
    const val LEGAL = "legal"
    const val CONSENT = "consent"

    const val ACTION_STEPS = "steps/{violationKey}"
    fun actionSteps(violationKey: String) = "steps/$violationKey"

    const val LEGAL_DOCUMENT = "legal/{documentId}"
    fun legalDocument(documentId: String) = "legal/$documentId"
}

/**
 * An entry in the left navigation drawer.
 *
 * [requires] is null for anything in the free tier. When it is set, the drawer
 * shows a lock and the route redirects to the paywall unless the user is both
 * signed in and subscribed.
 */
data class DrawerEntry(
    val route: String,
    val label: String,
    val requires: PremiumFeature? = null
)

/**
 * Drawer contents, in display order.
 *
 * Accounts, billing, case law, and AI chat are not shipped features — the code
 * for them exists (AccountRepository, BillingRepository, GatedScreens) but is
 * intentionally not reachable from here until each has a real backend behind
 * it. Surfacing a lock icon for a feature with nothing behind the lock is
 * worse than not mentioning it. See CLAUDE.md.
 */
object DrawerSections {

    val account = emptyList<DrawerEntry>()

    val library = listOf(
        DrawerEntry(Routes.ALASKA_LAW, "Alaska law"),
        DrawerEntry(Routes.FEDERAL_LAW, "Federal law")
    )

    /** Always reachable. Play requires the privacy policy to be available in-app. */
    val about = listOf(
        DrawerEntry(Routes.LEGAL, "Legal & privacy")
    )
}

/** Which body of statutes a law-browsing route shows. */
fun jurisdictionForRoute(route: String?): Jurisdiction = when (route) {
    Routes.FEDERAL_LAW, Routes.FEDERAL_CASE_LAW -> Jurisdiction.FEDERAL
    else -> Jurisdiction.ALASKA
}
