export type Jurisdiction = 'ALASKA' | 'FEDERAL';

export type ReviewStatus = 'NOT_ATTORNEY_REVIEWED' | 'ATTORNEY_REVIEWED';

export type SourceType = 'STATUTE' | 'REGULATION' | 'CONSTITUTION' | 'COURT_RULE';

/** A classification groups related statutes under one browsable topic, e.g. "Housing and eviction". */
export interface Classification {
  id: string;
  jurisdiction: Jurisdiction;
  name: string;
}

/** A single verified legal entry: one statute, regulation, or rule, tied to one classification. */
export interface LegalEntry {
  id: string;
  jurisdiction: Jurisdiction;
  classificationId: string;
  sourceType: SourceType;
  citation: string;
  title: string;
  /** Plain-language summary. Informational only — not legal advice. */
  approvedSummary: string;
  whoItAppliesTo: string;
  importantExceptions: string;
  keywords: string[];
  relatedCitations: string[];
  officialUrl: string;
  effectiveDate: string;
  lastReviewedDate: string;
  reviewStatus: ReviewStatus;
  contentVersion: number;
}
