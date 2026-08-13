# Feature Set

What the app does, in priority order. Every feature here is subject to `AI-REQUIREMENTS.md`; several
of the most valuable ones involve no AI at all.

Status legend: **Spec'd** = defined by the existing test suite · **Planned** = agreed, not written ·
**Proposed** = worth building, needs a decision.

---

## Core — the analysis path

### 1. Situation analysis → verified action steps · Spec'd
The founding feature, fully specified by `LegalAnalysisViewModelTest`. User describes what happened;
the classifier identifies which rights issues apply; the app displays reviewed action steps drawn
entirely from local content. Covered by 21 existing tests.

### 2. The honest refusal · Spec'd
When no reviewed guidance matches, the app says so and routes to human help. `NoVerifiedData` exists
in the state model. This is a feature, not an error path — see AI rule 2.

### 3. Deadline surfacing · Planned
Where matched content carries a statutory clock, compute and display the time remaining and what
expires with it. The highest-leverage feature in the app: deadlines are where information changes
outcomes. Requires deadline metadata on the content entity and, where the trigger date is unknown,
asking the user for it. See AI rule 5.

### 4. Emergency screening · Planned
Local, deterministic, pre-classification screen for active crisis, routing directly to human help and
bypassing the analysis pipeline. Works offline. See AI rule 4.

---

## Offline — works with no signal, no account

These need no model and no network, and they may be the most-used part of the app. Build them early;
they make the app useful even before the classifier exists.

### 5. Know-your-rights cards · Proposed
Short, reviewed, offline reference cards for the moments that matter: a traffic stop, a knock at the
door, being questioned, being arrested. Plain language, one screen each, no analysis required.

### 6. Invoke-your-rights script · Proposed
The exact words to say to stop questioning and request counsel, on screen, readable aloud, in large
type. Reviewed content, no generation. Simple to build and genuinely protective.

### 7. Legal aid directory · Planned
Public defender offices, legal aid organizations and bar referral lines for the covered jurisdiction,
bundled offline with the app. Every abstention and emergency route lands here, so it is a dependency
of features 2 and 4 rather than an extra.

---

## Documentation aids — help them preserve the facts

### 8. Evidence checklist · Proposed
A reviewed, per-violation checklist of what to obtain and preserve — booking record, arrest report,
names and badge numbers, medical records, witness contacts — with space to tick items off. Derived
from verified content, keyed by violation, never generated.

### 9. Timeline capture · Proposed
Structured entry of what happened and when — stop, arrest, transport, booking, first appearance.
Feeds the deadline calculator, and produces something an attorney can actually use. Stored encrypted
locally (AI rule 8).

### 10. Attorney handoff export · Proposed
Export a clean summary — the user's timeline, the identified issues with citations, the checklist
status — as a PDF or shareable text, carrying the disclaimer and review dates. This is the app's
natural endpoint: get the person to a lawyer better prepared than they arrived.

---

## Trust surfaces

### 11. Redacted-payload preview · Planned
Before the first transmission, show exactly what will be sent in its redacted form. AI rule 9.

### 12. Provenance display · Planned
Every guidance block shows its jurisdiction, review date and citation, inside the screenshot boundary.
Tapping through shows the full source list. AI rules 13 and 14.

### 13. One-tap erase · Planned
Destroys all local history immediately. Reachable in one action from the main screen, not buried in
settings. AI rule 8.

### 14. Quick exit · Proposed
Leaves the app and clears visible state instantly.

---

## Deliberately not building

Listed so the decisions don't get relitigated. Rationale in `AI-REQUIREMENTS.md`.

- A chat assistant.
- Document and motion drafting.
- Case outcome prediction or strength scoring.
- Named attorney referrals or fee estimates.
- Any social, sharing-to-feed or gamification surface.
- Multi-jurisdiction "best guess" when the user's jurisdiction isn't covered.

---

## Suggested build order

Features 1 and 2 are already specified and should be implemented directly against the existing tests.
Then 5, 6 and 7 — offline, no model dependency, immediately useful, and they give the abstention path
somewhere to land. Then 3 and 4, which carry the most safety weight. Documentation aids (8–10) are the
differentiator once the foundation holds. Trust surfaces (11–13) ship alongside the first build that
transmits anything.
