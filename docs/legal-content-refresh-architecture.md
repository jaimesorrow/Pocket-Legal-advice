# Daily Legal Content Refresh Architecture

## Goal

Pocket Legal Advice must automatically check authoritative Alaska and federal legal sources every day and make newly verified legal information available to the Android app without allowing unverified or generated legal prose to reach users.

## Trust boundary

The Android client is never the authority for legal freshness. A backend update worker fetches authoritative sources, computes changes, validates jurisdiction and metadata, and publishes only a versioned verified dataset. The Android app downloads a dataset only after integrity and schema checks.

Remote/LLM output may identify stable violation keys, but it must never supply user-facing legal text. Existing repository invariants remain unchanged.

## Source metadata

Every published legal record must contain:

- stable violation/content key
- jurisdiction: `ALASKA` or `FEDERAL`
- source type
- authoritative citation
- authoritative source URL
- effective date when known
- last verified timestamp
- content version
- source content hash
- publication status
- previous version identifier when applicable

## Daily workflow

1. A backend scheduled job runs at least once every 24 hours.
2. Each configured authoritative source is fetched.
3. Source retrieval is logged with timestamp, status, and content hash.
4. Retrieved material is normalized deterministically.
5. The normalized source is compared with the last accepted version.
6. Unchanged sources are recorded as checked with no publication.
7. Changed sources enter validation.
8. Validation rejects unknown jurisdictions, missing citations, malformed records, missing source metadata, and content that cannot be tied to an authoritative source.
9. A validated change receives a new immutable content version.
10. The new dataset is published atomically.
11. Failed validation never replaces the last known-good dataset.
12. A high-impact change may be promoted outside the normal daily schedule through the same validation/publishing path.

## Android workflow

Use WorkManager for the client-side freshness check. The worker should run approximately daily when Android permits background execution, require network connectivity, and never block normal app startup.

The worker:

1. Requests the current dataset manifest.
2. Compares the manifest version/hash with the active Room dataset.
3. Downloads a new package only when needed.
4. Validates package schema, jurisdiction, version, and cryptographic hash/signature.
5. Writes the new dataset transactionally.
6. Activates the new version only after the transaction succeeds.
7. Retains the previous known-good version for rollback.
8. Records last successful sync and last failed sync.

If synchronization fails, the app continues using the last verified local dataset and clearly reports its verification date when appropriate.

## Rollback

Datasets are immutable. The active version pointer can be moved back to the previous known-good version. A bad or incomplete update must never partially replace the active dataset.

## User-facing freshness

The UI should expose:

- jurisdiction
- last checked timestamp
- last successful update timestamp
- active legal content version
- source information

Avoid an absolute claim that the app contains every legal change immediately. Use wording such as: `Automatically checked against configured authoritative sources` and show the last successful verification time.

## Initial implementation scope

Implement the Android synchronization contract, Room version metadata, WorkManager scheduling, atomic dataset activation, rollback metadata, and tests first. Backend source fetchers/publishers can then implement individual authoritative sources without changing the Android trust boundary.
