#!/usr/bin/env python3
"""Tests for the content compiler.

Stdlib unittest, no pytest dependency — this must run anywhere counsel or CI can run Python.

    python3 tools/content-compiler/test_compile_content.py
"""

from __future__ import annotations

import copy
import datetime as dt
import json
import sys
import tempfile
import unittest
from pathlib import Path
from typing import Any

import yaml

sys.path.insert(0, str(Path(__file__).parent))

import compile_content as cc  # noqa: E402

TODAY = dt.date(2026, 8, 19)

VALID_DOC: dict[str, Any] = {
    "violation_key": "VIOLATION_48_HOUR_ARRAIGNMENT",
    "jurisdiction": "US-CA",
    "content_version": 1,
    "description": (
        "The police must bring you before a judge within a set time after they arrest you. "
        "If they held you longer than that, your rights may have been violated."
    ),
    "action_steps": [
        "Ask for a copy of your booking record. It shows the exact time you were arrested.",
        "Tell your lawyer how long you waited before you saw a judge.",
    ],
    "authorities": [
        {
            "type": "CASE",
            "citation": "County of Riverside v. McLaughlin, 500 U.S. 44 (1991)",
            "title": "Probable cause determinations generally within 48 hours of arrest",
            "official_url": "https://www.courtlistener.com/opinion/112594/county-of-riverside-v-mclaughlin/",
            "source_system": "courtlistener",
            "source_id": "112594",
            "version_label": "500 U.S. 44 (1991)",
            "retrieved_at": dt.date(2026, 8, 19),
            "text_hash": "sha256:" + "a" * 64,
        }
    ],
    "authored_by": "A. Author, State Bar #000001",
    "reviewed_by": "R. Reviewer, State Bar #000002",
    "reviewed_at": dt.date(2026, 8, 19),
    "review_by": dt.date(2026, 12, 19),
}


def doc_with(**overrides: Any) -> dict[str, Any]:
    doc = copy.deepcopy(VALID_DOC)
    doc.update(overrides)
    return doc


class CompilerTestCase(unittest.TestCase):
    """Writes authoring docs to a temp dir and runs them through the loader."""

    def compile(
        self,
        docs: list[dict[str, Any]],
        *,
        filenames: list[str] | None = None,
        allow_examples: bool = False,
        today: dt.date = TODAY,
    ) -> tuple[list[dict[str, Any]], cc.Diagnostics]:
        with tempfile.TemporaryDirectory() as tmp:
            content_dir = Path(tmp)
            for i, doc in enumerate(docs):
                name = filenames[i] if filenames else f"{doc.get('violation_key', 'UNNAMED')}.yaml"
                path = content_dir / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(yaml.safe_dump(doc, sort_keys=False), encoding="utf-8")
            diags = cc.Diagnostics()
            entries = cc.load_entries(content_dir, today, allow_examples, diags)
        return entries, diags

    def assertErrorMentions(self, diags: cc.Diagnostics, fragment: str) -> None:
        joined = " | ".join(diags.errors)
        self.assertIn(fragment, joined, f"expected an error mentioning {fragment!r}; got: {joined}")


# ---------------------------------------------------------------------------

