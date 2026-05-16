#!/usr/bin/env bash
# Installs the project's Git hooks for macOS / Linux.
# Symlinks .git/hooks/pre-commit -> scripts/pre-commit.sh so future updates
# are picked up automatically without re-running the installer.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
GIT_DIR="$(git rev-parse --git-dir)"
HOOK="$GIT_DIR/hooks/pre-commit"
TARGET_REL="../../scripts/pre-commit.sh"

# Idempotency: back up an existing non-managed hook so we never silently overwrite work.
if [[ -e "$HOOK" && ! -L "$HOOK" ]]; then
  backup="$HOOK.bak.$(date +%s)"
  mv "$HOOK" "$backup"
  echo "[install-hooks] Backed up existing hook -> $backup"
fi

ln -sf "$TARGET_REL" "$HOOK"
chmod +x "$REPO_ROOT/scripts/pre-commit.sh"

resolved="$(cd "$(dirname "$HOOK")" && readlink "$HOOK" || true)"
if [[ -x "$REPO_ROOT/scripts/pre-commit.sh" ]]; then
  echo "[install-hooks] OK -- pre-commit installed at $HOOK"
  echo "[install-hooks]    -> $resolved"
else
  echo "[install-hooks] ERROR: hook target is not executable" >&2
  exit 1
fi

case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*)
    echo "[install-hooks] Note: detected Windows shell. For native PowerShell, run scripts/install-hooks.ps1 instead."
    ;;
esac
