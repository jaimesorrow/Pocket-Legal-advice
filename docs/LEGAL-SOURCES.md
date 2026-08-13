# Incorporating Statutes, Regulations and Case Law

How primary legal authority enters the app, and — more importantly — the role it is allowed to play
once it is here. Read `AI-REQUIREMENTS.md` first; rules 1, 12, 14 and 15 govern everything below.

---

## The trap

The intuitive plan is: ingest a large corpus of statutes and case law, retrieve the relevant passages
for a user's situation, and show them. That plan makes the app **more** dangerous, not less. It should
be rejected explicitly so it doesn't get proposed again in six months as a RAG feature.

Three reasons.

**Raw law is not self-interpreting.** A statute read in isolation routinely means something different
from what it says. Exceptions live in other sections. Definitions live in a definitions section.
Courts have narrowed or expanded it. It may have been held unconstitutional and never repealed. A
layperson reading the plain text reaches a confident wrong conclusion, and we handed it to them.

**Case law decays.** An opinion may have been overruled, superseded by statute, limited to its facts,
depublished, or marked non-precedential. Nothing in the text of the opinion tells you this. The
document reads exactly the same the day before and the day after it stops being good law.

**Retrieval plus generation is exactly what rule 1 forbids.** Quoting retrieved passages, summarising
them, or letting a model select which passage is "relevant" reintroduces the failure mode the whole
architecture exists to remove. Retrieved text is not verified text merely because it came from a real
document.

### A worked example, from this repository

The test fixture contains this string, labelled `RAW_API_DESCRIPTION` — the unverified model output
the tests require us to discard:

> "Under *Gerstein v. Pugh* (1975), a warrantless arrest requires a prompt judicial determination of
> probable cause, generally within 48 hours."

It is wrong, in the specific way legal text is usually wrong: fluently, with a real citation attached.
*Gerstein v. Pugh*, 420 U.S. 103 (1975), required a **prompt** determination but never quantified it.
The 48-hour benchmark comes from *County of Riverside v. McLaughlin*, 500 U.S. 44 (1991), sixteen
years later — which also placed the burden on the government to justify longer delays. The sentence
attributes to one case a holding that belongs to another.

Two further conflations are worth noticing in the same fixture. The verified string beside it cites
Cal. Penal Code § 825, an **arraignment** deadline — a different requirement, on a different clock,
with different consequences, from the Fourth Amendment probable-cause determination in
Gerstein/McLaughlin. And the raw text also travels with a suggested remedy (a motion to suppress)
whose availability does not follow automatically from the violation.

Nobody would catch any of this by reading. That is the point, that is why the trust boundary exists,
and that is why a corpus of accurate primary sources does not by itself produce accurate guidance.
*(Both legal points above should still be confirmed by counsel before they inform content — I am
illustrating a failure mode, not supplying the law.)*

---

## The role law actually plays

Primary authority enters the app in three roles, none of which is "content the user reads instead of
plain-language guidance."

**1. Provenance.** Every verified entry cites the authority it rests on, pinned to a specific version.
This is what makes rule 14 real: guidance is traceable to a statute at a known revision and to the
attorney who read it.

**2. Change detection.** Tracked authorities are monitored. When the underlying text changes, every
entry citing it is flagged for re-review and stops being served until a human clears it. This is where
automation genuinely earns its place — it is a diffing problem, not a judgement problem.

**3. An optional "read the actual law" affordance.** Beneath the plain-language guidance, a link to
the official source, clearly framed as the underlying law rather than as advice. Link out to the
canonical government URL rather than reproducing text in-app, so the user always sees the current
version and we are not in the business of republishing.

---

## The hard constraint: there is no reliable free citator

The single most important question in legal research is *"is this still good law?"* Answering it is
what Shepard's (LexisNexis) and KeyCite (Westlaw) do, at thousands of dollars per seat per year, using
human editorial classification of how every later case treats every earlier one.

The free tools do not replace this:

- **CourtListener** and **Google Scholar** show you *citing* cases — they do not classify treatment.
  Useful for triage; you still have to read the opinions.
- **Free Law Project announced an AI-assisted citator in mid-2025** that classifies treatment signals.
  Genuinely promising and worth tracking, but an AI-generated treatment classification is precisely
  the kind of output this app is built not to trust blindly. Use it to *surface candidates for human
  review*, never as the authority that content is current.