class TestValidContent(CompilerTestCase):

    def test_valid_entry_compiles(self):
        entries, diags = self.compile([VALID_DOC])
        self.assertEqual([], diags.errors)
        self.assertEqual(1, len(entries))
        self.assertEqual("VIOLATION_48_HOUR_ARRAIGNMENT", entries[0]["violationKey"])
        self.assertEqual("US-CA", entries[0]["jurisdiction"])

    def test_action_steps_pass_through_unmodified(self):
        """DB strings reach the UI byte for byte — the compiler must not normalise them either."""
        spaced = "  Ask for a copy of your booking record.  "
        entries, diags = self.compile([doc_with(action_steps=[spaced])])
        self.assertEqual([], diags.errors)
        self.assertEqual(spaced, entries[0]["actionSteps"][0])

    def test_same_key_in_two_jurisdictions_is_allowed(self):
        """Jurisdiction is part of the identity, not a post-filter (AI rule 12)."""
        entries, diags = self.compile(
            [VALID_DOC, doc_with(jurisdiction="US-NY")],
            filenames=[
                "us-ca/VIOLATION_48_HOUR_ARRAIGNMENT.yaml",
                "us-ny/VIOLATION_48_HOUR_ARRAIGNMENT.yaml",
            ],
        )
        self.assertEqual([], diags.errors)
        self.assertEqual(2, len(entries))
        self.assertEqual(
            ["US-CA", "US-NY"], sorted(e["jurisdiction"] for e in entries)
        )

    def test_deadline_is_carried_into_the_bundle(self):
        entries, _ = self.compile([doc_with(deadline={
            "unit": "HOURS", "amount": 48, "trigger": "ARREST",
            "lost_if_missed": "It becomes much harder to challenge how long you were held.",
        })])
        self.assertEqual(48, entries[0]["deadline"]["amount"])
        self.assertEqual("ARREST", entries[0]["deadline"]["trigger"])


class TestRejections(CompilerTestCase):

    def test_example_status_is_rejected_by_default(self):
        _, diags = self.compile([doc_with(status="EXAMPLE")])
        self.assertErrorMentions(diags, "EXAMPLE")

    def test_example_status_is_allowed_behind_the_flag(self):
        entries, diags = self.compile([doc_with(status="EXAMPLE")], allow_examples=True)
        self.assertEqual([], diags.errors)
        self.assertEqual(1, len(entries))

    def test_filename_must_match_violation_key(self):
        _, diags = self.compile([VALID_DOC], filenames=["wrong-name.yaml"])
        self.assertErrorMentions(diags, "filename must match")

    def test_malformed_violation_key_is_rejected(self):
        _, diags = self.compile(
            [doc_with(violation_key="not_a_valid_key")], filenames=["not_a_valid_key.yaml"]
        )
        self.assertErrorMentions(diags, "violation_key")

    def test_entry_without_authorities_is_rejected(self):
        _, diags = self.compile([doc_with(authorities=[])])
        self.assertErrorMentions(diags, "authorities")

    def test_non_https_authority_url_is_rejected(self):
        doc = copy.deepcopy(VALID_DOC)
        doc["authorities"][0]["official_url"] = "http://insecure.gov/code"
        _, diags = self.compile([doc])
        self.assertErrorMentions(diags, "https")

    def test_malformed_text_hash_is_rejected(self):
        doc = copy.deepcopy(VALID_DOC)
        doc["authorities"][0]["text_hash"] = "md5:abc"
        _, diags = self.compile([doc])
        self.assertErrorMentions(diags, "text_hash")

    def test_missing_authority_field_is_rejected(self):
        doc = copy.deepcopy(VALID_DOC)
        del doc["authorities"][0]["source_system"]
        _, diags = self.compile([doc])
        self.assertErrorMentions(diags, "source_system")

    def test_review_by_must_follow_reviewed_at(self):
        _, diags = self.compile([doc_with(review_by=dt.date(2026, 8, 19))])
        self.assertErrorMentions(diags, "must be after")

    def test_future_review_date_is_rejected(self):
        _, diags = self.compile([doc_with(
            reviewed_at=dt.date(2027, 1, 1), review_by=dt.date(2027, 6, 1)
        )])
        self.assertErrorMentions(diags, "future")

    def test_author_cannot_be_their_own_reviewer(self):
        _, diags = self.compile([doc_with(
            authored_by="Same Person", reviewed_by="Same Person"
        )])
        self.assertErrorMentions(diags, "second reader")

    def test_placeholder_text_is_rejected(self):
        _, diags = self.compile([doc_with(
            action_steps=["TODO: write the real step here"]
        )])
        self.assertErrorMentions(diags, "placeholder")

    def test_placeholder_url_is_rejected(self):
        doc = copy.deepcopy(VALID_DOC)
        doc["authorities"][0]["official_url"] = "https://example.com/statute"
        _, diags = self.compile([doc])
        self.assertErrorMentions(diags, "placeholder")

    def test_duplicate_key_and_jurisdiction_is_rejected(self):
        _, diags = self.compile(
            [VALID_DOC, VALID_DOC],
            filenames=[
                "us-ca/VIOLATION_48_HOUR_ARRAIGNMENT.yaml",
                "duplicate/VIOLATION_48_HOUR_ARRAIGNMENT.yaml",
            ],
        )
        self.assertErrorMentions(diags, "duplicate")

    def test_invalid_deadline_unit_is_rejected(self):
        _, diags = self.compile([doc_with(deadline={
            "unit": "FORTNIGHTS", "amount": 2, "trigger": "ARREST",
            "lost_if_missed": "Something is lost.",
        })])
        self.assertErrorMentions(diags, "deadline.unit")

    def test_unparseable_yaml_is_reported_not_crashed(self):
        with tempfile.TemporaryDirectory() as tmp:
            content_dir = Path(tmp)
            (content_dir / "VIOLATION_BROKEN.yaml").write_text("key: [unclosed\n", encoding="utf-8")
            diags = cc.Diagnostics()
            entries = cc.load_entries(content_dir, TODAY, False, diags)
        self.assertEqual([], entries)
        self.assertErrorMentions(diags, "YAML")


