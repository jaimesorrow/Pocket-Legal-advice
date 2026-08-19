# Authoring Format

One YAML file per `(violation_key, jurisdiction)` pair, living under `content/<jurisdiction>/`.
These files are the source of truth for every word the app shows a user. Changes to them are reviewed
as pull requests — the git history *is* the audit trail required by AI rule 14.

File naming: `content/us-ca/VIOLATION_48_HOUR_ARRAIGNMENT.yaml`

---

## Example

```yaml
violation_key: VIOLATION_48_HOUR_ARRAIGNMENT
jurisdiction: US-CA
content_version: 1

description: >-
  The police must bring you before a judge within a set time after arresting you.
  If they held you longer than that, your rights may have been violated.

action_steps:
  - Ask for a copy of your booking record. It shows the exact time you were arrested.
  - Tell your lawyer how long you waited before you saw a judge.
  - Write down anything you said to police after the deadline passed, and when you said it.

authorities:
  - type: CASE
    citation: County of Riverside v. McLaughlin, 500 U.S. 44 (1991)
    title: Probable cause determinations must generally occur within 48 hours of arrest
    official_url: https://www.courtlistener.com/opinion/112594/county-of-riverside-v-mclaughlin/
    source_system: courtlistener
    source_id: "112594"
    version_label: "500 U.S. 44 (1991)"
    retrieved_at: 2026-08-19
    text_hash: "sha256:0000000000000000000000000000000000000000000000000000000000000000"

deadline:
  unit: HOURS
  amount: 48
  trigger: ARREST
  lost_if_missed: >-
    After this point it becomes much harder to challenge how long you were held.

authored_by: A. Author, State Bar #000000
reviewed_by: R. Reviewer, State Bar #000000
reviewed_at: 2026-08-19
review_by: 2027-02-19
```

---

## Fields

### Required, top level

| Field | Type | Notes |
| --- | --- | --- |
| `violation_key` | string | `VIOLATION_` followed by `A–Z`, `0–9`, `_`. Must match the filename. This is the closed vocabulary the classifier is restricted to (AI rule 1). |
| `jurisdiction` | string | `US` for federal, or `US-XX` for a state. Part of the lookup key, never a post-filter (AI rule 12). |
| `content_version` | integer | Increment on any change to user-visible text. |
| `description` | string | Plain-language explanation. Target grade 6–8 reading level (AI rule 17). |
| `action_steps` | list of strings | At least one. Each is a single concrete action. |
| `authorities` | list | At least one. See below. |
| `authored_by` | string | Name and bar number of the authoring attorney. |
| `reviewed_by` | string | Name and bar number of the reviewing attorney. Must differ from `authored_by`. |
| `reviewed_at` | date | When review completed. Cannot be in the future. |
| `review_by` | date | Expiry. Past this date the entry stops being served (AI rule 15). |

### `authorities[]`

| Field | Type | Notes |
| --- | --- | --- |
| `type` | enum | `STATUTE`, `REGULATION`, `CASE`, `CONSTITUTIONAL`, `COURT_RULE`. |
| `citation` | string | Full citation as it should be displayed. |
| `title` | string | Short human-readable summary of what this authority says. |
| `official_url` | string | Canonical government or CourtListener URL. Must be `https://`. |
| `source_system` | string | e.g. `govinfo`, `courtlistener`, `leginfo.ca.gov`. |
| `source_id` | string | Upstream identifier, for change detection. |
| `version_label` | string | Edition or opinion date. |
| `retrieved_at` | date | When the text behind `text_hash` was fetched. |
| `text_hash` | string | `sha256:` + 64 hex chars. **A hash of the authority text, not the text.** We do not republish law — the hash exists only so change detection can diff. |
| `effective_date` | date | Optional. |

### `deadline` — optional

Include only when the violation carries a statutory clock (AI rule 5).

| Field | Type | Notes |
| --- | --- | --- |
| `unit` | enum | `HOURS`, `DAYS`, `MONTHS`, `YEARS`. |
| `amount` | integer | Positive. |
| `trigger` | enum | `ARREST`, `BOOKING`, `CITATION`, `INCIDENT`, `NOTICE`, `FILING`. |
| `lost_if_missed` | string | Plain language: what the person loses when it expires. |

### `status` — optional

Only value is `EXAMPLE`, which marks a file as non-shippable. The compiler refuses to build such files
into a bundle unless `--allow-examples` is passed, which production builds never do. Used for the test
fixtures in this directory.

---

## What the compiler rejects

These fail the build (`exit 1`):

- A `violation_key` that doesn't match its filename, or doesn't match the required pattern.
- A duplicate `(violation_key, jurisdiction)` pair.
- An entry with zero authorities, or an authority missing a required field.
- `official_url` that isn't `https://`.
- A malformed `text_hash`.
- `review_by` on or before `reviewed_at`.
- `reviewed_at` in the future.
- `reviewed_by` identical to `authored_by` — review means a second pair of eyes.
- Placeholder text anywhere in user-visible copy: `TODO`, `TBD`, `FIXME`, `XXX`, `lorem ipsum`,
  `example.com`, `PLACEHOLDER`. Per DESIGN.md, unreviewed filler must never reach a build a real
  person can install.
- An `EXAMPLE`-status file, unless `--allow-examples`.

These warn, and fail only under `--strict` (which CI uses):

- Estimated reading level above grade 8 (AI rule 17).
- `review_by` within 30 days — the entry is about to stop serving.
- A case-law-backed entry with a review interval longer than 180 days. Case law decays invisibly and
  there is no reliable free citator, so these need shorter cycles. See `docs/LEGAL-SOURCES.md`.

Expired entries — `review_by` in the past — are **not** an error. They are reported and dropped from
the bundle. That is AI rule 15 working as intended: the app abstains rather than showing guidance
nobody has confirmed is still accurate.
