# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Alaska's Pocket Lawbook is a native Android app (Kotlin, Jetpack Compose) with package/namespace
`com.pocketlawbook.alaska`. It targets minSdk 24 / compileSdk 35 / targetSdk 35.

**Jurisdiction scope:** the app covers **Alaska state law plus federal law, and nothing else**. This
is a product guarantee, not an incidental detail — every piece of verified content must be tagged
with the jurisdiction it comes from (`ALASKA` or `FEDERAL`), and the UI must disclose which one an
answer rests on. Never add content from another state's law, and never let an answer imply
nationwide applicability when it is Alaska-specific.

**No accounts:** nothing reachable in the app today requires signing in, has a user profile, or
touches server-side identity. It launches straight to the main screen. This is deliberate — users
asking about arrests or evictions should not have that tied to an account. Do not wire
sign-in/account/paywall screens back into `DrawerSections` or any other navigation path without an
explicit decision to do so.

**Current repository state:** the zero-hallucination pipeline described below is implemented,
tested, and is the entire shipped feature set. `AppContainer` (hand-rolled DI, no Hilt) wires a
Room-backed `ActionStepDao`, an on-device `LegalApiService` implementation, and the
`LegalAnalysisViewModel` together; `PocketLawbookApp`'s drawer currently exposes Alaska law, federal
law, the analysis/action-steps slice, AI chat, and the legal/privacy screens.
`LegalAnalysisViewModelTest` (21 tests, all passing) remains the executable spec for the analysis
pipeline; treat it as authoritative when changing any of those classes. See "Architecture: the chat
pipeline" below for the chat feature's own tests and invariants.

**Unshipped scaffolding — present in the codebase but deliberately not wired into navigation:**
`AccountRepository`/`AccountViewModel`/`AccountScreens` (sign-in, sign-up, account) and
`BillingRepository` (a stub that always reports success — no Play Console product exists) stay
unreachable, along with `GatedScreens.CaseLawScreen` (no case-law corpus exists) and
`LegalContentSyncRepository`/`LegalContentRefreshScheduler` (a sync contract with zero
implementations; the `MainActivity` call that scheduled it is commented out). Do not re-add
accounts/billing/case-law to `DrawerSections` or call `LegalContentRefreshScheduler.schedule` until
each has a real backend behind it.

**AI chat is the one exception, and it was a deliberate, explicit call to go out of order** (see git
history around the `data.chat` package) — it is real and reachable, ungated, ahead of the content
review this file otherwise insists on finishing first. It is real in the sense that on-device
retrieval and corpus lookup always work with zero fabrication risk regardless of network state; the
model-framing half is optional, network-dependent, and does nothing at all without a real API key
(see "Architecture: the chat pipeline" below). Do not read its presence as evidence the ordering
below stopped mattering for accounts/billing/case-law — those are still blocked on real backends,
and chat's content is exactly as unreviewed as everything else in `VerifiedContentSeed`.

**Content is 7 entries and not attorney-reviewed.** `VerifiedContentSeed` is a demonstration set;
see the warning on that object. Do not treat its presence as evidence the content is safe to show to
a real user in a crisis. It needs both more coverage and a licensed Alaska attorney's sign-off
before any public release.

## Direction (do not build out of order)

1. **Harden the analyzer and add an honest disclaimer.** Done: `OnDeviceLegalAnalyzer`'s keyword
   coverage was broadened and `AnalysisScreen` now carries a persistent "not exhaustive" disclaimer.
   It is still keyword substring matching, though, and will still silently return zero matches for
   phrasings its rule list doesn't cover — keep extending `DEFAULT_RULES` as gaps are found rather
   than treating this as finished.
2. **Grow and get the content reviewed.** In progress: content grew from 5 to 7 entries (citations
   verified via web search against primary/well-corroborated sources, not written from memory).
   **Not done, and blocking:** none of it has been reviewed by a licensed Alaska attorney. Do not
   treat web-verified as a substitute for that review.
3. **Re-wiring accounts/billing/case-law** still waits on 1 and 2 being genuinely finished (attorney
   review, specifically) — and only with a real backend plan (real auth + entitlement server, a Play
   Console product, a licensed case-law corpus). AI chat was pulled forward of this ordering by an
   explicit decision; that precedent doesn't extend to the other three without the same kind of
   explicit call.

Implementation notes on the two data sources:
- `ActionStepDao` is backed by Room (`data.local.db.PocketLawbookDatabase`,
  `RoomActionStepDao`), seeded on first launch from `VerifiedContentSeed` — which is explicitly
  **not yet attorney-reviewed** (see the warning on that object) and must be replaced by a
  reviewed, versioned dataset before release.
