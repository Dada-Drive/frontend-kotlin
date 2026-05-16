# Installs the project's Git hooks for Windows (PowerShell 7+).
# Git for Windows invokes hooks through its bundled sh.exe, not PowerShell, so we
# install a tiny POSIX shim at .git/hooks/pre-commit that calls pwsh on the .ps1.
# This avoids needing admin/Developer Mode for symlinks.
$ErrorActionPreference = "Stop"

$repoRoot = (& git rev-parse --show-toplevel).Trim()
$gitDir   = (& git rev-parse --git-dir).Trim()
$hook     = Join-Path $gitDir "hooks\pre-commit"
$source   = Join-Path $repoRoot "scripts\pre-commit.ps1"

if (-not (Test-Path $source)) {
    Write-Error "[install-hooks] missing $source"
    exit 1
}

# Idempotency: back up an existing non-managed hook instead of silently overwriting.
if (Test-Path $hook) {
    $existing = Get-Content $hook -Raw -ErrorAction SilentlyContinue
    if ($existing -notmatch 'pre-commit\.ps1') {
        $stamp = [int][double]::Parse((Get-Date -UFormat %s))
        Move-Item $hook "$hook.bak.$stamp"
        Write-Host "[install-hooks] Backed up existing hook -> $hook.bak.$stamp"
    }
}

# POSIX shim. Use forward slashes for the path so Git's sh.exe handles it correctly.
$posixSource = $source -replace '\\','/'
$shim = "#!/bin/sh`nexec pwsh -NoProfile -ExecutionPolicy Bypass -File `"$posixSource`" `"`$@`"`n"
Set-Content -Path $hook -Value $shim -Encoding ascii -NoNewline

Write-Host "[install-hooks] OK -- pre-commit shim installed at $hook"
Write-Host "[install-hooks]    delegates to $source"
