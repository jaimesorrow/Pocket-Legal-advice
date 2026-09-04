---
name: data-model
description: A reference for the data schema and entity relationships in Alaska's Pocket Lawbook — the Room-backed verified-content store, the raw/untrusted API response shape, the account/subscription state, and the server-authoritative Firestore entitlement record. Include triggers: "what's the structure of", "how do I query", "what fields does", "entity diagram".
---

## PRIMARY ENTITIES

### ActionStepRow (Room, table `action_steps`)
- `violationKey: String` — primary key, column `violation_key`
- `actionSteps: List<String>` — column `action_steps`, joined with the ASCII unit separator via `Converters`
- `description: String`
- `jurisdiction: Jurisdiction` — enum `ALASKA` | `FEDERAL`

### ActionStepEntity (domain shape)
- `violationKey: String`, `actionSteps: List<String>`, `description: String`, `jurisdiction: Jurisdiction = ALASKA`

### ViolationDetail (raw API, untrusted; in `LegalViolationApiResponse.violations: List<ViolationDetail>`)
- `key: String` — only trusted field
- `title: String`, `description: String`, `recommendation: String` — discard after extracting `key`

### VerifiedActionStep (UI-facing)
- `violationKey: String`, `steps: List<String>`, `description: String`

### AccountState (sealed)
- `SignedOut`
- `SignedIn(email: String, subscription: SubscriptionStatus)` where `SubscriptionStatus` is `None` | `Active(renewsOn: String)`

### entitlements/{uid} (Firestore doc, server-authoritative)
- `caseLaw: Boolean`, `aiChat: Boolean`, `subscriptionState: Number`, `expiresAt: Timestamp?`, `purchaseToken: String?`, `updatedAt: Timestamp`

## RELATIONSHIPS
- `ViolationDetail` has-one `ActionStepRow` via `key` → `violation_key` (optional; `ActionStepDao.getActionStepsByKey` returns null on miss).
- `ActionStepRow` has-one mapped `ActionStepEntity` has-one mapped `VerifiedActionStep` — same `violationKey` threaded through (`RoomActionStepDao.toDomain`, `LegalAnalysisRepository.analyzeSituation`).
- `AccountState.SignedIn` (client belief, local) has-one `entitlements/{uid}` (server truth, Firebase Auth uid) — implicit, joined only by uid; the client `subscription` field is display-only and never gates `caseLaw/**` reads.
- `entitlements.purchaseToken` has-one Play purchase token, uniqueness enforced by `validatePurchase`'s existing-doc query, not a DB constraint.

## VALIDATION RULES
- `jurisdiction` constrained to `ALASKA`/`FEDERAL`; `Converters.toJurisdiction` throws on any other value.
- Email must contain `@`, trimmed length ≥ 3; password length ≥ 8 (`InMemoryAccountRepository.validate`).
- `entitlements.caseLaw`/`aiChat` true only if `subscriptionState ∈ {1,2,4}` (`ENTITLING_STATES`).
- Firestore rules: `entitlements/{uid}` — `allow write: if false`; `caseLaw/**` read requires `entitlements/{uid}.caseLaw == true` and `expiresAt > request.time`; `statutes/**` world-readable.

## QUERY PATTERNS
- Room: `@Query("SELECT * FROM action_steps WHERE violation_key = :violationKey")` → nullable single row.
- Firestore: `db.collection("entitlements").doc(uid).get()` (client); `.where("purchaseToken","==",token)` (functions).
- No SQL joins — `ViolationDetail.key` joins to `ActionStepEntity` in-memory via `mapNotNull`.

## MUTATIONS
- `ActionStepRow`: written only by `PocketLawbookDatabase.SeedCallback` from `VerifiedContentSeed`, `CONFLICT_REPLACE`; immutable elsewhere.
- `entitlements/{uid}`: written only by `writeEntitlement` (`{merge:true}`); deleted by `deleteAccount`. Client never writes.
- `AccountState`: in-memory, replaced wholesale on `signIn`/`signUp`/`subscribe`/`cancelSubscription`/`signOut`.