class TestExpiry(CompilerTestCase):
    """AI rule 15 — stale content is withdrawn, not shown, and that is not a build failure."""

    def test_expired_entry_is_excluded_without_erroring(self):
        entries, diags = self.compile([doc_with(
            reviewed_at=dt.date(2025, 1, 1), review_by=dt.date(2025, 6, 1)
        )])
        self.assertEqual([], diags.errors)
        self.assertEqual([], entries)
        self.assertEqual(1, len(diags.excluded))
        self.assertIn("expired", diags.excluded[0])

    def test_entry_expiring_soon_warns(self):
        _, diags = self.compile([doc_with(review_by=TODAY + dt.timedelta(days=10))])
        self.assertEqual([], diags.errors)
        self.assertTrue(any("within" in w for w in diags.warnings))

    def test_case_law_entry_with_long_review_interval_warns(self):
        _, diags = self.compile([doc_with(review_by=dt.date(2027, 8, 1))])
        self.assertTrue(
            any("case law" in w for w in diags.warnings),
            f"expected a case-law review-interval warning; got {diags.warnings}",
        )

    def test_statute_only_entry_may_have_a_long_review_interval(self):
        doc = copy.deepcopy(VALID_DOC)
        doc["authorities"][0]["type"] = "STATUTE"
        doc["review_by"] = dt.date(2027, 8, 1)
        _, diags = self.compile([doc])
        self.assertFalse(any("case law" in w for w in diags.warnings))


class TestReadingLevel(CompilerTestCase):
    """AI rule 17 — reading level is a release criterion, so it is machine-checked."""

    def test_plain_language_passes(self):
        _, diags = self.compile([VALID_DOC])
        self.assertFalse(
            any("reading level" in w for w in diags.warnings),
            f"plain copy should not warn; got {diags.warnings}",
        )

    def test_dense_legalese_warns(self):
        _, diags = self.compile([doc_with(description=(
            "Notwithstanding any provision heretofore enumerated, the aforementioned "
            "determination of probable cause consequent to a warrantless apprehension "
            "necessitates expeditious judicial ascertainment pursuant to constitutional "
            "requirements articulated in controlling appellate jurisprudence."
        ))])
        self.assertTrue(
            any("reading level" in w for w in diags.warnings),
            f"expected a reading-level warning; got {diags.warnings}",
        )

    def test_grade_estimate_is_none_for_trivial_text(self):
        self.assertIsNone(cc.flesch_kincaid_grade("Too short."))


