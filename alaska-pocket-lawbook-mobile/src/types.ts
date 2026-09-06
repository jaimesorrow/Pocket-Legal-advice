export type Jurisdiction = 'ALASKA' | 'FEDERAL';

export type ReviewStatus =
  | 'ATTORNEY_REVIEWED'
  | 'NOT_ATTORNEY_REVIEWED'
  | 'REVIEW_REQUIRED';

export type SourceType =
  | 'STATUTE'
  | 'STATUTE_CHAPTER'
  | 'CONSTITUTION'
  | 'REGULATION'
  | 'COURT_RULE';

export type Classification = {
  id: string;
  title: string;
  jurisdiction: Jurisdiction;
};

export type LegalEntry = {
  id: string;
  jurisdiction: Jurisdiction;
  classification: string;
  sourceType: SourceType;
  citation: string;
  title: string;
  approvedSummary: string;
  appliesTo: string;
  exceptions: string[];
  keywords: string[];
  relatedCitations: string[];
  officialUrl: string;
  effectiveDate?: string;
  lastReviewedDate: string;
  reviewStatus: ReviewStatus;
  contentVersion: string;
};

export type Screen =
  | { kind: 'HOME' }
  | { kind: 'CLASSIFICATION'; classificationId: string }
  | { kind: 'ENTRY'; entryId: string };
