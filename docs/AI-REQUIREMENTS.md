# AI Requirements — Non-Negotiable

This document defines the constraints on every AI/model-backed feature in Pocket Legal Advice.

**These are not guidelines.** A change that violates any rule here does not ship, regardless of how
much it improves accuracy, engagement, or coverage. If a requirement here conflicts with a product
goal, the requirement wins and the goal changes.

The reason for the severity: the person using this app may have been arrested in the last few hours.
They may be in custody. A statutory clock may be running. Wrong information does not cost them a bad
user experience — it costs them evidence, a motion, or their liberty. And their own words, typed into
this app, can be used against them.

> **On legal specifics in this document:** examples are illustrative only. The
> `VIOLATION_48_HOUR_ARRAIGNMENT` key, the `Gerstein v. Pugh` reference and the `Cal. Penal Code § 825`
> citation are carried over from the existing test fixture — they have not been verified by this
> project. Every legal specific that reaches a user must be authored and signed off by qualified
> counsel for the specific jurisdiction. See rule 14.

---

## Tier 1 — Protecting the user from the model

### 1. The model classifies. It never speaks.

**Rule.** The model's only permitted contribution to the user experience is the selection of one or
more violation keys from a closed, versioned vocabulary. No model-generated string is ever rendered,
spoken, logged into user-visible history, or exported.

**Why.** This is the founding architectural decision, already encoded in `LegalAnalysisViewModelTest`.
A language model asked about law will produce fluent, plausible, well-cited text that is wrong often
enough to be dangerous, and the user has no way to tell the difference. Removing generated prose from
the output surface removes the entire class of failure.

**Enforcement.** Two-sided, because either side alone is insufficient.
- *Server side:* constrained decoding / structured output schema restricting the key field to the
  enum. The model should not be physically able to return an off-vocabulary key.
- *Device side:* `LegalAnalysisRepository` validates every returned key against the locally compiled
  enum and discards unknown keys silently. Never trust the server's promise.

**Test.** Existing: raw `title`, `description` and `recommendation` must not appear in UI state, even
as substrings. Add: a response containing a key not in the local enum produces no result and does not
crash.

---

### 2. "I don't know" is a first-class, successful answer.

**Rule.** Abstention is a normal outcome, not an error and not a fallback. `NoVerifiedData` must be
reachable, styled as a legitimate destination, and never presented as a failure of the app or the user.

**Why.** Systems that treat abstention as failure quietly learn to guess. In this domain a guess is
the worst possible output — it is confidently wrong advice to someone with no way to check it. The
app's credibility rests on it being visibly willing to say nothing.

**Enforcement.** `LegalAnalysisUiState.NoVerifiedData` is distinct from `Error`. It routes the user to
human help (rule 4) rather than to a retry button. Never A/B test abstention rate downward.

**Test.** Existing: unknown key excluded from results; API-returned key with no DB match surfaces no
raw text.

---

### 3. Low confidence abstains. The threshold is conservative and it is not tuned for coverage.

**Rule.** Every classification carries a confidence score. Below the threshold, the result is dropped
before it reaches the repository's DB lookup. The threshold is set to minimize false positives, and
changing it requires the same review as changing legal content.

**Why.** A borderline match on a rights violation is not a "maybe useful suggestion" — it sends
someone to their attorney with a wrong theory, or worse, gives them false reassurance that nothing
went wrong when something did.

**Enforcement.** Confidence is part of `ViolationDetail`. Filtering happens in the repository, above
the DAO, so an unconfident key never even triggers a lookup.

**Test.** A response below threshold produces `NoVerifiedData` and zero DAO calls.

---

### 4. Emergencies bypass the pipeline entirely.

**Rule.** Before classification, input is screened for indicators of active crisis — imminent physical
danger, medical emergency in custody, self-harm, an ongoing assault, a deadline expiring within hours.
On detection the app immediately surfaces direct human contact routes and does **not** show action
steps.

**Why.** Someone typing "they won't give him his insulin" does not need a motion to suppress. This is
the single most common way well-intentioned legal tools fail their users: correctly answering the
question that was asked while missing the emergency in it.

**Enforcement.** A separate, local, deterministic screen (keyword and pattern based, not the remote
model) running before any network call. It must work offline. False positives here are acceptable;
false negatives are not.

**Test.** Crisis-indicator inputs produce the emergency surface, make no API call, and make no DAO
call.

---

### 5. If the matched violation carries a clock, the clock is the headline.

**Rule.** Where verified content records a statutory deadline, the app computes and displays the
remaining time above the action steps, and states plainly what is lost when it expires.

**Why.** Deadlines are where this app can change an outcome. Many jurisdictions impose very short
windows — notice-of-claim requirements against government entities are measured in weeks or months and
are routinely missed by people who had a valid claim. Action steps delivered after the window closes
are worthless.

**Enforcement.** Deadline metadata lives on the verified content entity, never computed by the model.
Where the trigger date is unknown, the app asks the user for it rather than assuming.

**Test.** Content with deadline metadata renders the deadline above steps; content without it renders
no deadline UI and no placeholder.

---

## Tier 2 — Protecting the user from their own words

The description someone types here is a self-authored account of events they may later be prosecuted
for. Treat it as the most sensitive data in the app.

### 6. Personal identifiers are stripped on-device before transmission.

**Rule.** Names, addresses, phone numbers, case and booking numbers, dates of birth and badge numbers
are redacted locally before the description leaves the device. Redaction happens before the network
call, not at the server.

