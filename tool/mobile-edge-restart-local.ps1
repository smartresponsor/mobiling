# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$edgeRoot = Join-Path $repositoryRoot "mobile-edge"
$npm = Get-Command npm.cmd -ErrorAction SilentlyContinue

if ($null -eq $npm) {
    throw "npm.cmd was not found on PATH."
}

Push-Location $edgeRoot
try {
    & $npm.Source run dev:restart
    if ($LASTEXITCODE -ne 0) {
        throw "mobile-edge local restart failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