- `LegalApiService` is `OnDeviceLegalAnalyzer`, a local keyword-matching implementation rather
  than a remote HTTP call — this keeps the analysis feature working offline and means the "remote"
  side of the pipeline cannot itself hallucinate. A real network-backed implementation can still be
  swapped in later; `LegalAnalysisRepository` only depends on the `LegalApiService` interface.

## Build & test commands

The Gradle wrapper is committed (pinned to Gradle 8.11.1). An Android SDK with platform 35 and
build-tools 35.0.0 is required; point the build at it via a `local.properties` containing
`sdk.dir=/path/to/android-sdk` (this file is gitignored and must not be committed).

`local.properties` can optionally also carry `anthropicApiKey=sk-...` to exercise
`AnthropicFramingService` locally. Leaving it unset is the normal case — the app builds and the chat
feature works fully without it (see "Architecture: the chat pipeline"). Never commit a real key here
or anywhere else in the repo.

Note on locale: Kotlin encodes backtick-quoted test names directly into `.class` filenames, so test
names must stay ASCII-only. A non-ASCII character in a test name fails the build on systems whose
`sun.jnu.encoding` resolves to ASCII, with a misleading "Internal compiler error". Setting
`-Dsun.jnu.encoding=UTF-8` does not fix it — the JDK derives that from the OS locale at startup.

Use the wrapper for everything:
```
./gradlew build                 # full build
./gradlew assembleDebug         # build debug APK
./gradlew test                  # run JVM unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.pocketlawbook.alaska.viewmodel.LegalAnalysisViewModelTest"
./gradlew testDebugUnitTest --tests "*.LegalAnalysisViewModelTest.initial ui state is Idle"
./gradlew connectedAndroidTest  # instrumented tests (app/src/androidTest), needs a device/emulator
./gradlew lint                  # Android Lint
```

## Architecture: the zero-hallucination analysis pipeline

The app's core feature analyzes a user's described situation for legal rights violations and shows
them recommended action steps. The defining architectural rule, enforced throughout
`LegalAnalysisViewModelTest`, is that **no free-text generated by the remote/LLM API is ever allowed
to reach the UI**. The remote API is only trusted to identify *which* violations apply (as stable
keys); all human-readable copy shown to the user must come from a local, verified database lookup
keyed off that identifier.

Pipeline (view model → repository → two data sources), by package:

- `data.remote.api.LegalApiService` — remote call `analyzeLegalSituation(query): LegalViolationApiResponse`.
- `data.remote.model` — `LegalViolationApiResponse(violations: List<ViolationDetail>)` and
  `ViolationDetail(key, title, description, recommendation)`. The `key` (e.g.
  `"VIOLATION_48_HOUR_ARRAIGNMENT"`) is the only field from this response that may be used
  downstream; `title`/`description`/`recommendation` are raw, potentially hallucinated LLM output
  and must be discarded after extracting the key.
- `data.local.dao.ActionStepDao` — `getActionStepsByKey(key): ActionStepEntity?`, backed by Room (or
  equivalent local persistence) and populated from vetted/verified content, not from the API.
- `data.local.entity.ActionStepEntity(violationKey, actionSteps: List<String>, description)` — the
  single source of truth for any text shown to the user. Strings must be passed through byte-for-byte
  (no trimming/mutation) from DB to UI.
- `data.repository.LegalAnalysisRepository(apiService, actionStepDao)` — orchestrates: call the API,
  extract violation keys, look up each key's verified entity in the DAO, and drop any key with no DB
  match (never falls back to the raw API text).
- `ui.model.LegalAnalysisUiState` — sealed state: `Idle`, `Loading`, `Success(verifiedActionSteps:
  List<VerifiedActionStep>)`, `Error(message)`, and `NoVerifiedData` (used when an API-returned key
  has no corresponding verified DB entry — an acceptable alternative to an empty `Success`).
- `ui.model.VerifiedActionStep(violationKey, steps: List<String>, description)` — the only shape
  allowed to carry violation copy into Compose UI.
- `viewmodel.LegalAnalysisViewModel(repository)` — exposes `uiState: StateFlow<LegalAnalysisUiState>`
  and `analyzeSituation(query: String)`. Must emit `Loading` synchronously before suspending on the
  network call, and each call to `analyzeSituation` fully replaces the previous `Success` state
  (no accumulation across calls).

When adding or changing anything in this pipeline, uphold the invariants the test file encodes:
1. DAO lookups are keyed only by `violationKey`, never by any raw API string.
2. Nothing in a `Success`/`Error` state may contain, or be a substring of, raw API `title`/
   `description`/`recommendation` text (including from exception messages).
3. An API-returned key with no DB match is excluded from the result rather than falling back to raw text.
4. DB-sourced strings are surfaced exactly as stored (whitespace included).