**Why.** The classifier does not need to know who anyone is to recognize an arraignment delay.
Anything transmitted can be breached, subpoenaed or logged by an intermediary.

**Enforcement.** A redaction pass between the ViewModel and the API client. It is part of the
repository's contract, not an optional interceptor.

**Test.** Descriptions containing identifiers reach `LegalApiService` with those spans replaced.

---

### 7. Zero server-side retention of user descriptions. No training on user input. Ever.

**Rule.** The analysis service does not persist request bodies, does not log them, and does not use
them for training, evaluation or fine-tuning. This is a contractual requirement on any third-party
model provider, not just our own code.

**Why.** A retained corpus of "here is what I did and what the police did" is a discovery target that
exists solely because our users trusted us. The correct amount of it to hold is none.

**Enforcement.** Provider selection requires zero-retention terms in writing. Verify it before rule 4
of the roadmap is implemented, because it constrains which providers are eligible.

---

### 8. Local history is encrypted, and destroying it takes one action.

**Rule.** Any stored history is encrypted at rest. A clearly labelled control deletes everything
immediately, with no dark-pattern confirmation friction and no soft-delete.

**Why.** Phones get seized, borrowed and shoulder-surfed. A user must be able to make this app
forget, quickly, without hunting through settings.

---

### 9. Show what leaves the device, before it leaves.

**Rule.** Prior to the first transmission the user sees exactly what will be sent, in its redacted
form, and confirms. Privacy copy states plainly what is sent, what is kept and for how long.

**Why.** Informed consent, and it is also the honest version of a trust claim. Showing the redacted
payload is far more convincing than a privacy policy nobody reads.

---

## Tier 3 — Not practicing law

### 10. No outcome prediction. No case-strength assessment.

**Rule.** The app never estimates likelihood of success, never characterizes a case as strong or weak,
never suggests what a court will do. Prohibited both as generated text and as verified content.

**Why.** It is unreliable, and it is the clearest line into rendering legal advice rather than legal
information. It also causes real harm in both directions: false hope, or a valid claim abandoned.

---

### 11. No individual attorney referrals. No fee estimates.

**Rule.** Refer to categories and institutions — public defender's office, legal aid organizations,
the state bar referral service. Never a named practitioner, never a predicted cost.

---

### 12. Jurisdiction gate: outside coverage, the app abstains completely.

**Rule.** If the user's jurisdiction is not covered by reviewed content, the app says so and refers
them onward. It never applies one jurisdiction's content to another, and it never silently defaults.

**Why.** Rights, deadlines and procedures differ by state. Showing California content to someone in
Texas is delivering wrong law with full confidence — the exact failure the architecture exists to
prevent, arriving through a data-modelling shortcut instead of through the model.

**Enforcement.** Jurisdiction is a required dimension on verified content and part of the DAO lookup
key, not a filter applied afterwards.

---

### 13. The disclaimer is persistent, not a one-time gate.

**Rule.** Every screen displaying legal guidance carries the disclaimer in view. It is not a
first-launch modal the user dismisses forever.

**Why.** People screenshot these screens and send them to family. The screenshot must carry the
disclaimer with it.

---

## Tier 4 — Integrity of the verified content

The architecture's guarantee is only as good as the database behind it. These rules protect that.

### 14. Everything displayed is traceable to a reviewer and a date.

**Rule.** Each verified content entry records: authoring attorney, reviewing attorney, review date,
jurisdiction, source citations, and a content version. Any displayed guidance can be traced back to
who approved it and when.

**Why.** When law changes you must know precisely which entries are affected and who signed off. It is
also the only credible answer to "where did this come from?"

---

### 15. Content expires. Stale entries are withdrawn, not shown.

**Rule.** Entries carry a review-by date. Past it, the entry stops being served and the app abstains
rather than showing guidance nobody has confirmed is still accurate.

**Why.** Silently serving three-year-old procedure after a statute is amended is how a well-built
system starts causing harm without anyone noticing.

---

### 16. The model version is pinned and recorded. Changing it re-runs the evaluation suite.

**Rule.** The classifier version is fixed and logged with every classification. Upgrading it requires
re-running the full evaluation set, including the abstention and emergency-detection cases, before
release.

**Why.** A model swap silently changes classification behaviour across every user. Without an eval
gate, the first signal of a regression is a user acting on a wrong key.

---

### 17. Reading level is a release criterion.

**Rule.** Verified content targets roughly a 6th–8th grade reading level and is reviewed for it. Legal
citations may appear alongside plain-language text, never instead of it.

**Why.** The audience includes people in acute stress, people reading in a second language, and people
who did not finish school. Content that is accurate but unreadable has failed, and in this context
failing quietly is still failing.

---

## Explicitly out of scope, permanently

- Chat. There is no free-form conversational surface, because there is no safe way to constrain one to
  verified content.
- Document drafting. The app does not generate motions, letters or filings.
- Voice interaction that produces generated speech. Reading verified content aloud is fine; generating
  spoken answers is not.
- Any feature whose value depends on the model producing prose the user reads.

---

## How to review a change against this document

1. Does any model output reach the user as text? → Reject.
2. Can it produce a confident answer where the correct answer is "I don't know"? → Reject.
3. Does it transmit, retain or log more of the user's account than before? → Justify against Tier 2 or
   reject.
4. Does it show guidance without a jurisdiction, a reviewer and a review date behind it? → Reject.
5. Does it work when offline, and when the deadline is the point? → If not, fix before shipping.
