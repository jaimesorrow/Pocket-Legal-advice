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

    const val ACTION_STEPS = "steps/{violationKey}"
    fun actionSteps(violationKey: String) = "steps/$violationKey"
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
 * The account controls sit at the top because they are what unlocks the locked
 * half of the list below them — a user who taps a locked item is sent here.
 */
object DrawerSections {

    val account = listOf(
        DrawerEntry(Routes.SIGN_IN, "Sign in"),
        DrawerEntry(Routes.ACCOUNT, "User account"),
        DrawerEntry(Routes.SIGN_UP, "Sign up")
    )

    val library = listOf(
        DrawerEntry(Routes.ALASKA_LAW, "Alaska law"),
        DrawerEntry(Routes.FEDERAL_LAW, "Federal law"),
        DrawerEntry(Routes.ALASKA_CASE_LAW, "Alaska case law", PremiumFeature.ALASKA_CASE_LAW),
        DrawerEntry(Routes.FEDERAL_CASE_LAW, "Federal case law", PremiumFeature.FEDERAL_CASE_LAW),
        DrawerEntry(Routes.AI_CHAT, "AI chat", PremiumFeature.AI_CHAT)
    )
}

/** Which body of statutes a law-browsing route shows. */
fun jurisdictionForRoute(route: String?): Jurisdiction = when (route) {
    Routes.FEDERAL_LAW, Routes.FEDERAL_CASE_LAW -> Jurisdiction.FEDERAL
    else -> Jurisdiction.ALASKA
}
