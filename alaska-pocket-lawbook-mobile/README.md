# Alaska's Pocket Lawbook Mobile

Expo + TypeScript mobile app for a searchable Alaska and United States federal legal-information library.

## What is implemented

- Search-first home screen
- Alaska classifications before federal classifications
- Search by citation, statute number, title, summary, keyword, and related phrase
- Separate classification screens
- Statute/source detail screens
- Alaska vs. federal jurisdiction badges
- Official-source buttons
- Last-reviewed date, review status, and content version on each entry
- Required no-match warning so an empty result is never presented as “no law applies”
- Legal-information / no-attorney-client-relationship notices
- No authentication, billing, paywalls, subscriptions, or unrestricted AI legal chat

## Start the app

```bash
npm install
npx expo start
```

Then open the project in Expo Go, an Android emulator, or an iOS simulator.

## Type-check

```bash
npm run typecheck
```

## Content policy

The initial records in `src/data.ts` are a small starter library, not a complete statement of Alaska or federal law. Every record links to an official source and carries a review status. Starter summaries are explicitly marked `NOT_ATTORNEY_REVIEWED`.

Add new content only after checking the official source. Do not use unrestricted model-generated legal text as authoritative content.

## Data shape

Each legal entry supports:

- `jurisdiction`
- `classification`
- `sourceType`
- `citation`
- `title`
- `approvedSummary`
- `appliesTo`
- `exceptions`
- `keywords`
- `relatedCitations`
- `officialUrl`
- `effectiveDate`
- `lastReviewedDate`
- `reviewStatus`
- `contentVersion`

Valid jurisdictions are `ALASKA` and `FEDERAL`.
