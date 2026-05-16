#!/usr/bin/env bash
# Pre-commit hook: runs ktlintCheck + detekt against staged Kotlin changes.
# Installed via scripts/install-hooks.sh.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

# Fast-path: skip when no .kt/.kts is staged (commits touching only docs/res/etc.).
CHANGED_KT="$(git diff --cached --name-only --diff-filter=ACMR | grep -E '\.kts?$' || true)"
if [[ -z "$CHANGED_KT" ]]; then
  exit 0
fi

echo "[pre-commit] Running ktlint + detekt on staged Kotlin changes..."
if ! ./gradlew ktlintCheck detekt --console=plain; then
  cat <<'EOF' >&2

==============================================================================
  COMMIT REJECTED -- ktlint or detekt violations were found.
------------------------------------------------------------------------------
  Auto-fix what is auto-fixable:
    ./gradlew ktlintFormat

  Re-stage and try again:
    git add -u && git commit

  Bypass exceptionally (use sparingly -- CI will still gate):
    git commit --no-verify
==============================================================================
EOF
  exit 1
fi
