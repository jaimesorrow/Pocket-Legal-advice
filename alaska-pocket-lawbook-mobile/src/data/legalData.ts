import { Classification, LegalEntry } from './types';

/**
 * ─────────────────────────────────────────────────────────────────────────
 * NOT YET ATTORNEY-REVIEWED.
 *
 * Every entry below is transcribed from memory of primary legal sources
 * (Alaska Statutes, the Alaska Constitution, the U.S. Code, the U.S.
 * Constitution, and reported case law) rather than looked up live, so
 * citation numbers or details could be stale or wrong. `officialUrl` on
 * every entry points to the primary source so a reader — or a reviewing
 * attorney — can check the current text directly. Do not treat this
 * dataset as authoritative, and do not ship it to a real user without a
 * licensed Alaska attorney's review of every entry. See CLAUDE.md.
 * ─────────────────────────────────────────────────────────────────────────
 */

const NOT_VERIFIED_DATE = 'Not independently verified — see official source';
const LAST_REVIEWED = '2026-09-06';

export const CLASSIFICATIONS: Classification[] = [
  // Alaska law, in the order the brief specifies
  { id: 'ak-criminal-law', jurisdiction: 'ALASKA', name: 'Criminal law' },
  { id: 'ak-arrests-police', jurisdiction: 'ALASKA', name: 'Arrests and police encounters' },
  { id: 'ak-housing-eviction', jurisdiction: 'ALASKA', name: 'Housing and eviction' },
  { id: 'ak-employment', jurisdiction: 'ALASKA', name: 'Employment' },
  { id: 'ak-family-law', jurisdiction: 'ALASKA', name: 'Family law' },
  { id: 'ak-consumer-protection', jurisdiction: 'ALASKA', name: 'Consumer protection' },
  { id: 'ak-public-benefits', jurisdiction: 'ALASKA', name: 'Public benefits' },
  { id: 'ak-property-land', jurisdiction: 'ALASKA', name: 'Property and land' },
  { id: 'ak-traffic-vehicles', jurisdiction: 'ALASKA', name: 'Traffic and vehicles' },
  { id: 'ak-civil-rights', jurisdiction: 'ALASKA', name: 'Civil rights' },
  { id: 'ak-court-rules', jurisdiction: 'ALASKA', name: 'Alaska court rules' },

  // Federal law, in the order the brief specifies
  { id: 'fed-constitutional-rights', jurisdiction: 'FEDERAL', name: 'Constitutional rights' },
  { id: 'fed-civil-rights', jurisdiction: 'FEDERAL', name: 'Civil rights' },
  { id: 'fed-criminal-procedure', jurisdiction: 'FEDERAL', name: 'Criminal procedure' },
  { id: 'fed-employment', jurisdiction: 'FEDERAL', name: 'Employment' },
  { id: 'fed-housing', jurisdiction: 'FEDERAL', name: 'Housing' },
  { id: 'fed-immigration', jurisdiction: 'FEDERAL', name: 'Immigration' },
  { id: 'fed-consumer-protection', jurisdiction: 'FEDERAL', name: 'Consumer protection' },
  { id: 'fed-bankruptcy', jurisdiction: 'FEDERAL', name: 'Bankruptcy' },
  { id: 'fed-federal-benefits', jurisdiction: 'FEDERAL', name: 'Federal benefits' },
  { id: 'fed-taxes', jurisdiction: 'FEDERAL', name: 'Taxes' },
  { id: 'fed-disability-rights', jurisdiction: 'FEDERAL', name: 'Disability rights' },
  { id: 'fed-federal-regulations', jurisdiction: 'FEDERAL', name: 'Federal regulations' },
];

const GENERIC_EXCEPTIONS =
  'This summary does not list every exception. Statutes are frequently amended and courts ' +
  'interpret them in ways a summary cannot capture — read the official text and, for anything ' +
  'that affects you personally, talk to a licensed attorney.';

