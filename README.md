# Pocket-Legal-advice

Legal advice at your fingertips.

## CircleCI

This repository now includes a CircleCI pipeline at `/home/runner/work/Pocket-Legal-advice/Pocket-Legal-advice/.circleci/config.yml`.

- PR branches run `lintDebug`, `testDebugUnitTest`, and `assembleDebug`
- The `main` branch also runs `assembleRelease`
- Test reports, lint reports, and generated build outputs are stored as CircleCI artifacts
- Gradle dependencies and wrapper downloads are cached between builds

To make the pipeline useful in GitHub:

1. Connect the repository to CircleCI
2. Require the CircleCI status checks in branch protection
3. Add a CircleCI badge after the first pipeline run using your project URL
