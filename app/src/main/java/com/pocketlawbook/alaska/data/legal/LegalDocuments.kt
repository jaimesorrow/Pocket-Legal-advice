package com.pocketlawbook.alaska.data.legal

/**
 * The app's legal documents.
 *
 * ═════════════════════════════════════════════════════════════════════════════
 * DRAFT — NOT ATTORNEY-REVIEWED. NOT LEGAL ADVICE.
 *
 * This is a structured starting point covering the categories Google Play
 * policy, US consumer-protection law, and Alaska's unauthorized-practice-of-law
 * rules each require. It was NOT written by a lawyer and it does not protect
 * anyone from anything in its current form.
 *
 * Before release, a licensed Alaska attorney must review and rewrite this, with
 * particular attention to:
 *   • Unauthorized practice of law (Alaska Bar rules) — the line this app walks.
 *   • Limitation of liability and its enforceability in Alaska, including which
 *     limitations Alaska law will not enforce.
 *   • Consumer protection: auto-renewal disclosure (federal ROSCA, and state
 *     auto-renewal statutes), refund handling, and cancellation.
 *   • Arbitration and class-action waiver — include only on counsel's advice.
 *   • COPPA / age gating, if under-13 users are conceivable.
 *
 * Bump [VERSION] whenever the substance changes: users who accepted an earlier
 * version are re-prompted, which is what makes acceptance meaningful.
 * ═════════════════════════════════════════════════════════════════════════════
 */
object LegalDocuments {

    /** Incremented on any substantive change. Drives re-acceptance. */
    const val VERSION = 1

    /** Replace with the real entity and contact before release. */
    const val PROVIDER_NAME = "[Provider legal entity name]"
    const val CONTACT_EMAIL = "[support@example.com]"
    const val EFFECTIVE_DATE = "[Effective date]"

    data class Section(
        val heading: String,
        val body: List<String>
    )

    data class Document(
        val id: String,
        val title: String,
        val summary: String,
        val sections: List<Section>
    )

    val all: List<Document> by lazy {
        listOf(disclaimer, terms, subscriptionTerms, privacy, acceptableUse, contentSources)
    }

