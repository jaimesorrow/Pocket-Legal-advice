const test = require("node:test");
const assert = require("node:assert/strict");
const { sha256 } = require("./legalIngestion");
const { LEGAL_SOURCES } = require("./legalSources");

test("authoritative source registry is restricted to Alaska and federal jurisdictions", () => {
  assert.ok(LEGAL_SOURCES.length >= 4);
  for (const source of LEGAL_SOURCES) {
    assert.ok(["ALASKA", "FEDERAL"].includes(source.jurisdiction));
    assert.match(source.url, /^https:\/\//);
  }
});

test("content hashes are deterministic SHA-256 values", () => {
  assert.equal(
    sha256("Pocket Legal Advice"),
    "495563c283f5bd0f05ca528b506cba283e3a8493b0f2fba0ad70dc0ac1eb5df6"
  );
});
