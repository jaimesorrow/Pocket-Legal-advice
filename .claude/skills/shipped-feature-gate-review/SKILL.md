---
name: shipped-feature-gate-review
description: Reviews changes to Alaska's Pocket Lawbook's navigation reachability — Destinations.kt's DrawerSections, PocketLawbookApp.kt's AppNavHost and screen call sites, MainActivity's WorkManager scheduling — against CLAUDE.md's "Direction" invariant that accounts, billing, case law, and AI chat stay unreachable until each has a real backend. Use this instead of, or alongside, pocket-lawbook-review for any change touching DrawerSections, any onNavigate/onSeeSubscription-style callback wiring, or LegalContentRefreshScheduler.schedule.
---

PR #11/#12 deliberately emptied `DrawerSections.account` and trimmed `DrawerSections.library` to
just Alaska/federal law, specifically so sign-in, billing, case law, and AI chat — all real,
committed, but backend-less code — can't be reached by a user. That was a considered product
decision (see CLAUDE.md's "Direction" section), not an oversight, and it is easy to silently undo
one call site at a time.

**A known live gap this skill exists to catch:** `WelcomeScreen`'s "Case law extension" card has an
unconditional `onSeeSubscription` button wired to `Routes.PAYWALL`, and `PaywallScreen` itself
offers `onSignUp`/`onSignIn`. Pruning `DrawerSections` didn't touch this path, so the paywall and
accounts are still reachable today despite the stated invariant — check whether this has been fixed
before assuming the invariant currently holds.

## Checks

1. **`DrawerSections.account` must stay empty and `DrawerSections.library` must stay
   Alaska/federal-law-only** unless the diff also shows the backend behind the new entry is real:
   `AccountRepository` no longer `InMemoryAccountRepository`, `BillingRepository` no longer
   `StubBillingRepository`, or `GatedScreens`' `CaseLawScreen`/`AiChatScreen` backed by an actual
   corpus/chat service instead of a placeholder.
2. **Reachability isn't just `DrawerSections`.** Check every screen's callback parameters
   (`onSeeSubscription`, `onUnlock`, `onSignIn`, etc.) and every `AppNavHost` `composable(...)` call
   site that constructs one from another screen — a route can be reachable through a button on
   *any* screen, not only through the drawer. A route existing in `Routes` or in the `NavHost` is
   not evidence it's unreachable; trace what actually calls `onNavigate` with it.
3. **`CLAUDE.md`'s "Direction" section sets an explicit order**: (1) harden the analyzer, (2) grow
   and get content attorney-reviewed, (3) only then re-wire accounts/billing/case-law/AI-chat. Flag
   a PR that does (3)-shaped work — new DrawerSections entries, new nav paths to `GatedScreens`,
   billing wiring — without the diff or PR description showing (1) and (2) are substantially done.
4. **`LegalContentRefreshScheduler.schedule()` must stay uncalled** from `MainActivity` while
   `LegalContentSyncRepository` has zero implementations in `AppContainer`. Flag any diff that
   re-enables the call without also adding a real implementation.
5. **If a PR intentionally changes this invariant** (re-wires a previously-gated feature on
   purpose), it must update CLAUDE.md's "Unshipped scaffolding" list and "Direction" section in the
   same diff — a reachability change that doesn't touch CLAUDE.md is either accidental or
   undocumented, and both are worth a comment.
