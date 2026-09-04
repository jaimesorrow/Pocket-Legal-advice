---
name: ci-wiring-review
description: Checks whether this repo's CircleCI config (.circleci/config.yml) is actually wired up as an enforced check on GitHub, versus being dead tooling nobody runs. Use this whenever a diff touches .circleci/config.yml, adds a new Gradle verification task (lint/test/detekt/ktlint/etc.), or when anyone claims "CI passes"/"CI is green" for this repo — that claim needs verifying, not assuming.
---

# CI wiring review: does CircleCI actually run here?

`.circleci/config.yml` defines a real, well-structured two-workflow pipeline (`pull-request`:
lint + unit-test + build-debug; `main-branch`: lint + unit-test + build-release, gated on both
checks) — but **as of this writing, verified against GitHub's own PR check-run data, it has never
once posted a status to a pull request in this repo.**

Verified evidence (checked via the GitHub API against real merged/reviewed PRs):
- PR #6 ("Fix CI: add gradle wrapper, test deps, and app/src/main"), PR #9 ("Back ActionStepDao
  with Room..."), and PR #10 (this repo's own code-review-skill PR) all show `total_count: 0`,
  `statuses: []` for their head commit — no CircleCI status, no CircleCI check run, nothing.
- The only check that has ever run on these PRs is a GitHub Actions-backed
  `copilot-pull-request-reviewer` job — proving GitHub Actions/App-based checks *can* reach this
  repo, which makes CircleCI's total silence a wiring gap rather than a platform limitation.
- The `.circleci/config.yml` file itself was authored by `circleci-app[bot]` (commit `2cad115`),
  which only commits via CircleCI's "Set Up Project" GitHub flow — so the config was generated
  through a real integration attempt, but no evidence since then shows CircleCI posting results
  back to this repo's PRs.

The most likely explanation: the CircleCI project was never actually followed/enabled on
circleci.com for this repo (or was disconnected since), so the config file is committed but inert.
This is exactly the "dead tooling nobody runs" failure mode — anyone reading `.circleci/config.yml`
and this repo's CLAUDE.md would reasonably assume lint and unit tests gate every PR; they do not.

## What to check on a relevant diff

1. **Never accept "CI is green" or "the pipeline passed" as a review signal for this repo without
   independently confirming a check actually posted** (e.g. via `pull_request_read` /
   `get_status` / `get_check_runs` against the PR's head commit, or asking the human to paste a
   CircleCI run URL). Silence is the default here, not evidence of an unattempted or broken run.
2. **A diff to `.circleci/config.yml` alone does not fix this.** Editing the workflow logic
   (adding a job, changing a filter, fixing a cache key) does not restore GitHub-side reporting if
   the underlying CircleCI project/GitHub App connection is what's actually missing. Flag any PR
   description that claims a CircleCI config change "makes CI run again" without addressing the
   connection itself (circleci.com project setup, or the CircleCI GitHub App's repo permissions).
3. **A new Gradle check added without a CI story is doubly unenforced.** If a diff adds a new
   verification step (a linter, a detekt/ktlint config, a new test module) and wires it only into
   `.circleci/config.yml`, point out that this makes it *2 layers* unenforced: the new step is not
   run by any human/agent unless they run `./gradlew` manually (per `pocket-lawbook-review`'s
   "Running checks" section), **and** CircleCI itself isn't reporting to GitHub, so nothing blocks
   a PR from merging regardless of what the new check finds.
4. **If asked to actually fix this** (as opposed to just reviewing a diff), the two concrete paths
   are: (a) go to circleci.com, follow/enable this project against `jaimesorrow/Pocket-Legal-advice`
   so the GitHub App starts posting check runs, or (b) since GitHub Actions is confirmed reachable
   here (`copilot-pull-request-reviewer` already reports successfully), port `.circleci/config.yml`'s
   three jobs to a `.github/workflows/*.yml` instead of relying on the CircleCI integration. Don't
   assume either fix without the human weighing in — this is an external-service configuration
   decision, not a pure code change.
5. **Re-verify before relying on this file's own claim.** If enough time has passed, or the human
   says CircleCI now works, redo the `pull_request_read` (`get_status` / `get_check_runs`) check
   against the most recent PR's head commit before repeating "CircleCI doesn't report here" — this
   skill's evidence is a snapshot, not a permanent fact about the repo.
