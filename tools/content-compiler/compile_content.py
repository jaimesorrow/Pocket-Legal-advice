#!/usr/bin/env python3
"""Compile authored legal content into a signed app bundle and a closed violation-key enum.

Authored YAML (see SCHEMA.md) is the source of truth for every word the app shows a user.
This tool validates it against the invariants in docs/AI-REQUIREMENTS.md and emits:

  1. a JSON content bundle, shipped as an Android asset — the only source of user-visible text
  2. generated Kotlin naming the closed violation-key vocabulary (AI rule 1)

Emitting both from one source is deliberate: a key cannot exist in the classifier's vocabulary
without verified content behind it, and content cannot exist for a key the app will not accept.

Usage:
    python3 compile_content.py --content-dir content --strict
    python3 compile_content.py --content-dir tools/content-compiler/fixtures/valid \\
        --allow-examples --out-assets /tmp/bundle.json

Exit codes: 0 clean · 1 validation errors · 2 warnings under --strict
"""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import yaml

BUNDLE_FORMAT_VERSION = 1

VIOLATION_KEY_RE = re.compile(r"^VIOLATION_[A-Z0-9_]+$")
JURISDICTION_RE = re.compile(r"^US(-[A-Z]{2})?$")
TEXT_HASH_RE = re.compile(r"^sha256:[0-9a-f]{64}$")

AUTHORITY_TYPES = {"STATUTE", "REGULATION", "CASE", "CONSTITUTIONAL", "COURT_RULE"}
DEADLINE_UNITS = {"HOURS", "DAYS", "MONTHS", "YEARS"}
DEADLINE_TRIGGERS = {"ARREST", "BOOKING", "CITATION", "INCIDENT", "NOTICE", "FILING"}

REQUIRED_AUTHORITY_FIELDS = (
    "type", "citation", "title", "official_url",
    "source_system", "source_id", "version_label", "retrieved_at", "text_hash",
)

# Filler that must never reach a build a real person can install (DESIGN.md anti-patterns).
PLACEHOLDER_PATTERNS = (
    re.compile(r"\bTODO\b"),
    re.compile(r"\bTBD\b"),
    re.compile(r"\bFIXME\b"),
    re.compile(r"\bXXX\b"),
    re.compile(r"\bPLACEHOLDER\b", re.IGNORECASE),
    re.compile(r"lorem ipsum", re.IGNORECASE),
    re.compile(r"example\.com", re.IGNORECASE),
)

# Case law decays invisibly and no reliable free citator exists (docs/LEGAL-SOURCES.md),
# so entries resting on case law get shorter review cycles than statutory ones.
CASE_LAW_MAX_REVIEW_DAYS = 180
REVIEW_EXPIRY_WARNING_DAYS = 30
MAX_READING_GRADE = 8.0


# ---------------------------------------------------------------------------
# Diagnostics
# ---------------------------------------------------------------------------

@dataclass
class Diagnostics:
    errors: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)
    excluded: list[str] = field(default_factory=list)

    def error(self, where: str, message: str) -> None:
        self.errors.append(f"{where}: {message}")

    def warn(self, where: str, message: str) -> None:
        self.warnings.append(f"{where}: {message}")

    def exclude(self, where: str, message: str) -> None:
        self.excluded.append(f"{where}: {message}")


# ---------------------------------------------------------------------------
# Reading level (AI rule 17)
# ---------------------------------------------------------------------------

def _count_syllables(word: str) -> int:
    """Approximate syllable count by vowel groups. Good enough to flag dense prose."""
    word = word.lower().strip(".,;:!?\"'()[]—–-")
    if not word:
        return 0
    groups = re.findall(r"[aeiouy]+", word)
    count = len(groups)
    if word.endswith("e") and count > 1 and not word.endswith(("le", "ee", "ye")):
        count -= 1
    return max(count, 1)