**Design consequence.** Because we cannot automate "is this still good law," we do not build features
that depend on knowing it at runtime. Instead: keep the cited authority set small, keep review cycles
short (rule 15), and prefer statutory citations — which change on a legislative record we can diff —
over case-law citations, which decay invisibly. Where guidance genuinely rests on case law, that entry
gets a shorter review interval.

---

## Source inventory

Verify current terms before building against any of these; APIs and licensing change.

### Federal statutes and regulations — solved

| Source | What | Notes |
| --- | --- | --- |
| **govinfo.gov** (GPO) | US Code, CFR, Public Laws, Statutes at Large, Federal Register | Official. Bulk XML repository at `govinfo.gov/bulkdata`; USLM XML for public laws (113th Congress forward) and parts of the CFR. The authoritative federal source. |
| **eCFR.gov** | Current CFR, continuously updated | Better than the annual CFR edition for "what is the rule today." |
| **federalregister.gov** | Rules and notices | Has bulk data and a developer API. |
| **congress.gov API** | Bills, legislative status | For tracking pending changes to law you cite. |

Federal government works are not subject to copyright, so reuse is unencumbered.

### Case law — solved, and better than it used to be

| Source | What | Notes |
| --- | --- | --- |
| **CourtListener** (Free Law Project) | ~10 million decisions, 2,000+ courts | REST API v4 plus quarterly bulk CSV downloads. Since March 2024 it includes the full Harvard Caselaw Access Project corpus. Open-source, non-profit. **Note:** as of May 2026 FLP lowered default API rate limits and moved full API access into membership — budget for a membership if you build on the API, or use bulk downloads. |
| **Caselaw Access Project** | 6.9m cases, 360 years | Restrictions expired March 2024; fully open. Native search retired September 2024 — the data now lives in CourtListener and on Hugging Face. |

### State law — this is where the work is

There is no federal-quality solution. Expect this to be the expensive part.

- **Coverage is fragmented.** Most states do not publish codified statutes as bulk data. Some do run
  good official sites (California's `leginfo.legislature.ca.gov`, the New York Senate's open API).
- **Copyright is a genuine minefield.** Several states contract with LexisNexis or Thomson West to
  publish their *official* annotated codes, and assert rights in them. *Georgia v. Public.Resource.Org*
  (2020) held the annotations in Georgia's official code uncopyrightable under the government edicts
  doctrine, but that does not make every state's compilation freely reusable, and it does not settle
  the terms attached to any particular state's website.
- **Aggregators exist but vary.** Open States covers legislatures and bills more than codified
  statutes. Various open-data projects publish compiled state codes under permissive licences with
  differing freshness and completeness.

**Recommendation.** For the launch jurisdiction, do not build a scraper. Have counsel work from the
official state source, and record the citation and canonical URL by hand. For one state and a few
dozen violation keys this is a spreadsheet, not a data pipeline. Build ingestion automation only when
you add your third jurisdiction and the manual cost is proven.

**Before any state ingestion, get a written answer to:** may we reproduce this text, must we link
rather than copy, and what attribution is required? That question goes to counsel, not to engineering.

---

## Architecture: build-time ingestion, not runtime lookup

The app never queries a legal database at runtime. Ingestion happens in a separate tool, produces a
reviewed and versioned content bundle, and that bundle ships with the app.

```
official sources          ingestion tool (separate repo)         app
─────────────────         ──────────────────────────────         ───
govinfo / eCFR      ──┐
CourtListener       ──┼──▶  fetch → normalise → hash        ──▶  signed content bundle
state official site ──┘        │                                  (ships in APK)
                               ▼
                        attorney authoring + review              runtime: local DB only,
                               │                                 no network for content
                               ▼
                        content bundle + provenance
```

Why build-time:

- **Offline-first survives.** The verified database works in a holding cell with no signal — the
  situation where the app matters most.
- **Nothing unreviewed can ever reach a user.** A runtime lookup is a path for unreviewed text to
  appear on screen. There is no such path if the only content source is the shipped bundle.
- **Reproducibility.** A given app version maps to exactly one content version, so you can always
  answer "what did this user see?"

