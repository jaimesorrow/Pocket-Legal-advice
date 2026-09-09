import { Classification, LegalEntry } from './types';

export const ALASKA_CLASSIFICATIONS: Classification[] = [
  { id: 'ak-criminal', title: 'Criminal law', jurisdiction: 'ALASKA' },
  { id: 'ak-police', title: 'Arrests and police encounters', jurisdiction: 'ALASKA' },
  { id: 'ak-housing', title: 'Housing and eviction', jurisdiction: 'ALASKA' },
  { id: 'ak-employment', title: 'Employment', jurisdiction: 'ALASKA' },
  { id: 'ak-family', title: 'Family law', jurisdiction: 'ALASKA' },
  { id: 'ak-consumer', title: 'Consumer protection', jurisdiction: 'ALASKA' },
  { id: 'ak-benefits', title: 'Public benefits', jurisdiction: 'ALASKA' },
  { id: 'ak-property', title: 'Property and land', jurisdiction: 'ALASKA' },
  { id: 'ak-traffic', title: 'Traffic and vehicles', jurisdiction: 'ALASKA' },
  { id: 'ak-civil-rights', title: 'Civil rights', jurisdiction: 'ALASKA' },
  { id: 'ak-court-rules', title: 'Alaska court rules', jurisdiction: 'ALASKA' },
];

export const FEDERAL_CLASSIFICATIONS: Classification[] = [
  { id: 'fed-constitutional', title: 'Constitutional rights', jurisdiction: 'FEDERAL' },
  { id: 'fed-civil-rights', title: 'Civil rights', jurisdiction: 'FEDERAL' },
  { id: 'fed-criminal-procedure', title: 'Criminal procedure', jurisdiction: 'FEDERAL' },
  { id: 'fed-employment', title: 'Employment', jurisdiction: 'FEDERAL' },
  { id: 'fed-housing', title: 'Housing', jurisdiction: 'FEDERAL' },
  { id: 'fed-immigration', title: 'Immigration', jurisdiction: 'FEDERAL' },
  { id: 'fed-consumer', title: 'Consumer protection', jurisdiction: 'FEDERAL' },
  { id: 'fed-bankruptcy', title: 'Bankruptcy', jurisdiction: 'FEDERAL' },
  { id: 'fed-benefits', title: 'Federal benefits', jurisdiction: 'FEDERAL' },
  { id: 'fed-taxes', title: 'Taxes', jurisdiction: 'FEDERAL' },
  { id: 'fed-disability', title: 'Disability rights', jurisdiction: 'FEDERAL' },
  { id: 'fed-regulations', title: 'Federal regulations', jurisdiction: 'FEDERAL' },
];

export const CLASSIFICATIONS = [
  ...ALASKA_CLASSIFICATIONS,
  ...FEDERAL_CLASSIFICATIONS,
];

