# Content Compiler

Turns authored legal content (`content/**/*.yaml`) into the two artifacts the app needs:

1. **`app/src/main/assets/legal-content.json`** — the content bundle, the only source of user-visible
   text in the app.
2. **`app/src/main/java/com/pocketlegal/advice/data/local/ViolationKey.kt`** — the closed violation
   vocabulary the classifier is restricted to (AI rule 1).

Both come from the same source, deliberately. A key cannot exist in the classifier's vocabulary
without verified content behind it, and content cannot exist for a key the app will refuse. The two
cannot drift.

## Running it

```bash
# validate without writing anything
python3 tools/content-compiler/compile_content.py --check

# build the bundle and the enum
python3 tools/content-compiler/compile_content.py

# what CI runs — warnings are failures
python3 tools/content-compiler/compile_content.py --strict

# tests
python3 tools/content-compiler/test_compile_content.py
```

Requires Python 3.11+ and PyYAML. Nothing else — no JVM, no Android SDK. That is intentional: the
people authoring legal content should not need an Android toolchain to check their work.

**Exit codes:** `0` clean · `1` validation errors (nothing written) · `2` warnings under `--strict`.

## What it enforces

Encoded from `docs/AI-REQUIREMENTS.md`. Full list in `SCHEMA.md`; the load-bearing ones:

| Enforcement | Rule |
| --- | --- |
| Every entry cites at least one authority, with a reviewer and a review date | 14 |
| Entries past their `review_by` date are dropped from the bundle, not shipped | 15 |
| Jurisdiction is part of the entry's identity, so the same key can carry different content per state | 12 |
| Reading level above grade 8 warns | 17 |
| Case-law-backed entries may not have review intervals over 180 days | — see `LEGAL-SOURCES.md` |
| Placeholder text (`TODO`, `example.com`, `lorem ipsum`) fails the build | DESIGN.md |
| `status: EXAMPLE` files cannot be built into a bundle without `--allow-examples` | DESIGN.md |
| Author and reviewer must be different people | 14 |

Expiry deserves a note: an expired entry is **not** a build failure. It is reported and dropped, and
the app then abstains for that key. That is rule 15 working — the failure mode we are preventing is
shipping guidance nobody has confirmed is still accurate, not a red build.

## Layout

```
tools/content-compiler/
  compile_content.py         the compiler
  test_compile_content.py    38 tests, stdlib unittest
  SCHEMA.md                  authoring format reference
  fixtures/                  example content — marked EXAMPLE, cannot ship
content/
  <jurisdiction>/            real authored content lives here
    VIOLATION_*.yaml
```

The fixtures under `fixtures/` are **not** reviewed legal content and carry `status: EXAMPLE` so the
compiler refuses to build them into a real bundle. They exist to demonstrate the format.

## Where this sits in the pipeline

```
official sources ──▶ counsel authors YAML ──▶ [this tool] ──▶ bundle + enum ──▶ APK
                          (reviewed in a PR)     validates
```

The git history of `content/` is the audit trail rule 14 requires — who wrote what, who reviewed it,
when. Keep review in pull requests for that reason.

## Not yet built

- **Bundle signing.** The bundle is hashed but not signed. Before shipping, sign it and verify the
  signature at load time.
- **Change detection.** The `text_hash` field on each authority exists for it, but the job that
  re-fetches authorities and diffs them isn't written. See `docs/LEGAL-SOURCES.md`.
- **Withdrawal list.** The runtime mechanism for retracting content between releases.
- **Gradle wiring.** Today this runs by hand. It should be a build step so the bundle can never be
  stale relative to the content.