The one runtime exception is a **withdrawal list**: a small signed file the app checks opportunistically
when online, naming content entries pulled since the last release. This lets you retract bad guidance
within hours instead of waiting on a store review. Offline, the app falls back to the review-by dates
baked into the bundle (rule 15).

---

## Data model

Sketch, to be firmed up when the persistence layer is written.

```kotlin
enum class AuthorityType { STATUTE, REGULATION, CASE, CONSTITUTIONAL, COURT_RULE }

/** A specific version of a specific piece of primary law. */
data class LegalAuthority(
    val id: String,               // stable internal id
    val type: AuthorityType,
    val jurisdiction: String,     // "US", "US-CA"
    val citation: String,         // "Cal. Penal Code § 825"
    val title: String,
    val officialUrl: String,      // canonical government URL, shown to users
    val sourceSystem: String,     // "govinfo" | "courtlistener" | "leginfo.ca.gov"
    val sourceId: String,         // upstream identifier
    val versionLabel: String,     // "2026 ed." / opinion date
    val effectiveDate: LocalDate?,
    val retrievedAt: Instant,
    val textHash: String,         // change detection only — text is NOT displayed
)

/** Verified guidance. The only thing a user ever reads. */
data class ActionStepEntity(
    val violationKey: String,
    val jurisdiction: String,           // part of the lookup key — AI rule 12
    val actionSteps: List<String>,
    val description: String,
    val authorityIds: List<String>,     // what this guidance rests on — AI rule 14
    val deadline: DeadlineSpec? = null, // AI rule 5
    val authoredBy: String,
    val reviewedBy: String,
    val reviewedAt: LocalDate,
    val reviewByDate: LocalDate,        // expiry — AI rule 15
    val contentVersion: Int,
)
```

**Two consequences for the existing test suite**, worth knowing before you start:

1. The 21 existing tests construct `ActionStepEntity` with named arguments for `violationKey`,
   `actionSteps` and `description`. Adding fields with default values keeps them compiling.
2. Making jurisdiction part of the lookup key changes `ActionStepDao.getActionStepsByKey(key)` to take
   a jurisdiction too. That signature change **does** touch the existing tests. Do it deliberately and
   update them — do not work around it with a post-filter, which is the failure rule 12 describes.

`textHash` deserves emphasis: we store a hash of the authority's text, not the text. We are not
republishing law, and the hash is all change detection needs.

---

## Change detection workflow

1. A scheduled job re-fetches each tracked authority from its official source.
2. It compares the new hash against the stored one.
3. On a change: every `ActionStepEntity` citing that authority is flagged, a review task is opened
   with a diff of what changed, and the affected entries are added to the withdrawal list.
4. Flagged entries stop serving. The app abstains for those keys rather than showing unreviewed
   guidance (rules 2 and 15).
5. An attorney reviews, updates the guidance if needed, and clears the flag with a new `reviewedAt`.

Note what is automated and what is not: **detection is automated, judgement is not.** The job never
edits content. Its only powers are to flag and to withdraw — both fail-safe directions.

For case-law-backed entries, add a periodic check of citing opinions (via CourtListener) as a
*prompt for human review*, never as an automated currency signal. See the citator section.

---

## Where to start

1. **Pick the launch jurisdiction** and enumerate 20–40 violation keys with counsel. That closed
   vocabulary is also what constrains the classifier (rule 1), so it is on the critical path anyway.
2. **Hand-record authorities for those keys** — citation, official URL, version, retrieval date. A
   spreadsheet is the correct tool at this size.
3. **Write the authoring and review format** — probably YAML or JSON in version control, so review is
   a pull request and the history is the audit trail rule 14 requires.
4. **Build the bundle compiler**: authoring files → validated, signed content bundle → app asset.
   Validation enforces the invariants (every entry has authorities, a reviewer, a review-by date, a
   jurisdiction).
5. **Add change detection** for federal authorities first, since govinfo and CourtListener make it
   straightforward, then state sources as coverage allows.
6. **Automate state ingestion only when manual cost is proven** — realistically, jurisdiction three.

Steps 1–4 are what stand between the current empty database and a shippable app. None of them require
a scraper, an embedding index, or a retrieval system.
