[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot "client/android"
$gradle = Join-Path $androidRoot "gradlew.bat"

if (-not (Test-Path $gradle)) {
    throw "Gradle wrapper was not found at $gradle"
}

Push-Location $androidRoot
try {
    & $gradle ":app:compileDebugKotlin" ":app:compileDebugAndroidTestKotlin"
    if ($LASTEXITCODE -ne 0) { throw "Android compile gate failed with exit code $LASTEXITCODE." }
} finally {
    Pop-Location
}