def flesch_kincaid_grade(text: str) -> float | None:
    """Flesch-Kincaid grade level. Returns None when there is too little text to judge."""
    sentences = [s for s in re.split(r"[.!?]+", text) if s.strip()]
    words = re.findall(r"[A-Za-z']+", text)
    if not sentences or len(words) < 10:
        return None
    syllables = sum(_count_syllables(w) for w in words)
    return (
        0.39 * (len(words) / len(sentences))
        + 11.8 * (syllables / len(words))
        - 15.59
    )


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

def _as_date(value: Any) -> dt.date | None:
    if isinstance(value, dt.datetime):
        return value.date()
    if isinstance(value, dt.date):
        return value
    if isinstance(value, str):
        try:
            return dt.date.fromisoformat(value)
        except ValueError:
            return None
    return None


def _check_placeholders(text: str, where: str, label: str, diags: Diagnostics) -> None:
    for pattern in PLACEHOLDER_PATTERNS:
        if pattern.search(text):
            diags.error(where, f"{label} contains placeholder text matching {pattern.pattern!r}")


def _validate_authority(
    authority: Any, index: int, where: str, diags: Diagnostics
) -> bool:
    label = f"authorities[{index}]"
    if not isinstance(authority, dict):
        diags.error(where, f"{label} must be a mapping")
        return False

    ok = True
    for required in REQUIRED_AUTHORITY_FIELDS:
        if authority.get(required) in (None, ""):
            diags.error(where, f"{label} is missing required field '{required}'")
            ok = False
    if not ok:
        return False

    if authority["type"] not in AUTHORITY_TYPES:
        diags.error(
            where,
            f"{label}.type {authority['type']!r} is not one of {sorted(AUTHORITY_TYPES)}",
        )
        ok = False

    url = str(authority["official_url"])
    if not url.startswith("https://"):
        diags.error(where, f"{label}.official_url must be https://, got {url!r}")
        ok = False
    _check_placeholders(url, where, f"{label}.official_url", diags)

    if not TEXT_HASH_RE.match(str(authority["text_hash"])):
        diags.error(where, f"{label}.text_hash must be 'sha256:' plus 64 hex characters")
        ok = False

    if _as_date(authority["retrieved_at"]) is None:
        diags.error(where, f"{label}.retrieved_at is not a valid date")
        ok = False

    if authority.get("effective_date") is not None and _as_date(authority["effective_date"]) is None:
        diags.error(where, f"{label}.effective_date is not a valid date")
        ok = False

    _check_placeholders(str(authority["citation"]), where, f"{label}.citation", diags)
    return ok


def _validate_deadline(deadline: Any, where: str, diags: Diagnostics) -> bool:
    if not isinstance(deadline, dict):
        diags.error(where, "deadline must be a mapping")
        return False

    ok = True
    for required in ("unit", "amount", "trigger", "lost_if_missed"):
        if deadline.get(required) in (None, ""):
            diags.error(where, f"deadline is missing required field '{required}'")
            ok = False
    if not ok:
        return False

    if deadline["unit"] not in DEADLINE_UNITS:
        diags.error(where, f"deadline.unit must be one of {sorted(DEADLINE_UNITS)}")
        ok = False
    if deadline["trigger"] not in DEADLINE_TRIGGERS:
        diags.error(where, f"deadline.trigger must be one of {sorted(DEADLINE_TRIGGERS)}")
        ok = False
    if not isinstance(deadline["amount"], int) or deadline["amount"] <= 0:
        diags.error(where, "deadline.amount must be a positive integer")
        ok = False

    _check_placeholders(str(deadline["lost_if_missed"]), where, "deadline.lost_if_missed", diags)
    return ok


