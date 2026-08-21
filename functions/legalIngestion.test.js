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
    "b6fba1b8dfdce5cf31f9d8c8d8c8cb1c6f6ccf13a4a7e9c4b9b2d5fbbd1d1f95"
  );
});