    fun byId(id: String): Document? = all.firstOrNull { it.id == id }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. The disclaimer. The most important document in an app like this.
    // ─────────────────────────────────────────────────────────────────────────
    val disclaimer = Document(
        id = "disclaimer",
        title = "Legal disclaimer",
        summary = "This app gives legal information, not legal advice.",
        sections = listOf(
            Section(
                "Not legal advice",
                listOf(
                    "Alaska's Pocket Lawbook provides general legal information about Alaska " +
                        "state law and federal law. It does not provide legal advice.",
                    "Legal information describes what the law says in general. Legal advice " +
                        "applies the law to your specific situation and can only come from a " +
                        "licensed attorney who knows the facts of your case. Nothing in this app " +
                        "is a substitute for that.",
                    "Do not rely on this app to decide what to do in a legal matter. If you are " +
                        "facing arrest, charges, eviction, or any legal proceeding, talk to a " +
                        "licensed Alaska attorney."
                )
            ),
            Section(
                "No attorney-client relationship",
                listOf(
                    "Using this app does not create an attorney-client relationship between you " +
                        "and $PROVIDER_NAME, its employees, or its contributors.",
                    "Nothing you type into this app is protected by attorney-client privilege. " +
                        "Information you enter is not confidential in the legal sense, even " +
                        "though this app is built to keep it on your device."
                )
            ),
            Section(
                "Jurisdiction is limited to Alaska and federal law",
                listOf(
                    "This app covers Alaska state law and United States federal law only. It " +
                        "does not cover the law of any other state, tribal law, or the law of " +
                        "any other country.",
                    "Alaska tribal courts and village jurisdictions may apply rules this app " +
                        "does not describe. If your matter involves a tribal court, consult an " +
                        "attorney familiar with that forum."
                )
            ),
            Section(
                "The law changes, and content may be out of date",
                listOf(
                    "Statutes are amended and cases are decided and overruled. Content in this " +
                        "app carries the date it was last reviewed. Content may nevertheless be " +
                        "outdated, incomplete, or wrong.",
                    "$PROVIDER_NAME does not warrant that any content is current, accurate, or " +
                        "applicable to your situation."
                )
            ),
            Section(
                "Emergencies",
                listOf(
                    "This app is not an emergency service. If you are in danger, call 911. If " +
                        "you need a lawyer now and cannot afford one, ask for the public defender."
                )
            )
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Terms of service / user agreement.
    // ─────────────────────────────────────────────────────────────────────────
    val terms = Document(
        id = "terms",
        title = "Terms of service",
        summary = "The agreement between you and $PROVIDER_NAME.",
        sections = listOf(
            Section(
                "Acceptance",
                listOf(
                    "These terms are effective $EFFECTIVE_DATE. By using Alaska's Pocket " +
                        "Lawbook you agree to them and to the Legal Disclaimer and Privacy " +
                        "Policy. If you do not agree, do not use the app.",
                    "If these terms change materially, you will be asked to accept the new " +
                        "version before continuing to use the app."
                )
            ),
            Section(
                "Who may use the app",
                listOf(
                    "You must be at least 13 years old, or the minimum age required in your " +
                        "jurisdiction, to use this app. You must be 18 or older to create an " +
                        "account or purchase a subscription."
                )
            ),
            Section(
                "What you get",
                listOf(
                    "A free tier providing Alaska statutes, federal statutes, and the situation " +
                        "analyzer. No account is required for the free tier.",
                    "A paid subscription adding Alaska case law, federal case law, and the AI " +
                        "chat. The paid tier requires an account and an active subscription."
                )
            ),
            Section(
                "Your account",
                listOf(
                    "You are responsible for keeping your credentials secure and for activity " +
                        "under your account. Tell us promptly at $CONTACT_EMAIL if you believe " +
                        "your account has been compromised.",
                    "You may delete your account at any time from the account screen in this " +
                        "app, or by writing to $CONTACT_EMAIL. Deleting your account deletes the " +
                        "personal data associated with it, subject to any records we are legally " +
                        "required to retain. Deleting your account does not by itself cancel a " +
                        "subscription purchased through Google Play — cancel that in Google Play."
                )
            ),
            Section(
                "Disclaimer of warranties",
                listOf(
                    "The app is provided \"as is\" and \"as available\", without warranties of " +
                        "any kind, express or implied, including any implied warranty of " +
                        "merchantability, fitness for a particular purpose, accuracy, or " +
                        "non-infringement, to the fullest extent permitted by law."
                )
            ),
            Section(
                "Limitation of liability",
                listOf(
                    "To the fullest extent permitted by law, $PROVIDER_NAME is not liable for " +
                        "any indirect, incidental, special, consequential, or punitive damages, " +
                        "or for any loss arising from your use of, or reliance on, this app — " +
                        "including any legal outcome.",
                    "Some limitations are not enforceable in some jurisdictions. Where a " +
                        "limitation is not enforceable, it does not apply to you.",
                    "[ATTORNEY: confirm the enforceable scope under Alaska law and set an " +
                        "aggregate liability cap.]"
                )
            ),
            Section(
                "Indemnity",
                listOf(
                    "You agree to indemnify $PROVIDER_NAME against claims arising from your " +
                        "misuse of the app or your violation of these terms.",
                    "[ATTORNEY: review scope and carve-outs.]"
                )
            ),
            Section(
                "Governing law and disputes",
                listOf(
                    "These terms are governed by the laws of the State of Alaska, without regard " +
                        "to its conflict-of-laws rules.",
                    "[ATTORNEY: decide venue, and whether to include an arbitration clause and " +
                        "class-action waiver. Do not include either without advice — both carry " +
                        "consumer-law consequences and disclosure requirements.]"
                )
            ),
            Section(
                "Changes and termination",
                listOf(
                    "We may modify or discontinue the app or any feature. We may suspend or " +
                        "terminate access for violation of these terms.",
                    "If we discontinue the paid tier, we will provide notice and handle " +
                        "outstanding subscription periods in accordance with Google Play policy " +
                        "and applicable law."
                )
            )
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Subscription terms. Google Play requires these to be clear BEFORE
    //    purchase, and auto-renewal disclosure is a legal requirement.
    // ─────────────────────────────────────────────────────────────────────────
    val subscriptionTerms = Document(
        id = "subscription",
        title = "Subscription terms",
        summary = "Price, renewal, and how to cancel.",
        sections = listOf(
            Section(
                "What it costs",
                listOf(
                    "The subscription is $10.00 per month in US dollars, charged through your " +
                        "Google Play account. Tax may be added depending on your location.",
                    "The price shown in Google Play at the time of purchase controls."
                )
            ),
            Section(
                "Automatic renewal",
                listOf(
                    "The subscription renews automatically every month until you cancel. Your " +
                        "Google Play account is charged at each renewal.",
                    "Renewal happens within 24 hours of the end of the current period unless you " +
                        "cancel before then."
                )
            ),
            Section(
                "How to cancel",
                listOf(
                    "Cancel any time in Google Play: open the Play Store, then Payments and " +
                        "subscriptions, then Subscriptions, then select this app and cancel.",
                    "Cancelling stops future charges. You keep access to the paid tier until the " +
                        "end of the period you have already paid for.",
                    "Uninstalling the app does not cancel your subscription."
                )
            ),
            Section(
                "Refunds",
                listOf(
                    "Purchases are processed by Google Play and are subject to Google Play's " +
                        "refund policy. Requests are made through Google Play.",
                    "Where consumer law gives you a right to a refund beyond that policy, that " +
                        "right is unaffected by these terms."
                )
            ),
            Section(
                "What the subscription does not include",
                listOf(
                    "A subscription does not buy legal advice, representation, or an attorney's " +
                        "review of your situation. It unlocks access to case law content and the " +
                        "AI chat feature, both of which remain legal information only."
                )
            )
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Privacy policy. Play requires this, and the Data Safety form in Play
    //    Console must match it exactly.
    // ─────────────────────────────────────────────────────────────────────────
    val privacy = Document(
        id = "privacy",
        title = "Privacy policy",
        summary = "What is collected, what stays on your device, and what is not.",
        sections = listOf(
            Section(
                "The short version",
                listOf(
                    "The free tier requires no account and collects no personal information. " +
                        "What you type into the situation analyzer is processed on your device " +
                        "and is not transmitted.",
                    "If you create an account, we collect your email address so you can sign in " +
                        "and so your subscription can be attached to you."
                )
            ),
            Section(
                "Situation descriptions stay on your device",
                listOf(
                    "The situation analyzer runs entirely on your device. Descriptions you enter " +
                        "are not sent to a server, not stored remotely, and not used for training.",
                    "This is deliberate. People describe arrests, evictions, and family matters " +
                        "in this app, and that should not become a record held by anyone else."
                )
            ),
            Section(
                "What we collect if you create an account",
                listOf(
                    "Email address, and authentication data needed to sign you in.",
                    "Subscription status, so the app knows whether the paid tier is unlocked. " +
                        "Payment card details are handled by Google Play and are never seen by us.",
                    "[ATTORNEY / ENGINEERING: if the AI chat sends queries to a server, that must " +
                        "be disclosed here explicitly, along with retention. Do not ship the chat " +
                        "until this section describes it accurately.]"
                )
            ),
            Section(
                "Analytics",
                listOf(
                    "If analytics are enabled, they are limited to which screens are opened and " +
                        "whether features succeed or fail. The text of anything you type, and the " +
                        "identity of any statute or case you read, are not collected.",
                    "[ENGINEERING: keep this true. Screen-level counters only — never query text " +
                        "and never content identifiers.]"
                )
            ),
            Section(
                "Sharing",
                listOf(
                    "We do not sell personal information. We do not share it with advertisers.",
                    "We share data with service providers only as needed to run the app — " +
                        "authentication and payment processing — and with law enforcement only " +
                        "where legally compelled."
                )
            ),
            Section(
                "Your choices",
                listOf(
                    "Use the app without an account. Delete your account at any time from the " +
                        "account screen, which deletes the personal data associated with it. " +
                        "Clear locally saved items in Settings.",
                    "Request a copy of your data, or ask questions, at $CONTACT_EMAIL."
                )
            ),
            Section(
                "Children",
                listOf(
                    "This app is not directed to children under 13 and we do not knowingly " +
                        "collect personal information from them."
                )
            )
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Acceptable use.
    // ─────────────────────────────────────────────────────────────────────────
    val acceptableUse = Document(
        id = "acceptable-use",
        title = "Acceptable use",
        summary = "What you may and may not do with the app and its content.",
        sections = listOf(
            Section(
                "Permitted use",
                listOf(
                    "Use the app to inform yourself about Alaska and federal law, for your own " +
                        "personal, non-commercial purposes."
                )
            ),
            Section(
                "Not permitted",
                listOf(
                    "Do not redistribute, resell, or bulk-export the content, or use it to build " +
                        "a competing product.",
                    "Do not present output from this app as legal advice to another person. " +
                        "Doing so may constitute the unauthorized practice of law.",
                    "Do not attempt to bypass the subscription, reverse engineer the app, or " +
                        "access paid content without an active subscription.",
                    "Do not use the app to harass, or to plan or carry out unlawful activity."
                )
            )
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Content sources and attribution.
    // ─────────────────────────────────────────────────────────────────────────
    val contentSources = Document(
        id = "sources",
        title = "Content sources",
        summary = "Where the law in this app comes from, and who reviewed it.",
        sections = listOf(
            Section(
                "Primary sources",
                listOf(
                    "Statutes and court opinions are transcribed from primary sources. Alaska " +
                        "statutes come from the Alaska Statutes as published by the State of " +
                        "Alaska. Alaska Supreme Court and Court of Appeals opinions, and federal " +
                        "opinions, are works of government and are in the public domain.",
                    "[ENGINEERING: record the exact source and retrieval date for every entry, " +
                        "and surface it here.]"
                )
            ),
            Section(
                "Review",
                listOf(
                    "Content is reviewed by a licensed Alaska attorney before publication and " +
                        "carries a last-reviewed date.",
                    "[THIS IS NOT YET TRUE. It must be true before release — see " +
                        "VerifiedContentSeed.]"
                )
            ),
            Section(
                "How the AI chat works",
                listOf(
                    "The AI chat does not write legal statements. It selects which reviewed " +
                        "statutes and cases bear on your question, and those are shown to you as " +
                        "written, with their citations. Any plain-language framing it adds is " +
                        "labelled and contains no legal claim or citation.",
                    "This design exists so that a citation shown to you cannot be invented."
                )
            ),
            Section(
                "Open source",
                listOf(
                    "This app uses open-source software. Licenses are listed in Settings."
                )
            )
        )
    )
}