def validate_entry(
    doc: Any, path: Path, today: dt.date, allow_examples: bool, diags: Diagnostics
) -> dict[str, Any] | None:
    """Validate one authoring document. Returns the normalised entry, or None if unusable."""
    where = path.name

    if not isinstance(doc, dict):
        diags.error(where, "file must contain a single YAML mapping")
        return None

    if doc.get("status") == "EXAMPLE" and not allow_examples:
        diags.error(where, "status is EXAMPLE — refusing to build example content into a bundle")
        return None

    required = (
        "violation_key", "jurisdiction", "content_version", "description",
        "action_steps", "authorities", "authored_by", "reviewed_by",
        "reviewed_at", "review_by",
    )
    missing = [f for f in required if doc.get(f) in (None, "", [])]
    if missing:
        diags.error(where, f"missing required field(s): {', '.join(missing)}")
        return None

    key = str(doc["violation_key"])
    jurisdiction = str(doc["jurisdiction"])
    ok = True

    if not VIOLATION_KEY_RE.match(key):
        diags.error(where, f"violation_key {key!r} must match {VIOLATION_KEY_RE.pattern}")
        ok = False
    if path.stem != key:
        diags.error(where, f"filename must match violation_key — expected {key}.yaml")
        ok = False
    if not JURISDICTION_RE.match(jurisdiction):
        diags.error(where, f"jurisdiction {jurisdiction!r} must be 'US' or 'US-XX'")
        ok = False

    if not isinstance(doc["content_version"], int) or doc["content_version"] < 1:
        diags.error(where, "content_version must be a positive integer")
        ok = False

    description = str(doc["description"]).strip()
    if not description:
        diags.error(where, "description must not be empty")
        ok = False
    _check_placeholders(description, where, "description", diags)

    steps = doc["action_steps"]
    if not isinstance(steps, list) or not steps:
        diags.error(where, "action_steps must be a non-empty list")
        ok = False
        steps = []
    else:
        for i, step in enumerate(steps):
            if not isinstance(step, str) or not step.strip():
                diags.error(where, f"action_steps[{i}] must be a non-empty string")
                ok = False
            else:
                _check_placeholders(step, where, f"action_steps[{i}]", diags)

    authorities = doc["authorities"]
    if not isinstance(authorities, list) or not authorities:
        diags.error(where, "authorities must be a non-empty list — guidance needs a source (AI rule 14)")
        ok = False
        authorities = []
    else:
        for i, authority in enumerate(authorities):
            if not _validate_authority(authority, i, where, diags):
                ok = False

    if doc.get("deadline") is not None and not _validate_deadline(doc["deadline"], where, diags):
        ok = False

    authored_by = str(doc["authored_by"]).strip()
    reviewed_by = str(doc["reviewed_by"]).strip()
    if authored_by == reviewed_by:
        diags.error(where, "reviewed_by must differ from authored_by — review needs a second reader")
        ok = False

    reviewed_at = _as_date(doc["reviewed_at"])
    review_by = _as_date(doc["review_by"])
    if reviewed_at is None:
        diags.error(where, "reviewed_at is not a valid date")
        ok = False
    elif reviewed_at > today:
        diags.error(where, f"reviewed_at {reviewed_at} is in the future")
        ok = False
    if review_by is None:
        diags.error(where, "review_by is not a valid date")
        ok = False
    if reviewed_at and review_by and review_by <= reviewed_at:
        diags.error(where, f"review_by {review_by} must be after reviewed_at {reviewed_at}")
        ok = False

    if not ok:
        return None

    # --- warnings: real but not build-breaking outside --strict ---

    prose = " ".join([description, *[str(s) for s in steps]])
    grade = flesch_kincaid_grade(prose)
    if grade is not None and grade > MAX_READING_GRADE:
        diags.warn(
            where,
            f"estimated reading level is grade {grade:.1f}, above the grade "
            f"{MAX_READING_GRADE:.0f} ceiling (AI rule 17)",
        )

    assert reviewed_at is not None and review_by is not None
    if today < review_by <= today + dt.timedelta(days=REVIEW_EXPIRY_WARNING_DAYS):
        diags.warn(where, f"review_by {review_by} is within {REVIEW_EXPIRY_WARNING_DAYS} days")

    has_case_law = any(a.get("type") == "CASE" for a in authorities)
    interval = (review_by - reviewed_at).days
    if has_case_law and interval > CASE_LAW_MAX_REVIEW_DAYS:
        diags.warn(
            where,
            f"rests on case law but the review interval is {interval} days "
            f"(max {CASE_LAW_MAX_REVIEW_DAYS}) — case law decays invisibly",
        )

    # --- expiry is enforcement, not an error (AI rule 15) ---

    if review_by <= today:
        diags.exclude(where, f"expired on {review_by} — dropped from bundle, app will abstain")
        return None

    return {
        "violationKey": key,
        "jurisdiction": jurisdiction,
        "contentVersion": doc["content_version"],
        "description": description,
        "actionSteps": [str(s) for s in steps],
        "authorities": [
            {
                "type": a["type"],
                "citation": str(a["citation"]),
                "title": str(a["title"]),
                "officialUrl": str(a["official_url"]),
                "sourceSystem": str(a["source_system"]),
                "sourceId": str(a["source_id"]),
                "versionLabel": str(a["version_label"]),
                "effectiveDate": (
                    _as_date(a["effective_date"]).isoformat()
                    if a.get("effective_date") is not None else None
                ),
                "retrievedAt": _as_date(a["retrieved_at"]).isoformat(),
                "textHash": str(a["text_hash"]),
            }
            for a in authorities
        ],
        "deadline": (
            {
                "unit": doc["deadline"]["unit"],
                "amount": doc["deadline"]["amount"],
                "trigger": doc["deadline"]["trigger"],
                "lostIfMissed": str(doc["deadline"]["lost_if_missed"]),
            }
            if doc.get("deadline") is not None else None
        ),
        "authoredBy": authored_by,
        "reviewedBy": reviewed_by,
        "reviewedAt": reviewed_at.isoformat(),
        "reviewBy": review_by.isoformat(),
    }


