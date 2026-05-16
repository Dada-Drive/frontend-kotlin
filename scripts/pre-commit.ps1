# Pre-commit hook: runs ktlintCheck + detekt against staged Kotlin changes.
# Installed via scripts/install-hooks.ps1.
$ErrorActionPreference = "Stop"

$repoRoot = (& git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

# Fast-path: skip when no .kt/.kts is staged (commits touching only docs/res/etc.).
$changed = (& git diff --cached --name-only --diff-filter=ACMR) | Where-Object { $_ -match '\.kts?$' }
if (-not $changed) { exit 0 }

Write-Host "[pre-commit] Running ktlint + detekt on staged Kotlin changes..."
& .\gradlew.bat ktlintCheck detekt --console=plain
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "==============================================================================" -ForegroundColor Red
    Write-Host "  COMMIT REJECTED -- ktlint or detekt violations were found." -ForegroundColor Red
    Write-Host "------------------------------------------------------------------------------" -ForegroundColor Red
    Write-Host "  Auto-fix:  .\gradlew.bat ktlintFormat"
    Write-Host "  Re-stage:  git add -u && git commit"
    Write-Host "  Bypass:    git commit --no-verify  (sparingly -- CI still gates)"
    Write-Host "==============================================================================" -ForegroundColor Red
    exit 1
}