export const LEGAL_ENTRIES: LegalEntry[] = [
  {
    id: 'as-34-03-070',
    jurisdiction: 'ALASKA',
    classification: 'ak-housing',
    sourceType: 'STATUTE',
    citation: 'AS 34.03.070',
    title: 'Landlord to maintain fit premises',
    approvedSummary:
      'Alaska landlords covered by this chapter generally must comply with applicable housing codes, keep premises habitable, keep common areas clean and safe, maintain supplied systems and appliances, provide for waste removal, and supply running water, hot water, and heat subject to statutory exceptions.',
    appliesTo:
      'Residential landlords and rental premises governed by Alaska’s Uniform Residential Landlord and Tenant Act.',
    exceptions: [
      'The chapter contains exclusions and special rules that can affect whether a rental is covered.',
      'The statute includes exceptions for some tenant-controlled utility arrangements and certain water or hot-water arrangements.',
    ],
    keywords: [
      'eviction',
      'tenant rights',
      'landlord repairs',
      'repairs',
      'heat',
      'hot water',
      'habitable',
      'rental',
      'housing',
    ],
    relatedCitations: ['AS 34.03.100', 'AS 34.03.160'],
    officialUrl: 'https://www.akleg.gov/statutesPDF/Title-34.pdf',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
  {
    id: 'as-11-41',
    jurisdiction: 'ALASKA',
    classification: 'ak-criminal',
    sourceType: 'STATUTE_CHAPTER',
    citation: 'AS 11.41',
    title: 'Offenses against the person',
    approvedSummary:
      'AS 11.41 is the Alaska criminal-code chapter covering offenses against people, including homicide, assault, kidnapping and related offenses, sexual offenses, robbery, and other person-focused crimes.',
    appliesTo: 'Conduct prosecuted under Alaska state criminal law when the elements of a listed offense are met.',
    exceptions: [
      'Each offense has its own required elements, definitions, defenses, and penalty provisions.',
      'This chapter heading is not a substitute for reading the specific section that applies to the alleged conduct.',
    ],
    keywords: ['criminal law', 'assault', 'homicide', 'robbery', 'sexual offense', 'kidnapping', 'AS 11.41'],
    relatedCitations: ['AS 11.41.100', 'AS 11.41.200', 'AS 11.41.300', 'AS 11.41.500'],
    officialUrl: 'https://www.akleg.gov/statutesPDF/Title-11.pdf',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
  {
    id: 'usc-18-242',
    jurisdiction: 'FEDERAL',
    classification: 'fed-civil-rights',
    sourceType: 'STATUTE',
    citation: '18 U.S.C. § 242',
    title: 'Deprivation of rights under color of law',
    approvedSummary:
      'This federal criminal statute prohibits willful deprivation of federally protected rights by a person acting under color of law. Penalties increase in specified circumstances, including when bodily injury or certain aggravated conduct is involved.',
    appliesTo:
      'People who act under color of law, including qualifying government officials or others exercising governmental authority.',
    exceptions: [
      'The government must prove the elements of the criminal offense, including willfulness.',
      'Section 242 is a criminal statute; it is not itself a general private civil damages claim.',
    ],
    keywords: ['police misconduct', 'civil rights', 'color of law', 'police abuse', 'government official', '18 USC 242'],
    relatedCitations: ['42 U.S.C. § 1983'],
    officialUrl: 'https://uscode.house.gov/view.xhtml?req=%28title%3A18+section%3A242+edition%3Aprelim%29',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
  {
    id: 'usc-29-207',
    jurisdiction: 'FEDERAL',
    classification: 'fed-employment',
    sourceType: 'STATUTE',
    citation: '29 U.S.C. § 207',
    title: 'Maximum hours and federal overtime',
    approvedSummary:
      'Covered, nonexempt employees generally must receive at least one and one-half times their regular rate for covered work over 40 hours in a workweek. The statute also contains special rules and multiple exemptions.',
    appliesTo: 'Employees and employers covered by the Fair Labor Standards Act, subject to exemptions and special provisions.',
    exceptions: [
      'Not every worker or employer is covered by the same overtime rule.',
      'Exemptions, industry-specific provisions, and rules for calculating the regular rate can change the result.',
    ],
    keywords: ['overtime', 'time and a half', '40 hours', 'wages', 'employment', 'FLSA', '29 USC 207'],
    relatedCitations: ['29 U.S.C. § 213'],
    officialUrl: 'https://uscode.house.gov/view.xhtml?req=%28title%3A29+section%3A207+edition%3Aprelim%29',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
  {
    id: 'usc-42-3604',
    jurisdiction: 'FEDERAL',
    classification: 'fed-housing',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 3604',
    title: 'Fair housing: prohibited discrimination',
    approvedSummary:
      'Federal fair-housing law prohibits specified discrimination in the sale or rental of dwellings and related terms, services, advertising, and availability. It also includes disability-related protections such as reasonable accommodations in covered circumstances.',
    appliesTo: 'Housing transactions and dwellings covered by the federal Fair Housing Act.',
    exceptions: [
      'Coverage is subject to statutory exemptions and related sections, including 42 U.S.C. §§ 3603 and 3607.',
      'Different subsections protect different characteristics and regulate different housing practices.',
    ],
    keywords: ['housing discrimination', 'fair housing', 'disability accommodation', 'rent', 'landlord', '42 USC 3604'],
    relatedCitations: ['42 U.S.C. § 3603', '42 U.S.C. § 3607'],
    officialUrl: 'https://uscode.house.gov/view.xhtml?req=%28title%3A42+section%3A3604+edition%3Aprelim%29',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
  {
    id: 'constitution-amendment-4',
    jurisdiction: 'FEDERAL',
    classification: 'fed-constitutional',
    sourceType: 'CONSTITUTION',
    citation: 'U.S. Const. amend. IV',
    title: 'Fourth Amendment — searches and seizures',
    approvedSummary:
      'The Fourth Amendment protects against unreasonable searches and seizures and sets requirements for warrants, including probable cause and particular description of the place to be searched and the persons or things to be seized.',
    appliesTo: 'Government searches and seizures when Fourth Amendment protections apply.',
    exceptions: [
      'Whether a search or seizure is reasonable depends on the facts and on constitutional doctrines developed by the courts.',
      'There are recognized warrant exceptions; this entry does not attempt to list or apply them to a specific situation.',
    ],
    keywords: ['police search', 'search warrant', 'probable cause', 'search and seizure', 'fourth amendment', 'police encounter'],
    relatedCitations: ['U.S. Const. amend. XIV'],
    officialUrl: 'https://constitution.congress.gov/constitution/amendment-4/',
    lastReviewedDate: '2026-09-06',
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: '1.0.0',
  },
];

export const NO_VERIFIED_MATCH_MESSAGE =
  'We could not find a verified match. This does not mean no law applies. Try different words, search the statute number, or contact a qualified attorney or legal aid organization.';

const normalize = (value: string) =>
  value
    .toLowerCase()
    .replace(/§/g, ' ')
    .replace(/[^a-z0-9.]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

export function searchLegalEntries(query: string): LegalEntry[] {
  const needle = normalize(query);
  if (!needle) return [];

  return LEGAL_ENTRIES.filter((entry) => {
    const haystack = normalize(
      [
        entry.citation,
        entry.title,
        entry.approvedSummary,
        entry.appliesTo,
        ...entry.keywords,
        ...entry.relatedCitations,
      ].join(' '),
    );
    return haystack.includes(needle) || needle.split(' ').every((part) => haystack.includes(part));
  });
}

export function entriesForClassification(classificationId: string): LegalEntry[] {
  return LEGAL_ENTRIES.filter((entry) => entry.classification === classificationId);
}