# ---------------------------------------------------------------------------
# Compilation
# ---------------------------------------------------------------------------

def load_entries(
    content_dir: Path, today: dt.date, allow_examples: bool, diags: Diagnostics
) -> list[dict[str, Any]]:
    entries: list[dict[str, Any]] = []
    seen: dict[tuple[str, str], str] = {}

    paths = sorted(content_dir.rglob("*.yaml")) + sorted(content_dir.rglob("*.yml"))
    for path in paths:
        try:
            doc = yaml.safe_load(path.read_text(encoding="utf-8"))
        except yaml.YAMLError as exc:
            diags.error(path.name, f"could not be parsed as YAML: {exc}")
            continue

        entry = validate_entry(doc, path, today, allow_examples, diags)
        if entry is None:
            continue

        identity = (entry["violationKey"], entry["jurisdiction"])
        if identity in seen:
            diags.error(
                path.name,
                f"duplicate ({identity[0]}, {identity[1]}) — already defined in {seen[identity]}",
            )
            continue
        seen[identity] = path.name
        entries.append(entry)

    entries.sort(key=lambda e: (e["jurisdiction"], e["violationKey"]))
    return entries


def build_bundle(entries: list[dict[str, Any]], today: dt.date) -> dict[str, Any]:
    """Bundle with a content hash over the entries, so a build maps to exactly one content version."""
    payload = json.dumps(entries, sort_keys=True, separators=(",", ":"), ensure_ascii=False)
    digest = hashlib.sha256(payload.encode("utf-8")).hexdigest()
    return {
        "bundleFormatVersion": BUNDLE_FORMAT_VERSION,
        "generatedOn": today.isoformat(),
        "contentHash": f"sha256:{digest}",
        "jurisdictions": sorted({e["jurisdiction"] for e in entries}),
        "entryCount": len(entries),
        "entries": entries,
    }


