# Alaska's Pocket Lawbook Mobile App

## Purpose

Build a mobile legal-information library dedicated only to Alaska state law and United States federal law. The app provides legal information, not legal advice.

## Main screen

The home screen is a searchable content library. Alaska classifications appear before federal classifications.

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

## Search

Search by statute number, legal citation, law title, plain-language keyword, or related phrase. Results show title, jurisdiction, classification, citation, plain-language summary, and an official source link.

Examples include `AS 11.41`, `AS 34.03.070`, `18 U.S.C. § 242`, `eviction`, `landlord repairs`, `police search`, `overtime`, and `tenant rights`.

## Classification screens

Selecting a classification opens a separate screen. Information is separated by statute or source. Each detail view supports official citation, title, plain-language summary, who the rule applies to, important exceptions, related citations, official source link, jurisdiction, review date, review status, and content version.

## Legal safety

The app must:

- Cover Alaska and federal law only
- Clearly label Alaska-specific and federal information
- Link to official sources
- Show the date content was reviewed
- Show when content has not been attorney-reviewed
- State that the app provides legal information, not legal advice
- State that the app does not create an attorney-client relationship
- Avoid presenting unrestricted AI-generated legal text as authoritative
- Never show a blank result that could be mistaken for “no law applies”

When no verified result is found, show:

> We could not find a verified match. This does not mean no law applies. Try different words, search the statute number, or contact a qualified attorney or legal aid organization.

## Data structure

Each legal entry supports:

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

Valid jurisdictions are `ALASKA` and `FEDERAL`.

## Design

Use a clean, trustworthy, accessible mobile design with large readable text, clear jurisdiction labels, search at the top, easy-to-tap classification rows, clear statute headings, strong source-link buttons, high contrast, and accessible touch targets. Reuse the existing Alaska's Pocket Lawbook blue/gold branding.

## Out of scope

Do not add authentication, billing, paywalls, fake subscriptions, unrestricted legal chatbot responses, nationwide state-law coverage, or unreviewed legal content presented as authoritative.
