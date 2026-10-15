# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [int]$Port = 8080,
    [string]$HostAppBaseUrl = "http://127.0.0.1:8000"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$edgeRoot = Join-Path $repositoryRoot "mobile-edge"
$node = Get-Command node.exe -ErrorAction SilentlyContinue

if ($null -eq $node) {
    throw "node.exe was not found on PATH."
}

$env:PORT = [string]$Port
$env:LOCALIZING_API_BASE_URL = $HostAppBaseUrl
$env:ACCESSING_API_BASE_URL = $HostAppBaseUrl
$env:CRUDING_API_BASE_URL = $HostAppBaseUrl
$env:CATALOGING_API_BASE_URL = $HostAppBaseUrl

Push-Location $edgeRoot
try {
    & $node.Source "dist/app.js"
    if ($LASTEXITCODE -ne 0) {
        throw "mobile-edge exited with code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