KOTLIN_HEADER = """// Generated by tools/content-compiler/compile_content.py — do not edit by hand.
//
// The closed violation vocabulary (AI rule 1). The remote classifier may only return keys
// named here, and LegalAnalysisRepository must reject anything else before it reaches the DAO.
// Generated from the same authored content as the shipped bundle, so a key can never exist
// without verified content behind it.

package com.pocketlegal.advice.data.local

"""


def render_kotlin(entries: list[dict[str, Any]], bundle: dict[str, Any]) -> str:
    keys = sorted({e["violationKey"] for e in entries})
    lines = [KOTLIN_HEADER]
    lines.append(f'internal const val CONTENT_HASH: String = "{bundle["contentHash"]}"\n')
    lines.append("enum class ViolationKey(val key: String) {")
    for key in keys:
        lines.append(f'    {key}("{key}"),')
    lines.append("    ;")
    lines.append("")
    lines.append("    companion object {")
    lines.append("        private val byKey: Map<String, ViolationKey> = entries.associateBy { it.key }")
    lines.append("")
    lines.append("        /** Returns null for any key outside the closed vocabulary. */")
    lines.append("        fun fromKeyOrNull(raw: String): ViolationKey? = byKey[raw]")
    lines.append("    }")
    lines.append("}")
    return "\n".join(lines) + "\n"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--content-dir", type=Path, default=Path("content"))
    parser.add_argument("--out-assets", type=Path, default=None,
                        help="path for the JSON bundle (default: app/src/main/assets/legal-content.json)")
    parser.add_argument("--out-kotlin", type=Path, default=None,
                        help="path for the generated enum "
                             "(default: app/src/main/java/com/pocketlegal/advice/data/local/ViolationKey.kt)")
    parser.add_argument("--strict", action="store_true", help="treat warnings as failures (use in CI)")
    parser.add_argument("--allow-examples", action="store_true",
                        help="permit status: EXAMPLE files — never use for a shipping build")
    parser.add_argument("--today", type=dt.date.fromisoformat, default=dt.date.today(),
                        help="override today's date, for deterministic testing")
    parser.add_argument("--check", action="store_true", help="validate only, write nothing")
    args = parser.parse_args(argv)

    if not args.content_dir.is_dir():
        print(f"error: content directory not found: {args.content_dir}", file=sys.stderr)
        return 1

    diags = Diagnostics()
    entries = load_entries(args.content_dir, args.today, args.allow_examples, diags)
    bundle = build_bundle(entries, args.today)

    for message in diags.excluded:
        print(f"excluded  {message}")
    for message in diags.warnings:
        print(f"warning   {message}")
    for message in diags.errors:
        print(f"error     {message}", file=sys.stderr)

    if diags.errors:
        print(f"\nFAILED — {len(diags.errors)} error(s), nothing written.", file=sys.stderr)
        return 1

    if not args.check:
        assets_path = args.out_assets or Path("app/src/main/assets/legal-content.json")
        kotlin_path = args.out_kotlin or Path(
            "app/src/main/java/com/pocketlegal/advice/data/local/ViolationKey.kt"
        )
        assets_path.parent.mkdir(parents=True, exist_ok=True)
        kotlin_path.parent.mkdir(parents=True, exist_ok=True)
        assets_path.write_text(
            json.dumps(bundle, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )
        kotlin_path.write_text(render_kotlin(entries, bundle), encoding="utf-8")
        print(f"\nwrote {assets_path}")
        print(f"wrote {kotlin_path}")

    print(
        f"\n{len(entries)} entr{'y' if len(entries) == 1 else 'ies'} "
        f"across {len(bundle['jurisdictions'])} jurisdiction(s) · "
        f"{len(diags.excluded)} excluded · {len(diags.warnings)} warning(s)"
    )
    print(f"content hash {bundle['contentHash']}")

    if args.strict and diags.warnings:
        print(f"\nFAILED — {len(diags.warnings)} warning(s) under --strict.", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
