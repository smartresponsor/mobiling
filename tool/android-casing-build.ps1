$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$android = Join-Path $root "client/android"
$wrapper = Join-Path $android "gradlew.bat"
if (-not (Test-Path $wrapper)) { throw "Gradle wrapper not found." }
Push-Location $android
try {
    & $wrapper :app:assembleDebug --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Android build failed with exit code $LASTEXITCODE." }
}
finally { Pop-Location }
Write-Host "Android Casing build gate passed."
