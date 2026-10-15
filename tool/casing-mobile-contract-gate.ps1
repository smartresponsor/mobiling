$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
    & python tools/validate_repository.py
    if ($LASTEXITCODE -ne 0) { throw "Repository validation failed." }
}
finally { Pop-Location }

$swift = Get-Command swift -ErrorAction SilentlyContinue
if ($null -ne $swift) {
    Push-Location (Join-Path $root "client/ios")
    try {
        & swift package dump-package | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "Swift package manifest validation failed." }
    }
    finally { Pop-Location }
    Write-Host "Swift package manifest gate passed."
} else {
    Write-Host "Swift toolchain is unavailable on this Windows host; Xcode build remains CI-only."
}
