[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$Commit,
    [switch]$Push
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

function Invoke-Step {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][scriptblock]$Action
    )

    Write-Host "`n==> $Name" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed with exit code $LASTEXITCODE"
    }
}

if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    throw 'Python is required.'
}

Invoke-Step 'Materialize Android' { python tools/materialize_android.py }
Invoke-Step 'Materialize iOS' { python tools/materialize_ios.py }
Invoke-Step 'Materialize Mobile Edge runtime' { python tools/materialize_edge_runtime.py }
Invoke-Step 'Materialize Mobile Edge storage' { python tools/materialize_edge_storage.py }
Invoke-Step 'Finalize repository' { python tools/materialize_finalize.py }

Write-Host "`n==> Remove one-time bootstrap" -ForegroundColor Cyan
Remove-Item -Recurse -Force .materialize -ErrorAction SilentlyContinue
Remove-Item -Force tools/materialize_android.py -ErrorAction SilentlyContinue
Remove-Item -Force tools/materialize_ios.py -ErrorAction SilentlyContinue
Remove-Item -Force tools/materialize_edge_runtime.py -ErrorAction SilentlyContinue
Remove-Item -Force tools/materialize_edge_storage.py -ErrorAction SilentlyContinue
Remove-Item -Force tools/materialize_finalize.py -ErrorAction SilentlyContinue
Remove-Item -Force .github/workflows/materialize.yml -ErrorAction SilentlyContinue
Remove-Item -Force .github/workflows/materialize-retry.yml -ErrorAction SilentlyContinue

Invoke-Step 'Validate repository' { python tools/validate_repository.py }

if (-not $SkipBuild) {
    if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
        throw 'npm is required unless -SkipBuild is used.'
    }

    Push-Location mobile-edge
    try {
        Invoke-Step 'Install Mobile Edge dependencies' { npm install --no-audit --no-fund }
        Invoke-Step 'Build Mobile Edge' { npm run build }
    }
    finally {
        Pop-Location
    }

    Push-Location mobile-client/android
    try {
        if (Test-Path .\gradlew.bat) {
            Invoke-Step 'Compile Android' { .\gradlew.bat :app:compileDebugKotlin --stacktrace }
        }
        elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
            Invoke-Step 'Compile Android' { gradle :app:compileDebugKotlin --stacktrace }
        }
        else {
            throw 'Gradle or gradlew.bat is required unless -SkipBuild is used.'
        }
    }
    finally {
        Pop-Location
    }
}

if ($Commit -or $Push) {
    Invoke-Step 'Stage materialized baseline' { git add -A }

    $Pending = git diff --cached --name-only
    if ($Pending) {
        Invoke-Step 'Commit materialized baseline' { git commit -m 'materialize mobile runtime baseline' }
    }
    else {
        Write-Host 'No staged changes to commit.' -ForegroundColor Yellow
    }
}

if ($Push) {
    Invoke-Step 'Push master' { git push origin master }
}

Write-Host "`nMobiling materialization completed." -ForegroundColor Green