export const LEGAL_ENTRIES: LegalEntry[] = [
  // ── Alaska: Criminal law ─────────────────────────────────────────────
  {
    id: 'ak-crim-murder1',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-criminal-law',
    sourceType: 'STATUTE',
    citation: 'AS 11.41.100',
    title: 'Murder in the first degree',
    approvedSummary:
      'Defines the most serious homicide offense under Alaska law, based on intentionally ' +
      'causing another person’s death under the circumstances the statute specifies.',
    whoItAppliesTo: 'Anyone charged with intentionally causing another person’s death.',
    importantExceptions:
      'Alaska law separates homicide into several degrees (murder, manslaughter, criminally ' +
      'negligent homicide) with different intent requirements and penalties. ' + GENERIC_EXCEPTIONS,
    keywords: ['murder', 'homicide', 'killing', 'first degree murder', 'as 11.41.100'],
    relatedCitations: ['AS 11.41.110', 'AS 11.41.120'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-crim-theft',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-criminal-law',
    sourceType: 'STATUTE',
    citation: 'AS 11.46.100',
    title: 'Theft (consolidated offense)',
    approvedSummary:
      'Alaska combines the historic crimes of larceny, embezzlement, and related property ' +
      'offenses into a single crime of "theft." The degree of the offense, and the penalty, is ' +
      'set mainly by the value of the property or services taken.',
    whoItAppliesTo: 'Anyone charged with unlawfully taking or exercising control over property or services.',
    importantExceptions:
      'The dollar-value thresholds separating theft degrees are set in statute and have changed ' +
      'over time. ' + GENERIC_EXCEPTIONS,
    keywords: ['theft', 'larceny', 'stealing', 'shoplifting', 'as 11.46.100'],
    relatedCitations: ['AS 11.46.120', 'AS 11.46.130', 'AS 11.46.140', 'AS 11.46.150'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Arrests and police encounters ───────────────────────────
  {
    id: 'ak-arrest-warrantless',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-arrests-police',
    sourceType: 'STATUTE',
    citation: 'AS 12.25.030',
    title: 'Arrest by a peace officer without a warrant',
    approvedSummary:
      'Sets out the circumstances under which an Alaska peace officer may arrest someone without ' +
      'first obtaining a warrant from a judge — generally covering offenses committed in the ' +
      'officer’s presence, or where the officer has probable cause as to certain crimes.',
    whoItAppliesTo: 'Alaska peace officers making an arrest, and anyone they arrest.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['arrest', 'warrantless arrest', 'probable cause', 'police', 'as 12.25.030'],
    relatedCitations: ['AS 12.25.150', 'U.S. Const. amend. IV'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-arrest-searches',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-arrests-police',
    sourceType: 'CONSTITUTION',
    citation: 'Alaska Const. art. I, § 14',
    title: 'Searches and seizures',
    approvedSummary:
      'Alaska’s own constitutional guarantee against unreasonable searches and seizures, ' +
      'paralleling (and, in some Alaska Supreme Court decisions, interpreted more protectively ' +
      'than) the Fourth Amendment to the U.S. Constitution.',
    whoItAppliesTo: 'Anyone in Alaska subject to a search or seizure by government actors.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['search', 'seizure', 'police search', 'alaska constitution', 'privacy'],
    relatedCitations: ['U.S. Const. amend. IV'],
    officialUrl: 'https://courts.alaska.gov/',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Housing and eviction ────────────────────────────────────
  {
    id: 'ak-housing-obligations',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-housing-eviction',
    sourceType: 'STATUTE',
    citation: 'AS 34.03.070',
    title: 'Landlord obligations',
    approvedSummary:
      'Part of the Alaska Uniform Residential Landlord and Tenant Act. Sets out a landlord’s ' +
      'general duties to a tenant, including keeping the premises in a condition that complies ' +
      'with applicable housing codes.',
    whoItAppliesTo: 'Residential landlords and tenants covered by the Alaska URLTA.',
    importantExceptions:
      'The URLTA does not cover every kind of tenancy (for example, some owner-occupied or ' +
      'short-term arrangements may be excluded). ' + GENERIC_EXCEPTIONS,
    keywords: ['landlord', 'tenant', 'rental', 'housing', 'repairs', 'as 34.03.070'],
    relatedCitations: ['AS 34.03.100', 'AS 34.03.020'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-housing-habitability',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-housing-eviction',
    sourceType: 'STATUTE',
    citation: 'AS 34.03.100',
    title: 'Landlord’s duty to maintain fit premises',
    approvedSummary:
      'Requires a landlord to maintain the premises in a fit and habitable condition, including ' +
      'supplying heat and running water where the rental agreement calls for the landlord to ' +
      'provide them.',
    whoItAppliesTo: 'Residential landlords and tenants covered by the Alaska URLTA.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['heat', 'habitability', 'water', 'repairs', 'landlord repairs', 'as 34.03.100'],
    relatedCitations: ['AS 34.03.070'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-housing-nonpayment',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-housing-eviction',
    sourceType: 'STATUTE',
    citation: 'AS 34.03.220(b)',
    title: 'Notice before termination for nonpayment of rent',
    approvedSummary:
      'If rent is unpaid, a landlord must give written notice of the nonpayment and of the ' +
      'intent to terminate the tenancy. The tenant then has 7 days after that notice to pay the ' +
      'rent in full before the landlord may terminate the rental agreement and seek possession.',
    whoItAppliesTo: 'Residential landlords and tenants covered by the Alaska URLTA.',
    importantExceptions:
      'Other lease violations (not nonpayment) can carry different notice periods. ' + GENERIC_EXCEPTIONS,
    keywords: ['eviction', 'nonpayment', 'rent', 'notice to quit', '7 day notice', 'as 34.03.220'],
    relatedCitations: ['AS 34.03.070', 'AS 09.45'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Employment ───────────────────────────────────────────────
  {
    id: 'ak-employment-minwage',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-employment',
    sourceType: 'STATUTE',
    citation: 'AS 23.10.065',
    title: 'Alaska minimum wage',
    approvedSummary:
      'Sets Alaska’s state minimum wage, which is set independently of (and has historically ' +
      'been higher than) the federal minimum wage under the Fair Labor Standards Act.',
    whoItAppliesTo: 'Most employers and employees in Alaska, subject to statutory exemptions.',
    importantExceptions:
      'Certain categories of workers may be exempt. The current dollar amount changes over time ' +
      '— check the official text. ' + GENERIC_EXCEPTIONS,
    keywords: ['minimum wage', 'wages', 'pay', 'as 23.10.065'],
    relatedCitations: ['29 U.S.C. § 206', 'AS 23.10.060'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-employment-overtime',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-employment',
    sourceType: 'STATUTE',
    citation: 'AS 23.10.060',
    title: 'Overtime compensation',
    approvedSummary:
      'Generally requires covered employers to pay one and one-half times an employee’s ' +
      'regular rate for hours worked beyond 8 in a day or 40 in a week, whichever is greater.',
    whoItAppliesTo: 'Most hourly employees in Alaska, subject to statutory exemptions.',
    importantExceptions:
      'Several categories of employees (for example, certain salaried, executive, or seasonal ' +
      'workers) are exempt from overtime rules. ' + GENERIC_EXCEPTIONS,
    keywords: ['overtime', 'time and a half', 'hours worked', 'wage and hour', 'as 23.10.060'],
    relatedCitations: ['29 U.S.C. § 207', 'AS 23.10.065'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Family law ───────────────────────────────────────────────
  {
    id: 'ak-family-custody',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-family-law',
    sourceType: 'STATUTE',
    citation: 'AS 25.24.150',
    title: 'Child custody: best interests of the child',
    approvedSummary:
      'Lists the factors an Alaska court considers when deciding child custody, all directed at ' +
      'the child’s best interests — including the child’s needs, each parent’s ' +
      'capability, and the child’s preference where appropriate.',
    whoItAppliesTo: 'Parents in a custody dispute before an Alaska court.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['custody', 'child custody', 'best interests', 'divorce', 'as 25.24.150'],
    relatedCitations: ['AS 25.20', 'AS 25.24.160'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-family-divorce-judgment',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-family-law',
    sourceType: 'STATUTE',
    citation: 'AS 25.24.160',
    title: 'What a divorce judgment may provide',
    approvedSummary:
      'Sets out what an Alaska court may decide as part of a divorce judgment, including custody ' +
      'and support of children, spousal support, and division of property.',
    whoItAppliesTo: 'Married couples going through divorce in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['divorce', 'property division', 'spousal support', 'alimony', 'as 25.24.160'],
    relatedCitations: ['AS 25.24.150'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Consumer protection ──────────────────────────────────────
  {
    id: 'ak-consumer-unfair-practices',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-consumer-protection',
    sourceType: 'STATUTE',
    citation: 'AS 45.50.471',
    title: 'Unfair Trade Practices and Consumer Protection Act — unlawful acts',
    approvedSummary:
      'Lists acts and practices the Alaska Unfair Trade Practices and Consumer Protection Act ' +
      'treats as unlawful, such as deceptive representations about goods and services.',
    whoItAppliesTo: 'Businesses and consumers in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['consumer protection', 'unfair trade practices', 'deceptive', 'fraud', 'as 45.50.471'],
    relatedCitations: ['AS 45.50.531'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-consumer-private-action',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-consumer-protection',
    sourceType: 'STATUTE',
    citation: 'AS 45.50.531',
    title: 'Private right of action for unfair trade practices',
    approvedSummary:
      'Allows a person harmed by an unlawful act under the Unfair Trade Practices and Consumer ' +
      'Protection Act to bring a private civil lawsuit for damages.',
    whoItAppliesTo: 'Consumers harmed by an unfair or deceptive trade practice in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['consumer lawsuit', 'damages', 'private right of action', 'as 45.50.531'],
    relatedCitations: ['AS 45.50.471'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Public benefits ──────────────────────────────────────────
  {
    id: 'ak-benefits-atap',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-public-benefits',
    sourceType: 'STATUTE',
    citation: 'AS 47.27',
    title: 'Alaska Temporary Assistance Program (ATAP)',
    approvedSummary:
      'Establishes Alaska’s temporary cash assistance program for low-income families with ' +
      'children, Alaska’s version of the federal Temporary Assistance for Needy Families ' +
      '(TANF) program.',
    whoItAppliesTo: 'Low-income Alaska families with children who meet program eligibility rules.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['public assistance', 'welfare', 'atap', 'tanf', 'cash assistance', 'as 47.27'],
    relatedCitations: ['AS 47.25'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-benefits-adult-assistance',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-public-benefits',
    sourceType: 'STATUTE',
    citation: 'AS 47.25',
    title: 'Adult public assistance and general relief',
    approvedSummary:
      'Chapter of the Alaska welfare code covering state assistance programs for aged, blind, and ' +
      'disabled adults, and general relief assistance for other Alaskans in need.',
    whoItAppliesTo: 'Alaskans who qualify for adult public assistance or general relief.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['adult public assistance', 'general relief', 'disability benefits', 'as 47.25'],
    relatedCitations: ['AS 47.27'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Property and land ────────────────────────────────────────
  {
    id: 'ak-property-state-land',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-property-land',
    sourceType: 'STATUTE',
    citation: 'AS 38.05',
    title: 'State land',
    approvedSummary:
      'Chapter governing the management, sale, lease, and use of land owned by the State of ' +
      'Alaska, administered by the Department of Natural Resources.',
    whoItAppliesTo: 'Anyone applying for, leasing, or otherwise dealing with Alaska state land.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['state land', 'public land', 'land use', 'department of natural resources', 'as 38.05'],
    relatedCitations: ['AS 34.15'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-property-conveyances',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-property-land',
    sourceType: 'STATUTE',
    citation: 'AS 34.15',
    title: 'Conveyances of real property',
    approvedSummary:
      'Chapter covering how real property is legally conveyed (transferred) in Alaska, including ' +
      'requirements for deeds and recording.',
    whoItAppliesTo: 'Anyone buying, selling, or transferring real property in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['deed', 'conveyance', 'real property', 'recording', 'as 34.15'],
    relatedCitations: ['AS 38.05'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Traffic and vehicles ─────────────────────────────────────
  {
    id: 'ak-traffic-oui',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-traffic-vehicles',
    sourceType: 'STATUTE',
    citation: 'AS 28.35.030',
    title: 'Driving while under the influence',
    approvedSummary:
      'Alaska’s core OUI/DUI statute, prohibiting operating a motor vehicle while under the ' +
      'influence of alcohol or drugs, or with a blood alcohol content at or above the statutory limit.',
    whoItAppliesTo: 'Anyone operating a motor vehicle in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['dui', 'oui', 'drunk driving', 'blood alcohol', 'as 28.35.030'],
    relatedCitations: ['AS 28.15.011'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-traffic-license',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-traffic-vehicles',
    sourceType: 'STATUTE',
    citation: 'AS 28.15.011',
    title: 'Driver’s license required',
    approvedSummary:
      'Requires a person to hold a valid Alaska driver’s license (or a license recognized ' +
      'under reciprocity rules) before operating a motor vehicle on Alaska roads.',
    whoItAppliesTo: 'Anyone operating a motor vehicle on Alaska roads.',
    importantExceptions:
      'Certain vehicles, locations (e.g., private property), and license classes are treated ' +
      'differently. ' + GENERIC_EXCEPTIONS,
    keywords: ['driver license', 'license required', 'unlicensed driving', 'as 28.15.011'],
    relatedCitations: ['AS 28.35.030'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Civil rights ─────────────────────────────────────────────
  {
    id: 'ak-civilrights-employment',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-civil-rights',
    sourceType: 'STATUTE',
    citation: 'AS 18.80.220',
    title: 'Unlawful employment practices',
    approvedSummary:
      'Part of the Alaska Human Rights Act. Prohibits employment discrimination based on ' +
      'protected characteristics such as race, religion, sex, national origin, age, and disability.',
    whoItAppliesTo: 'Employers, employment agencies, and labor organizations in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['discrimination', 'employment discrimination', 'human rights act', 'as 18.80.220'],
    relatedCitations: ['AS 18.80.240', '42 U.S.C. § 2000e-2'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-civilrights-housing',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-civil-rights',
    sourceType: 'STATUTE',
    citation: 'AS 18.80.240',
    title: 'Unlawful practices in real property transactions',
    approvedSummary:
      'Part of the Alaska Human Rights Act. Prohibits discrimination in the sale or rental of ' +
      'real property based on protected characteristics.',
    whoItAppliesTo: 'Landlords, sellers, and real estate professionals in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['housing discrimination', 'fair housing', 'human rights act', 'as 18.80.240'],
    relatedCitations: ['AS 18.80.220', '42 U.S.C. § 3604'],
    officialUrl: 'https://www.akleg.gov/basis/statutes.asp',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Alaska: Alaska court rules ────────────────────────────────────────
  {
    id: 'ak-rules-initial-appearance',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-court-rules',
    sourceType: 'COURT_RULE',
    citation: 'Alaska R. Crim. P. 5',
    title: 'Initial appearance before a judicial officer',
    approvedSummary:
      'Requires a person arrested to be brought before a judicial officer without unnecessary ' +
      'delay, where they are advised of the charges and their rights.',
    whoItAppliesTo: 'Anyone arrested in Alaska.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['arraignment', 'initial appearance', 'court rule', '48 hour rule', 'alaska rule of criminal procedure 5'],
    relatedCitations: ['AS 12.25.030'],
    officialUrl: 'https://courts.alaska.gov/rules/',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'ak-rules-service',
    jurisdiction: 'ALASKA',
    classificationId: 'ak-court-rules',
    sourceType: 'COURT_RULE',
    citation: 'Alaska R. Civ. P. 4',
    title: 'Service of process',
    approvedSummary:
      'Sets out how a party must be formally notified of a civil lawsuit filed against them in ' +
      'Alaska state court.',
    whoItAppliesTo: 'Anyone filing or defending a civil lawsuit in Alaska state court.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['service of process', 'lawsuit', 'civil procedure', 'court rule', 'summons'],
    relatedCitations: [],
    officialUrl: 'https://courts.alaska.gov/rules/',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Constitutional rights ───────────────────────────────────
  {
    id: 'fed-const-fourth',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-constitutional-rights',
    sourceType: 'CONSTITUTION',
    citation: 'U.S. Const. amend. IV',
    title: 'Protection against unreasonable searches and seizures',
    approvedSummary:
      'Protects the right of people to be secure against unreasonable searches and seizures by ' +
      'the government, generally requiring a warrant based on probable cause.',
    whoItAppliesTo: 'Everyone in the United States, against government action.',
    importantExceptions:
      'Courts recognize a number of exceptions (consent, exigent circumstances, and others). ' + GENERIC_EXCEPTIONS,
    keywords: ['fourth amendment', 'search and seizure', 'warrant', 'probable cause'],
    relatedCitations: ['Alaska Const. art. I, § 14'],
    officialUrl: 'https://www.law.cornell.edu/constitution/fourth_amendment',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-const-fifth',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-constitutional-rights',
    sourceType: 'CONSTITUTION',
    citation: 'U.S. Const. amend. V',
    title: 'Due process and protection against self-incrimination',
    approvedSummary:
      'Guarantees due process of law, protects against being compelled to testify against ' +
      'yourself, and against double jeopardy (being tried twice for the same offense).',
    whoItAppliesTo: 'Everyone in the United States, against federal government action (extended ' +
      'to states through the Fourteenth Amendment).',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['fifth amendment', 'due process', 'self-incrimination', 'plead the fifth', 'double jeopardy'],
    relatedCitations: ['U.S. Const. amend. VI'],
    officialUrl: 'https://www.law.cornell.edu/constitution/fifth_amendment',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Civil rights ─────────────────────────────────────────────
  {
    id: 'fed-civilrights-242',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-civil-rights',
    sourceType: 'STATUTE',
    citation: '18 U.S.C. § 242',
    title: 'Deprivation of rights under color of law',
    approvedSummary:
      'Makes it a federal crime for someone acting under government authority (such as a police ' +
      'officer) to willfully deprive a person of rights protected by the Constitution or federal law.',
    whoItAppliesTo: 'Government actors, and people whose rights they violate.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['civil rights', 'color of law', 'police misconduct', 'excessive force', '18 usc 242'],
    relatedCitations: ['42 U.S.C. § 1983'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/18/242',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-civilrights-1983',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-civil-rights',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 1983',
    title: 'Civil action for deprivation of rights',
    approvedSummary:
      'Gives a person a civil (not criminal) right to sue a state or local government actor who ' +
      'violated their constitutional or federal statutory rights while acting under state authority.',
    whoItAppliesTo: 'People whose federal rights were violated by a state or local government actor.',
    importantExceptions:
      'Various immunities (such as qualified immunity) can limit these lawsuits. ' + GENERIC_EXCEPTIONS,
    keywords: ['section 1983', 'civil rights lawsuit', 'qualified immunity', 'police lawsuit'],
    relatedCitations: ['18 U.S.C. § 242'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/1983',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-civilrights-recording',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-civil-rights',
    sourceType: 'COURT_RULE',
    citation: 'Fordyce v. City of Seattle, 55 F.3d 436 (9th Cir. 1995)',
    title: 'Right to record police in public',
    approvedSummary:
      'The Ninth Circuit (which includes Alaska) recognized a First Amendment right to film ' +
      'matters of public interest, including police officers carrying out their duties in a ' +
      'public place.',
    whoItAppliesTo: 'Anyone in the Ninth Circuit (including Alaska) recording police in public.',
    importantExceptions:
      'This right does not permit physically interfering with police activity. ' + GENERIC_EXCEPTIONS,
    keywords: ['recording police', 'filming police', 'first amendment', 'right to record'],
    relatedCitations: ['U.S. Const. amend. I'],
    officialUrl: 'https://www.ca9.uscourts.gov/',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Criminal procedure ───────────────────────────────────────
  {
    id: 'fed-crimproc-miranda',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-criminal-procedure',
    sourceType: 'COURT_RULE',
    citation: 'Miranda v. Arizona, 384 U.S. 436 (1966)',
    title: 'Miranda rights',
    approvedSummary:
      'Before questioning someone who is in custody, police must advise them of the right to ' +
      'remain silent and the right to an attorney.',
    whoItAppliesTo: 'Anyone subjected to custodial interrogation by police in the United States.',
    importantExceptions:
      'Miranda warnings are only required for custodial interrogation — not every police ' +
      'encounter. ' + GENERIC_EXCEPTIONS,
    keywords: ['miranda rights', 'right to remain silent', 'right to an attorney', 'interrogation'],
    relatedCitations: ['U.S. Const. amend. V', 'U.S. Const. amend. VI'],
    officialUrl: 'https://www.law.cornell.edu/supremecourt/text/384/436',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-crimproc-48hour',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-criminal-procedure',
    sourceType: 'COURT_RULE',
    citation: 'County of Riverside v. McLaughlin, 500 U.S. 44 (1991)',
    title: 'Prompt judicial determination of probable cause (the "48-hour rule")',
    approvedSummary:
      'Applying Gerstein v. Pugh, 420 U.S. 103 (1975), the Supreme Court held that a person ' +
      'arrested without a warrant is generally entitled to a judicial probable-cause ' +
      'determination within 48 hours of arrest.',
    whoItAppliesTo: 'Anyone arrested without a warrant in the United States.',
    importantExceptions:
      'Delays can be justified in some circumstances even within 48 hours, and unreasonable ' +
      'delays can be challenged even under 48 hours. ' + GENERIC_EXCEPTIONS,
    keywords: ['48 hour rule', 'probable cause hearing', 'arraignment delay', 'gerstein hearing'],
    relatedCitations: ['Alaska R. Crim. P. 5'],
    officialUrl: 'https://www.law.cornell.edu/supremecourt/text/500/44',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Employment ───────────────────────────────────────────────
  {
    id: 'fed-employment-overtime',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-employment',
    sourceType: 'STATUTE',
    citation: '29 U.S.C. § 207',
    title: 'Fair Labor Standards Act — overtime pay',
    approvedSummary:
      'Requires covered employers to pay non-exempt employees at least one and one-half times ' +
      'their regular rate for hours worked over 40 in a workweek.',
    whoItAppliesTo: 'Covered, non-exempt employees in the United States.',
    importantExceptions:
      'Many categories of employees are exempt (executive, administrative, professional, and ' +
      'others). ' + GENERIC_EXCEPTIONS,
    keywords: ['overtime', 'fair labor standards act', 'flsa', 'time and a half', 'wage and hour'],
    relatedCitations: ['AS 23.10.060'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/29/207',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-employment-title7',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-employment',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 2000e-2',
    title: 'Title VII — employment discrimination prohibited',
    approvedSummary:
      'Part of the Civil Rights Act of 1964. Prohibits employers from discriminating based on ' +
      'race, color, religion, sex, or national origin.',
    whoItAppliesTo: 'Employers with 15 or more employees, employment agencies, and labor unions.',
    importantExceptions:
      'Coverage thresholds and exceptions (such as certain religious organizations) apply. ' + GENERIC_EXCEPTIONS,
    keywords: ['title vii', 'employment discrimination', 'civil rights act', 'workplace discrimination'],
    relatedCitations: ['AS 18.80.220'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/2000e-2',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Housing ──────────────────────────────────────────────────
  {
    id: 'fed-housing-fairhousing',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-housing',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 3604',
    title: 'Fair Housing Act — discriminatory housing practices',
    approvedSummary:
      'Prohibits discrimination in the sale, rental, or financing of housing based on race, ' +
      'color, religion, sex, national origin, familial status, or disability.',
    whoItAppliesTo: 'Landlords, sellers, lenders, and real estate professionals nationwide.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['fair housing act', 'housing discrimination', 'landlord discrimination'],
    relatedCitations: ['AS 18.80.240', '42 U.S.C. § 3610'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/3604',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-housing-complaint',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-housing',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 3610',
    title: 'Filing a housing discrimination complaint',
    approvedSummary:
      'Sets out the process for filing an administrative complaint of housing discrimination ' +
      'with the U.S. Department of Housing and Urban Development (HUD).',
    whoItAppliesTo: 'Anyone who believes they experienced housing discrimination.',
    importantExceptions:
      'Complaints generally must be filed within a statutory time limit after the alleged ' +
      'discriminatory act. ' + GENERIC_EXCEPTIONS,
    keywords: ['hud complaint', 'housing discrimination complaint', 'fair housing complaint'],
    relatedCitations: ['42 U.S.C. § 3604'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/3610',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Immigration ──────────────────────────────────────────────
  {
    id: 'fed-immigration-removal',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-immigration',
    sourceType: 'STATUTE',
    citation: '8 U.S.C. § 1229a',
    title: 'Removal proceedings',
    approvedSummary:
      'Establishes the immigration court process (removal proceedings) used to decide whether a ' +
      'noncitizen may be removed (deported) from the United States.',
    whoItAppliesTo: 'Noncitizens placed in removal proceedings.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['removal proceedings', 'deportation', 'immigration court'],
    relatedCitations: ['8 U.S.C. § 1158'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/8/1229a',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-immigration-asylum',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-immigration',
    sourceType: 'STATUTE',
    citation: '8 U.S.C. § 1158',
    title: 'Asylum',
    approvedSummary:
      'Sets out who may apply for asylum in the United States and the general standard for ' +
      'showing eligibility, based on persecution or a well-founded fear of persecution.',
    whoItAppliesTo: 'Noncitizens physically present in or arriving in the United States.',
    importantExceptions:
      'Strict filing deadlines and bars to eligibility apply. ' + GENERIC_EXCEPTIONS,
    keywords: ['asylum', 'refugee', 'persecution', 'immigration'],
    relatedCitations: ['8 U.S.C. § 1229a'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/8/1158',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Consumer protection ─────────────────────────────────────
  {
    id: 'fed-consumer-fdcpa',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-consumer-protection',
    sourceType: 'STATUTE',
    citation: '15 U.S.C. § 1692g',
    title: 'Fair Debt Collection Practices Act — validation of debts',
    approvedSummary:
      'Requires a debt collector to send a written notice with the amount of the debt, the ' +
      'creditor’s name, and the consumer’s right to dispute the debt within 30 days.',
    whoItAppliesTo: 'Consumers contacted by third-party debt collectors.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['debt collection', 'fdcpa', 'debt validation', 'debt collector'],
    relatedCitations: ['15 U.S.C. § 1681i'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/15/1692g',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-consumer-fcra',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-consumer-protection',
    sourceType: 'STATUTE',
    citation: '15 U.S.C. § 1681i',
    title: 'Fair Credit Reporting Act — disputing inaccurate information',
    approvedSummary:
      'Requires a credit reporting agency to reinvestigate information a consumer disputes as ' +
      'inaccurate, generally within 30 days.',
    whoItAppliesTo: 'Consumers disputing information on their credit report.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['credit report', 'fcra', 'credit dispute', 'credit bureau'],
    relatedCitations: ['15 U.S.C. § 1692g'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/15/1681i',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Bankruptcy ───────────────────────────────────────────────
  {
    id: 'fed-bankruptcy-stay',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-bankruptcy',
    sourceType: 'STATUTE',
    citation: '11 U.S.C. § 362',
    title: 'Automatic stay',
    approvedSummary:
      'Filing bankruptcy automatically stops (stays) most collection actions, lawsuits, wage ' +
      'garnishments, and creditor contact against the debtor.',
    whoItAppliesTo: 'Anyone who has filed a bankruptcy petition, and their creditors.',
    importantExceptions:
      'Certain actions (like some criminal proceedings and some family-law matters) are not ' +
      'stayed. ' + GENERIC_EXCEPTIONS,
    keywords: ['bankruptcy', 'automatic stay', 'collections', 'garnishment'],
    relatedCitations: ['11 U.S.C. § 727'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/11/362',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-bankruptcy-discharge',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-bankruptcy',
    sourceType: 'STATUTE',
    citation: '11 U.S.C. § 727',
    title: 'Discharge in Chapter 7 bankruptcy',
    approvedSummary:
      'Sets out when a Chapter 7 bankruptcy debtor is entitled to a discharge — a court order ' +
      'releasing the debtor from personal liability for most debts.',
    whoItAppliesTo: 'Individuals who filed a Chapter 7 bankruptcy case.',
    importantExceptions:
      'Certain debts (such as some taxes, student loans, and support obligations) are generally ' +
      'not dischargeable. ' + GENERIC_EXCEPTIONS,
    keywords: ['chapter 7', 'bankruptcy discharge', 'debt relief'],
    relatedCitations: ['11 U.S.C. § 362'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/11/727',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Federal benefits ─────────────────────────────────────────
  {
    id: 'fed-benefits-ssdi',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-federal-benefits',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 423',
    title: 'Social Security Disability Insurance (SSDI)',
    approvedSummary:
      'Establishes eligibility rules for Social Security disability insurance benefits, paid to ' +
      'workers who have paid into Social Security and can no longer work due to a qualifying ' +
      'disability.',
    whoItAppliesTo: 'Workers with sufficient Social Security work credits who become disabled.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['ssdi', 'social security disability', 'disability benefits'],
    relatedCitations: ['29 U.S.C. § 794'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/423',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-benefits-snap',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-federal-benefits',
    sourceType: 'STATUTE',
    citation: '7 U.S.C. § 2014',
    title: 'SNAP (food stamp) eligibility',
    approvedSummary:
      'Sets out income and other eligibility requirements for the Supplemental Nutrition ' +
      'Assistance Program (SNAP), the federal food-assistance program.',
    whoItAppliesTo: 'Low-income individuals and households applying for SNAP.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['snap', 'food stamps', 'food assistance', 'nutrition assistance'],
    relatedCitations: ['AS 47.27'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/7/2014',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Taxes ────────────────────────────────────────────────────
  {
    id: 'fed-tax-grossincome',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-taxes',
    sourceType: 'STATUTE',
    citation: '26 U.S.C. § 61',
    title: 'Gross income defined',
    approvedSummary:
      'Broadly defines "gross income" for federal income tax purposes as all income from ' +
      'whatever source derived, unless a specific exclusion applies.',
    whoItAppliesTo: 'Anyone subject to U.S. federal income tax.',
    importantExceptions:
      'Many specific exclusions and deductions exist elsewhere in the tax code. ' + GENERIC_EXCEPTIONS,
    keywords: ['gross income', 'income tax', 'irs', 'internal revenue code'],
    relatedCitations: ['26 U.S.C. § 6321'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/26/61',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-tax-lien',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-taxes',
    sourceType: 'STATUTE',
    citation: '26 U.S.C. § 6321',
    title: 'Federal tax lien',
    approvedSummary:
      'Creates a lien in favor of the United States on all property of a person who fails to pay ' +
      'a tax after demand, arising automatically once the tax is assessed and unpaid.',
    whoItAppliesTo: 'Taxpayers with unpaid, assessed federal tax debt.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['tax lien', 'irs lien', 'unpaid taxes'],
    relatedCitations: ['26 U.S.C. § 61'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/26/6321',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Disability rights ────────────────────────────────────────
  {
    id: 'fed-disability-ada',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-disability-rights',
    sourceType: 'STATUTE',
    citation: '42 U.S.C. § 12101',
    title: 'Americans with Disabilities Act — findings and purpose',
    approvedSummary:
      'Opens the ADA, which prohibits discrimination against people with disabilities in ' +
      'employment, public services, public accommodations, and telecommunications.',
    whoItAppliesTo: 'People with disabilities, and covered employers, businesses, and government entities.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['ada', 'americans with disabilities act', 'disability discrimination', 'accessibility'],
    relatedCitations: ['29 U.S.C. § 794'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/42/12101',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-disability-504',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-disability-rights',
    sourceType: 'STATUTE',
    citation: '29 U.S.C. § 794',
    title: 'Rehabilitation Act § 504 — nondiscrimination in federal programs',
    approvedSummary:
      'Prohibits discrimination against people with disabilities in any program or activity that ' +
      'receives federal financial assistance.',
    whoItAppliesTo: 'Recipients of federal financial assistance, and people with disabilities they serve.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['section 504', 'rehabilitation act', 'disability discrimination', 'federal funding'],
    relatedCitations: ['42 U.S.C. § 12101'],
    officialUrl: 'https://www.law.cornell.edu/uscode/text/29/794',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },

  // ── Federal: Federal regulations ──────────────────────────────────────
  {
    id: 'fed-regs-osha',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-federal-regulations',
    sourceType: 'REGULATION',
    citation: '29 C.F.R. Part 1910',
    title: 'OSHA general industry safety standards',
    approvedSummary:
      'Occupational Safety and Health Administration regulations setting workplace safety and ' +
      'health standards for general industry.',
    whoItAppliesTo: 'Most private-sector employers and employees in general industry.',
    importantExceptions:
      'Some industries (e.g., construction, maritime) are covered by separate OSHA parts. ' + GENERIC_EXCEPTIONS,
    keywords: ['osha', 'workplace safety', 'occupational safety', 'general industry standards'],
    relatedCitations: [],
    officialUrl: 'https://www.ecfr.gov/current/title-29/subtitle-B/chapter-XVII/part-1910',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
  {
    id: 'fed-regs-drinkingwater',
    jurisdiction: 'FEDERAL',
    classificationId: 'fed-federal-regulations',
    sourceType: 'REGULATION',
    citation: '40 C.F.R. Part 141',
    title: 'National Primary Drinking Water Regulations',
    approvedSummary:
      'EPA regulations under the Safe Drinking Water Act setting enforceable standards for ' +
      'contaminants in public drinking water systems.',
    whoItAppliesTo: 'Public water systems and the people they serve.',
    importantExceptions: GENERIC_EXCEPTIONS,
    keywords: ['drinking water', 'safe drinking water act', 'epa', 'water quality'],
    relatedCitations: [],
    officialUrl: 'https://www.ecfr.gov/current/title-40/chapter-I/subchapter-D/part-141',
    effectiveDate: NOT_VERIFIED_DATE,
    lastReviewedDate: LAST_REVIEWED,
    reviewStatus: 'NOT_ATTORNEY_REVIEWED',
    contentVersion: 1,
  },
];

export function getClassificationsByJurisdiction(jurisdiction: 'ALASKA' | 'FEDERAL'): Classification[] {
  return CLASSIFICATIONS.filter((c) => c.jurisdiction === jurisdiction);
}

export function getClassificationById(id: string): Classification | undefined {
  return CLASSIFICATIONS.find((c) => c.id === id);
}

export function getEntriesByClassification(classificationId: string): LegalEntry[] {
  return LEGAL_ENTRIES.filter((e) => e.classificationId === classificationId);
}
