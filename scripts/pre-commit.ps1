$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
& .\gradlew.bat ktlintCheck detekt --no-daemon
exit $LASTEXITCODE
