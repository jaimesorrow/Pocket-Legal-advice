# Verified Legal Content

Every word the app shows a user comes from this directory. Nothing else is a source of user-visible
legal text — not the classifier, not a retrieved document, not a fallback string in the code.

**This directory is intentionally empty of content right now.** The app's architecture is complete;
the database it protects is not. Filling this is the work that makes the app worth shipping.

## Layout

```
content/
  us-ca/
    VIOLATION_48_HOUR_ARRAIGNMENT.yaml
    VIOLATION_MIRANDA_RIGHTS.yaml
  us-ny/
    VIOLATION_48_HOUR_ARRAIGNMENT.yaml     # same key, different jurisdiction, different content
```

One file per `(violation_key, jurisdiction)`. Filename must match the key. Format is documented in
`tools/content-compiler/SCHEMA.md`, with a worked example in `tools/content-compiler/fixtures/`.

## Before adding anything here

1. **It must be authored and reviewed by licensed attorneys**, and by two different people. The
   compiler rejects an entry whose author and reviewer are the same.
2. **It must cite its authority** — statute, regulation, case or court rule, with the official URL and
   the version it was read at.
3. **It must carry a review-by date.** Past that date the entry stops being served and the app
   abstains, rather than showing guidance nobody has confirmed is current.
4. **It must be readable.** Target grade 6–8. The compiler warns above grade 8 and CI fails on it.

## Checking your work

```bash
python3 tools/content-compiler/compile_content.py --check
```

No Android toolchain needed — Python 3.11 and PyYAML is the whole requirement.

## Why review happens in pull requests

The git history of this directory *is* the audit trail required by AI rule 14. When a statute changes
and you need to know who approved the affected guidance and when, that answer has to exist. Review in
PRs, keep the history, don't rewrite it.

## What must never go here

Placeholder content, draft content, "we'll fix it before launch" content. The compiler blocks the
obvious markers (`TODO`, `TBD`, `example.com`, `lorem ipsum`), but it cannot detect plausible-sounding
text that simply hasn't been checked — and that is the exact failure mode this whole architecture
exists to prevent. See the worked example in `docs/LEGAL-SOURCES.md`: the wrong version of a citation
reads exactly as convincingly as the right one.

If it has not been reviewed, it does not go in this directory.
