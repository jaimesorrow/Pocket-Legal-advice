---
name: pocket-lawbook-review
description: Review Kotlin/Compose diffs in this repo (Alaska's Pocket Lawbook) against the zero-hallucination analysis pipeline, jurisdiction-tagging, and no-accounts invariants, on top of ordinary correctness review. Use this instead of a generic code review for any change touching data.remote, data.local, data.repository, viewmodel.LegalAnalysisViewModel, or the account/legal/subscription screens.
---

# Pocket Lawbook code review

This app's core guarantee is that **nothing a remote/LLM call generates ever reaches the UI as
displayed text** — only a stable `violationKey` crosses that boundary, and every string a user sees
comes from a verified local entity. A change that looks correct in isolation can still break this
guarantee. Review diffs in this repo against the checklist below, in addition to normal correctness,
readability, and test-coverage review. Read `/home/user/Pocket-Legal-advice/CLAUDE.md` first if it
isn't already in context — it's the canonical description of the pipeline these checks enforce.

## 1. Zero-hallucination pipeline

For any diff touching `data.remote`, `data.local`, `data.repository.LegalAnalysisRepository`, or
`viewmodel.LegalAnalysisViewModel`:

- **No raw API text downstream of the repository.** `ViolationDetail.title` / `.description` /
  `.recommendation` must never be assigned into `LegalAnalysisUiState`, `VerifiedActionStep`, a log
  line shown to the user, an exception message that reaches the UI, or a new field that re-exports
  them under another name. Only `ViolationDetail.key` may cross the repository boundary.
- **DAO lookups are keyed only by `violationKey`.** Flag any new lookup, cache, or fallback path
  that keys off raw API strings (title/description text, free-text search, fuzzy matching) instead
  of the exact key.
- **Missing DB entry → dropped, never defaulted.** A `violationKey` with no `ActionStepDao` match
  must be excluded from the result (or produce `NoVerifiedData`), never filled in with the raw API
  text, a hardcoded placeholder, or a "best guess" derived from the key itself.
- **DB strings pass through byte-for-byte.** No `.trim()`, `.replace()`, capitalization, or
  markdown-stripping applied to `ActionStepEntity` fields between DAO and Compose UI — verified
  legal text must render exactly as stored.
- **`Loading` emits synchronously** before any suspending call in `analyzeSituation`, and each call
  fully replaces the prior `Success` state — flag anything that appends/accumulates across calls.
- **Compare against `LegalAnalysisViewModelTest`.** It's the executable spec (11 numbered `// ====`
  sections). If a change touches pipeline behavior without a corresponding test update, say so
  explicitly rather than assuming coverage is fine.

## 2. Jurisdiction tagging (Alaska + federal only)

- Every `ActionStepEntity` — in `VerifiedContentSeed` or any future content source — must set
  `jurisdiction` explicitly (`ALASKA` or `FEDERAL`); don't let new entries silently ride the
  `ALASKA` default when the content is actually federal (or vice versa).
- Any UI surface that renders `ActionStepEntity`/`VerifiedActionStep` content must show which
  jurisdiction it rests on. Flag a new screen or component that drops the jurisdiction badge/label.
- Flag any new content, citation, or copy that reads as another state's law, or that would let a
  reader believe an Alaska-specific answer applies nationwide.
- New entries added to `VerifiedContentSeed` are demonstration content per its own header comment
  (not yet attorney-reviewed). Flag any PR description or commit message that implies newly added
  seed content is ready to ship without attorney sign-off and effective/last-reviewed dates.

## 3. No accounts / no server-side identity

- The app launches straight to the main screen with no sign-in. Flag any diff that adds
  authentication, a user profile, a server-side identity, or persistence that ties a query,
  situation description, or action-step history to an identifiable user — this needs an explicit
  product decision, not a quiet addition inside an unrelated PR.

## 4. Architecture conventions

- DI is hand-rolled via `AppContainer` (`di` package) — flag any new Hilt/Dagger annotations
  (`@HiltAndroidApp`, `@Inject`, `@Module`, etc.) or Hilt dependency additions as inconsistent with
  the existing wiring, unless the PR is explicitly a migration.
- `LegalApiService` is implemented on-device (`OnDeviceLegalAnalyzer`) so the feature works offline.
  A diff that adds a real network call should still go through the `LegalApiService` interface
  (repository code should not need to change), and should not remove offline behavior as a
  side effect.

## 5. Test conventions (`app/src/test`)

- Backtick-quoted natural-language test names, **ASCII only** — a non-ASCII character in a test
  name breaks the build on systems where `sun.jnu.encoding` is ASCII (see CLAUDE.md's locale note).
  Flag any curly quote, em dash, or non-ASCII punctuation in a new test name.
- New tests in `LegalAnalysisViewModelTest` should land under the existing numbered `// ====`
  section for their concern, using the top-level `private const val` fixtures for raw-API-vs-verified
  strings rather than inlining new literals per test.

## Running checks

Before signing off, run what's feasible from the changed surface:

```
./gradlew testDebugUnitTest --tests "com.pocketlawbook.alaska.viewmodel.LegalAnalysisViewModelTest"
./gradlew lint
```

If `local.properties` / the Android SDK isn't available in this environment, say so explicitly
instead of claiming the build was verified.

## Output

Report findings the same way `/code-review` does: most-severe first, each with file:line, a one
-sentence defect summary, and a concrete failure scenario. A pipeline-invariant violation (section 1
or 2) is always at least as severe as an equivalent plain correctness bug in this codebase — it's
the product's core guarantee, not a style preference.
