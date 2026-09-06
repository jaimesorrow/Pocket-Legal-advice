# Alaska's Pocket Lawbook Mobile App

## Purpose

Build a mobile legal-information library dedicated only to:

1. Alaska state law
2. United States federal law

The app must provide legal information, not legal advice.

## Main screen

The home screen must be a searchable content library.

Display content in this order:

### Alaska law

- Criminal law
- Arrests and police encounters
- Housing and eviction
- Employment
- Family law
- Consumer protection
- Public benefits
- Property and land
- Traffic and vehicles
- Civil rights
- Alaska court rules

### Federal law

- Constitutional rights
- Civil rights
- Criminal procedure
- Employment
- Housing
- Immigration
- Consumer protection
- Bankruptcy
- Federal benefits
- Taxes
- Disability rights
- Federal regulations

Alaska classifications must appear before federal classifications.

## Search

The main screen must allow users to search by:

- Statute number
- Legal citation
- Law title
- Plain-language keyword
- Related phrase

Examples:

- AS 11.41
- AS 34.03.070
- 18 U.S.C. § 242
- eviction
- landlord repairs
- police search
- overtime
- tenant rights

Search results must show:

- Law title
- Jurisdiction
- Classification
- Statute or regulation citation
- Plain-language summary
- Official source link

Search must identify whether the result is Alaska or federal law.

## Classification screens

Selecting a classification must open a separate screen.

Each classification screen must contain information separated by statute.

Example:

### Alaska Housing Law

#### AS 34.03.070 — Landlord obligations

Show:

- Official citation
- Title
- Plain-language summary
- Who the rule applies to
- Important exceptions
- Related statutes
- Official source link
- Jurisdiction
- Review date

## Legal safety

The app must:

- Cover Alaska and federal law only
- Clearly label Alaska-specific information
- Clearly label federal information
- Link to official sources
- Show the date content was reviewed
- Show when content has not been attorney-reviewed
- State that the app provides legal information, not legal advice
- State that the app does not create an attorney-client relationship
- Avoid presenting unrestricted AI-generated legal text as authoritative
- Never show a blank result that could be mistaken for “no law applies”

When no verified result is found, show:

“We could not find a verified match. This does not mean no law applies. Try different words, search the statute number, or contact a qualified attorney or legal aid organization.”

## Data structure

Each legal entry should support:

- jurisdiction
- classification
- source type
- citation
- title
- approved summary
- keywords
- related citations
- official URL
- effective date
- last reviewed date
- review status
- content version

Valid jurisdictions:

- ALASKA
- FEDERAL

## Design

Use a clean, trustworthy, accessible mobile design.

Prioritize:

- Large readable text
- Clear jurisdiction labels
- Search at the top of the home screen
- Easy-to-tap classification rows
- Clear statute headings
- Strong source-link buttons
- High contrast
- Accessible touch targets
- No misleading premium or AI-chat placeholders

Reuse the existing Alaska's Pocket Lawbook colors, typography, and branding where available.

## Out of scope

Do not add:

- Authentication
- Billing
- Paywalls
- Fake subscriptions
- Unrestricted legal chatbot responses
- Nationwide state-law coverage
- Unreviewed legal content presented as authoritative