class TestBundleAndCodegen(CompilerTestCase):

    def test_bundle_hash_is_stable_for_identical_content(self):
        entries, _ = self.compile([VALID_DOC])
        first = cc.build_bundle(entries, TODAY)
        second = cc.build_bundle(copy.deepcopy(entries), TODAY)
        self.assertEqual(first["contentHash"], second["contentHash"])

    def test_bundle_hash_changes_when_content_changes(self):
        base, _ = self.compile([VALID_DOC])
        changed, _ = self.compile([doc_with(action_steps=["A different step entirely."])])
        self.assertNotEqual(
            cc.build_bundle(base, TODAY)["contentHash"],
            cc.build_bundle(changed, TODAY)["contentHash"],
        )

    def test_generated_enum_lists_every_key(self):
        entries, _ = self.compile([VALID_DOC])
        kotlin = cc.render_kotlin(entries, cc.build_bundle(entries, TODAY))
        self.assertIn('VIOLATION_48_HOUR_ARRAIGNMENT("VIOLATION_48_HOUR_ARRAIGNMENT")', kotlin)
        self.assertIn("fun fromKeyOrNull(raw: String): ViolationKey?", kotlin)
        self.assertIn("package com.pocketlegal.advice.data.local", kotlin)

    def test_generated_enum_is_empty_but_valid_with_no_content(self):
        kotlin = cc.render_kotlin([], cc.build_bundle([], TODAY))
        self.assertIn("enum class ViolationKey", kotlin)
        self.assertIn("    ;", kotlin)

    def test_bundle_is_json_serialisable(self):
        entries, _ = self.compile([VALID_DOC])
        encoded = json.dumps(cc.build_bundle(entries, TODAY))
        self.assertIn("VIOLATION_48_HOUR_ARRAIGNMENT", encoded)


class TestCli(unittest.TestCase):

    def _run(self, docs: list[dict[str, Any]], *extra: str) -> tuple[int, Path]:
        tmp = Path(tempfile.mkdtemp())
        content_dir = tmp / "content"
        content_dir.mkdir()
        for doc in docs:
            (content_dir / f"{doc['violation_key']}.yaml").write_text(
                yaml.safe_dump(doc, sort_keys=False), encoding="utf-8"
            )
        code = cc.main([
            "--content-dir", str(content_dir),
            "--out-assets", str(tmp / "bundle.json"),
            "--out-kotlin", str(tmp / "ViolationKey.kt"),
            "--today", TODAY.isoformat(),
            *extra,
        ])
        return code, tmp

    def test_clean_content_exits_zero_and_writes_both_outputs(self):
        code, tmp = self._run([VALID_DOC])
        self.assertEqual(0, code)
        self.assertTrue((tmp / "bundle.json").exists())
        self.assertTrue((tmp / "ViolationKey.kt").exists())
        bundle = json.loads((tmp / "bundle.json").read_text())
        self.assertEqual(1, bundle["entryCount"])
        self.assertEqual(["US-CA"], bundle["jurisdictions"])

    def test_errors_exit_one_and_write_nothing(self):
        code, tmp = self._run([doc_with(authorities=[])])
        self.assertEqual(1, code)
        self.assertFalse((tmp / "bundle.json").exists())

    def test_warnings_exit_two_under_strict(self):
        code, _ = self._run([doc_with(review_by=TODAY + dt.timedelta(days=10))], "--strict")
        self.assertEqual(2, code)

    def test_warnings_exit_zero_without_strict(self):
        code, _ = self._run([doc_with(review_by=TODAY + dt.timedelta(days=10))])
        self.assertEqual(0, code)

    def test_check_mode_writes_nothing(self):
        code, tmp = self._run([VALID_DOC], "--check")
        self.assertEqual(0, code)
        self.assertFalse((tmp / "bundle.json").exists())

    def test_missing_content_dir_exits_one(self):
        self.assertEqual(1, cc.main(["--content-dir", "/nonexistent/path/xyz"]))


if __name__ == "__main__":
    unittest.main(verbosity=2)