## Architecture: the chat pipeline

`ui.screen.AiChatScreen` (in `ChatScreen.kt`, not `GatedScreens.kt`) is a second front-end over the
*same* verified store the analysis screen uses, per the hybrid design in `docs/screen-map.html`
("The chat, without breaking your guarantee"). It adds one genuinely new, optional, network-facing
component — a language model that may narrow candidates and write one framing sentence — without
weakening the zero-hallucination guarantee above, because the model never gets to originate a key or
any legal text; it can only select from what on-device retrieval already vetted.

- `data.chat.CandidatePassage(violationKey, description)` — one on-device-retrieved, DB-verified
  entry offered to the model as something it may pick, not something it may write about.
- `data.chat.LanguageModelFramingService` — `frame(query, candidates): FramingResult?`. Returns null
  (never throws to the caller) on any failure; a null result means "no framing, show every candidate
  unfiltered," never "show nothing."
- `data.chat.FramingResult(framingSentence: String?, selectedKeys: List<String>)` — the only two
  things a model implementation is trusted to produce.
- `data.chat.FramingResponseParser` — pure, synchronous, no network — the safety boundary. Enforces:
  (1) `selectedKeys` is filtered to only keys that were in the candidate set (an invented key is
  silently dropped, never passed through); (2) `framingSentence` is discarded to null if it contains
  a digit or `§` (a plain framing sentence has no reason to; a citation always does). Test this
  directly, per docs/screen-map.html's callout — see `FramingResponseParserTest`.
- `data.chat.AnthropicFramingService` — the real implementation, calling the Anthropic Messages API
  via OkHttp. **Its API key is not safe to ship**: a `BuildConfig` field is still bundled into the
  APK and extractable with a decompiler. It's wired only for local development, read from a
  gitignored `local.properties` key `anthropicApiKey` (empty by default). A real release needs a
  server-side proxy holding the key, not this. See the class's doc comment before changing this.
- `data.chat.NullFramingService` — the default when no key is configured. Always returns null. The
  chat is fully usable without a key; it just never gets a framing sentence or model-narrowed
  selection, identical to what happens on any single network failure.
- `data.repository.ChatRepository(retriever, actionStepDao, framingService)` — retrieval reuses the
  existing `LegalApiService` (today, `OnDeviceLegalAnalyzer`) exactly as the analysis pipeline does;
  a key with no DB match is dropped before the model ever sees it, same as `LegalAnalysisRepository`.
- `ui.model.ChatAnswer(framingSentence, matches: List<VerifiedActionStep>)` and
  `ui.model.ChatTurn(query, answer, error)` — `matches` reuses `VerifiedActionStep`, so a chat
  answer and an analysis answer render through the same `ui.component.VerifiedMatchCard`.
- `viewmodel.ChatViewModel(repository)` — exposes an append-only `turns: StateFlow<List<ChatTurn>>`
  (each `ask()` call adds a turn; unlike `LegalAnalysisViewModel`, nothing is replaced) and
  `isAsking: StateFlow<Boolean>`.

Invariants a change here must not break, on top of the ones above: a model implementation can never
add a key retrieval didn't already surface and DB-verify; a rejected framing sentence must still let
a valid key selection through (the two are validated independently); and a framing-service failure
of any kind must degrade to "every retrieved candidate, no framing," never to an error state or an
empty answer.

## Testing conventions

- JVM unit tests use JUnit4 (`@RunWith(JUnit4::class)`), MockK for mocking (`mockk()`, `coEvery`,
  `coVerify`), and `kotlinx-coroutines-test` (`StandardTestDispatcher`, `runTest`,
  `Dispatchers.setMain`/`resetMain` in `@Before`/`@After`) for deterministic coroutine execution.
- Test names are backtick-quoted natural-language sentences describing behavior, not method-name style.
- Tests are grouped into 11 numbered `// ====` banner sections by concern: (1) initial state,
  (2) loading, (3) zero-hallucination guarantees, (4) DB entity as single source of truth (DAO
  queried by key, never by raw text), (5) multi-violation handling, (6) missing DB entry, (7) API
  failure, (8) idempotency (second `analyzeSituation` call fully replaces the first), (9) empty API
  response, (10) exact string equality/no mutation, and (11) violation key pass-through. Shared
  fixtures (raw API strings vs. verified DB strings) are defined as top-level `private const val`s.
  Follow this structure for new tests in this class/file rather than inlining literals per-test.
- Code under test that touches `org.json.*` (e.g. `FramingResponseParser`) needs the `libs.json`
  test dependency (`org.json:json` from Maven) on the unit-test classpath — Android's own `org.json`
  classes are stubs on the local JVM and throw rather than parse anything.
