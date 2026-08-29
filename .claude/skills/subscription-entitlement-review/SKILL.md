---
name: subscription-entitlement-review
description: Reviews changes to Alaska's Pocket Lawbook's server-authoritative subscription entitlement backend (functions/index.js, functions/firestore.rules) and the Android-side purchase/account seams it depends on (data.billing.BillingRepository, data.account.AccountRepository). Use this instead of, or alongside, a generic code review for any change touching those files, since this backend is the only place in the repo that handles real payment/webhook processing and is not covered by pocket-lawbook-review.
---

# Subscription entitlement backend review

`functions/index.js` and `functions/firestore.rules` are **not yet deployed** (no Firebase
project, no Play Console product, no Pub/Sub topic exist for it yet — see the file's own header
comment), but they are real, reviewable code that implements the rule stated throughout this repo:
*the client can never be the authority on whether someone has paid*. `pocket-lawbook-review`
covers the Kotlin/Compose zero-hallucination pipeline and jurisdiction tagging; it does not mention
this backend at all. Review diffs against the checklist below whenever they touch
`functions/index.js`, `functions/firestore.rules`, `data.billing.BillingRepository`, or
`data.account.AccountRepository`.

## 1. Entitlement writes must stay idempotent

`writeEntitlement()` is called from both `validatePurchase` (client-triggered) and
`playNotifications` (Play's real-time developer notifications, delivered over Pub/Sub, which **can
redeliver the same message**). It works today because every write is a full `.set(..., { merge:
true })` of the *current* state re-derived from a fresh `purchases.subscriptionsv2.get()` call —
replaying the same notification twice produces the same document, not a double-applied change.

- Flag any change that turns `caseLaw`/`aiChat`/`subscriptionState` into something
  incremented, appended, or toggled relative to the *previous* stored value instead of recomputed
  fresh from the Play API response each time — that reintroduces a double-application bug on
  Pub/Sub redelivery.
- Flag any new code path that grants entitlement without first calling
  `purchases.subscriptionsv2.get()` (or equivalent) to confirm state with Google, per this file's
  own stated reason for existing: "the client can only ever say a purchase flow completed here,
  which is trivially faked."

## 2. Cross-account purchase binding must not regress

`validatePurchase` explicitly rejects a `purchaseToken` already bound to a different `uid`
(`existing.docs.some((d) => d.id !== uid)`) so one purchase cannot unlock many accounts. Flag any
diff that removes, weakens, or reorders this check relative to the Play API call, or that trusts a
`uid` from anywhere other than `request.auth.uid` (a Firebase callable's verified caller identity —
never from `request.data`).

## 3. Acknowledgement-within-3-days must survive

Google auto-refunds an unacknowledged purchase after 3 days. `validatePurchase` acknowledges via
`api.purchases.subscriptions.acknowledge(...)` only when `acknowledgementState === 1`, wrapped in
its own try/catch so an acknowledgement failure doesn't fail the whole entitlement grant. Flag any
refactor that drops this call, makes it unconditional (re-acknowledging an already-acknowledged
purchase throws), or lets its failure bubble up and block `writeEntitlement`.

## 4. No secrets or purchase identifiers in logs

Current logging (`functions.logger.error("Play validation failed", { uid, err: err.message })`,
etc.) logs `uid` and an error message, never the raw `purchaseToken`, the full Play API response
body, or service-account credentials. Flag any new log line that includes `purchaseToken`,
`subscription` (the full object), or any field from the `google.auth.GoogleAuth` client — these are
either replay-able secrets or unnecessary PII in log storage.

## 5. Firestore rules stay server-write-only

`functions/firestore.rules` — `allow write: if false` on both `/entitlements/{uid}` and
`/caseLaw/{document=**}` is the entire reason client-side purchase state can be trusted at all
(Cloud Functions run with admin privileges and bypass rules; nothing else can write these paths).

- Flag any loosening of `allow write` on `entitlements/{uid}` or `caseLaw/**`, even
  conditionally (e.g. "if request.auth.uid == uid" on a write) — a client that can write its own
  entitlement can grant itself the paid tier, which the rules file's own comment calls out as
  making "the whole server-side validation pointless."
- Flag any new gated-content collection that doesn't mirror the `caseLaw` pattern (auth check +
  `entitlements/$(uid).caseLaw == true` + unexpired `expiresAt`) — a new collection that checks only
  `request.auth != null` without the entitlement lookup reopens the same hole.
- The comment on `statutes/{document=**}` ("knowing your rights is not the part that is being
  sold") is a product decision, not an oversight — don't flag its `allow read: if true` as a bug
  without confirming the free/paid split actually changed.

## 6. Client stays advisory, never authoritative

`BillingRepository`'s own doc comment states the rule this file exists to enforce: Play Billing
purchase completion drives UI only; entitlement is decided server-side. `AccountRepository`'s
`InMemoryAccountRepository`/`StubBillingRepository` are explicitly stand-ins ("authenticates
nobody and charges nobody").

- Flag any change that has a screen or view model gate `caseLaw`/`aiChat` content on
  `BillingRepository.purchaseState` or `AccountRepository.state`'s `SubscriptionStatus` directly,
  instead of on a value that ultimately traces back to a server-validated entitlement record.
- Flag any change that bundles real case-law content into the APK (as opposed to fetching it from
  the server at read time) — the rules file's comment explains why this makes the paywall
  decorative regardless of how correct the backend logic is.
- If a diff starts replacing `StubBillingRepository`/`InMemoryAccountRepository` with a real
  implementation, confirm it's an explicit, intentional migration (matching CLAUDE.md's "no
  accounts" callout for `AccountRepository`, which needs its own explicit product decision) rather
  than an incidental change riding along in an unrelated PR.

## 7. No monetary amounts, if any get introduced

Today no float/int dollar amount is ever computed or stored here — `formattedPrice` is an opaque,
store-localized string (`"$10.00"`) shown as-is, never parsed or arithmetic'd on, and
`functions/index.js` stores only booleans, an enum-like `subscriptionState`, and a timestamp. If a
future diff introduces an actual amount (e.g. logging revenue, prorating a refund, computing a
credit), flag any use of `Float`/`Double` for it and confirm currency travels with the value rather
than being assumed.
