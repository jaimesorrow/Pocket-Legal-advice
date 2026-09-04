---
name: pr-writer
description: Draft pull request descriptions matching this repo's conventions. Include triggers: "write a PR description", "create a PR title", "describe this change", "summarize this PR".
---

## TITLE FORMAT
Plain imperative sentence, capitalized, no ticket number. Multi-part changes join clauses with a comma or semicolon rather than a title-level bullet list. A minority of older CI/test-infra PRs use a lowercase conventional-commit prefix (`fix:`, `ci:`, `test:`), but recent PRs drop it. Real examples:
- "Add project-specific code-review skill for the legal pipeline" (#10)
- "Back ActionStepDao with Room instead of an in-memory map" (#9)
- "Prune nav to shipped features, disable no-op refresh job" (#11)

## DESCRIPTION SECTIONS
`## Summary` (or `## What`) — bulleted concrete changes, grouped under bold phase labels for multi-part work (#12: "**Phase 0 — reconcile app to shipped feature set:**"). `## Test plan` — checkbox list (`- [ ]`/`- [x]`) naming the exact command and its outcome. Ends with the Claude Code attribution trailer. Small single-purpose PRs (#4, #6, #11) skip headers entirely and use plain prose instead.

## DETAIL LEVEL
Name the exact classes/files touched, e.g. #12 names `OnDeviceLegalAnalyzer`, `AnalysisScreen`, `VerifiedContentSeed` directly. State explicitly what's NOT done and why (#12: "Not done, and not something this PR can finish: attorney review..."). Always say whether `./gradlew test`/`lint` actually ran, or couldn't (#9, #12: "no Android SDK available in this environment") — never imply untested work passed.

## TONE & AUDIENCE
Technical, direct, no marketing language. Written for the repo owner reviewing their own Claude-authored PRs as a decision record, not for release-notes automation — no such tooling exists in this repo.

## CHECKLIST
The `## Test plan` checkbox list is the only recurring pre-merge checklist. Mark an unrun item `- [ ]` with the blocking reason stated inline (e.g. sandbox has no Android SDK); do not omit it silently.
