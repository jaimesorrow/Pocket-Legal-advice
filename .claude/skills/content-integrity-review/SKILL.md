---
name: content-integrity-review
description: Reviews changes to Alaska's Pocket Lawbook's verified legal content — VerifiedContentSeed.kt, LegalDocuments.kt, and OnDeviceLegalAnalyzer's keyword rules — against the citation, jurisdiction, and non-attorney-reviewed invariants this repo depends on. Use this instead of, or alongside, pocket-lawbook-review for any change adding or editing an ActionStepEntity, a LegalDocuments Document/Section, or a MatchRule.
---

This is the one place in the repo where a wrong or invented legal claim can actually get committed —
the whole zero-hallucination pipeline exists to keep that from happening downstream, but nothing
guards the content going *in*. Review every change here as if it will be read by someone in
crisis, because per `VerifiedContentSeed`'s own warning, it eventually will be.

## Checks

1. **Every `description` must be a real, checkable citation**, not a paraphrase: a case cite
   (`Name v. Name, Vol Reporter Page (Court Year)`), an Alaska Statute cite (`AS XX.XX.XXX(x)`), or
   a constitutional cite (`U.S. Const. amend. X`). Flag any entry whose legal claim isn't traceable
   to one of these. Never accept a citation "from memory" — the header explicitly requires primary
   sources, transcribed; if a PR description doesn't say where a new citation was verified, ask.
2. **`jurisdiction` must match what the citation actually is** — a 9th Circuit or U.S. Supreme Court
   case is `FEDERAL`; an Alaska Statute or Alaska court case is `ALASKA`. Get this wrong and the UI
   tells the user the wrong body of law an answer rests on.
3. **`violationKey` must be new and unique.** Never repurpose an existing key's meaning —
   `LegalAnalysisViewModelTest` and `OnDeviceLegalAnalyzerTest` assert against specific keys, and a
   key silently changing meaning breaks that contract invisibly.
4. **Every `VerifiedContentSeed.KEY_*` needs a matching `OnDeviceLegalAnalyzer.MatchRule`, and vice
   versa.** A seed entry with no rule is unreachable content; a rule with no seed entry means
   `LegalAnalysisRepository.analyzeSituation`'s `mapNotNull` silently drops the match — the same
   "acceptable" failure mode the pipeline uses for genuinely unverified keys, but here it would be
   masking a wiring bug instead. Check both directions on every diff to either file.
5. **`actionSteps` stay in the "document and consult an attorney" register** — write down dates,
   photograph, tell your attorney, contact Alaska Legal Services Corporation. Flag any step that
   predicts a legal outcome or tells the user what a court will do; per `LegalDocuments.acceptableUse`,
   presenting this app's output as advice risks the unauthorized practice of law.
6. **Nothing here may claim to be attorney-reviewed.** `VerifiedContentSeed` has no
   effective-date/last-reviewed-date fields yet — flag any PR description, commit message, or code
   comment implying real review happened; that requires adding those fields and a real attorney
   sign-off, not just merging content.
7. **In `LegalDocuments.kt`: don't fill in a placeholder quietly.** `PROVIDER_NAME`,
   `CONTACT_EMAIL`, `EFFECTIVE_DATE`, and any `[ATTORNEY: ...]` bracketed note exist so nobody ships
   them by accident. A PR that resolves one must say a licensed Alaska attorney approved that exact
   change, not just supply a plausible-looking value.
8. **Bump `LegalDocuments.VERSION` on any substantive text change** — it's what makes
   `ConsentRepository`'s re-acceptance meaningful. A content change that doesn't bump it means
   users who already accepted silently keep access to changed terms they never saw.
