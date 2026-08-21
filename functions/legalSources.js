/**
 * Allow-listed authoritative legal sources.
 *
 * These URLs are source monitors, not LLM inputs. A change is recorded for
 * validation; it is never automatically converted into user-facing legal
 * advice or prose.
 */

const LEGAL_SOURCES = [
  {
    id: "alaska-legislature-laws",
    jurisdiction: "ALASKA",
    sourceType: "STATUTES_PORTAL",
    authority: "Alaska State Legislature",
    url: "https://www.akleg.gov/basis/Home/Law",
  },
  {
    id: "alaska-court-rules",
    jurisdiction: "ALASKA",
    sourceType: "COURT_RULES",
    authority: "Alaska Court System",
    url: "https://courts.alaska.gov/rules/",
  },
  {
    id: "alaska-supreme-court-orders",
    jurisdiction: "ALASKA",
    sourceType: "SUPREME_COURT_ORDERS",
    authority: "Alaska Court System",
    url: "https://courts.alaska.gov/sco/",
  },
  {
    id: "us-code",
    jurisdiction: "FEDERAL",
    sourceType: "UNITED_STATES_CODE",
    authority: "Office of the Law Revision Counsel, U.S. House of Representatives",
    url: "https://uscode.house.gov/",
  },
];

module.exports = { LEGAL_SOURCES };
