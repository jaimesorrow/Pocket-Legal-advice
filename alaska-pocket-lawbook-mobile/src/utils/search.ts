import { LEGAL_ENTRIES } from '../data/legalData';
import { LegalEntry } from '../data/types';

function normalize(value: string): string {
  return value.toLowerCase().replace(/[.,§]/g, '').replace(/\s+/g, ' ').trim();
}

/**
 * Client-side search over citation, title, classification, and keywords.
 * Matches on substring so partial statute numbers (e.g. "AS 34.03") and
 * plain-language phrases (e.g. "landlord repairs") both work.
 */
export function searchLegalEntries(query: string): LegalEntry[] {
  const q = normalize(query);
  if (!q) return [];

  return LEGAL_ENTRIES.filter((entry) => {
    const haystack = normalize(
      [
        entry.citation,
        entry.title,
        entry.approvedSummary,
        ...entry.keywords,
        ...entry.relatedCitations,
      ].join(' ')
    );
    return haystack.includes(q);
  }).sort((a, b) => {
    // Exact/near citation matches first, then title matches, then the rest.
    const aCitation = normalize(a.citation).includes(q);
    const bCitation = normalize(b.citation).includes(q);
    if (aCitation !== bCitation) return aCitation ? -1 : 1;
    const aTitle = normalize(a.title).includes(q);
    const bTitle = normalize(b.title).includes(q);
    if (aTitle !== bTitle) return aTitle ? -1 : 1;
    return 0;
  });
}
