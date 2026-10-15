# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$AvdName = "Mobiling_Light_API_34",
    [int]$PollMilliseconds = 750,
    [int]$DebounceMilliseconds = 1200,
    [string]$ScreenshotName = "live"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot "client/android"
$refreshScript = Join-Path $PSScriptRoot "android-design-refresh.ps1"
$statusRoot = Join-Path $repositoryRoot "watchdog/runtime/design"
$statusFile = Join-Path $statusRoot "latest.json"
$logFile = Join-Path $statusRoot "watch.log"

if (-not (Test-Path $refreshScript)) {
    throw "Design refresh script was not found at $refreshScript"
}

New-Item -ItemType Directory -Force -Path $statusRoot | Out-Null

$watchedExtensions = @(
    ".kt",
    ".kts",
    ".xml",
    ".properties",
    ".pro"
)

function Get-InputSnapshot {
    $files = Get-ChildItem -Path $androidRoot -Recurse -File |
        Where-Object {
            $watchedExtensions -contains $_.Extension -and
            $_.FullName -notmatch "[\\/](build|\.gradle)[\\/]"
        } |
        Sort-Object FullName

    $signatureInput = foreach ($file in $files) {
        "$($file.FullName)|$($file.Length)|$($file.LastWriteTimeUtc.Ticks)"
    }

    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes(($signatureInput -join "`n"))
        $hash = [System.BitConverter]::ToString($sha.ComputeHash($bytes)).Replace("-", "").ToLowerInvariant()
    }
    finally {
        $sha.Dispose()
    }

    return [ordered]@{
        hash = $hash
        fileCount = $files.Count
        generatedAt = (Get-Date).ToString("o")
    }
}

function Write-Status {
    param(
        [string]$State,
        [string]$Message,
        [object]$Snapshot,
        [object]$RefreshResult = $null
    )

    $status = [ordered]@{
        state = $State
        message = $Message
        avdName = $AvdName
        deviceSerial = $DeviceSerial
        processId = $PID
        snapshot = $Snapshot
        refresh = $RefreshResult
        updatedAt = (Get-Date).ToString("o")
    }

    $status | ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 -Path $statusFile
    "$(Get-Date -Format o) [$State] $Message" | Add-Content -Encoding utf8 -Path $logFile
}

$snapshot = Get-InputSnapshot
Write-Status -State "WATCHING" -Message "Android design watch started for AVD '$AvdName'." -Snapshot $snapshot

while ($true) {
    Start-Sleep -Milliseconds $PollMilliseconds
    $candidate = Get-InputSnapshot

    if ($candidate.hash -eq $snapshot.hash) {
        continue
    }

    Start-Sleep -Milliseconds $DebounceMilliseconds
    $stableCandidate = Get-InputSnapshot
    if ($stableCandidate.hash -ne $candidate.hash) {
        continue
    }

    Write-Status -State "BUILDING" -Message "Android source change detected." -Snapshot $stableCandidate

    try {
        $arguments = @("-ScreenshotName", $ScreenshotName, "-AvdName", $AvdName)
        if (-not [string]::IsNullOrWhiteSpace($DeviceSerial)) {
            $arguments += @("-DeviceSerial", $DeviceSerial)
        }

        $output = & powershell -NoProfile -ExecutionPolicy Bypass -File $refreshScript @arguments 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw ($output -join "`n")
        }

        $jsonStart = ($output | Select-String -Pattern '^\{' | Select-Object -Last 1).LineNumber
        $refreshResult = $null
        if ($null -ne $jsonStart) {
            $json = ($output[($jsonStart - 1)..($output.Count - 1)] -join "`n")
            $refreshResult = $json | ConvertFrom-Json
        }

        $snapshot = $stableCandidate
        Write-Status -State "GREEN" -Message "APK refreshed on emulator and screenshot captured." -Snapshot $snapshot -RefreshResult $refreshResult
    }
    catch {
        Write-Status -State "RED" -Message $_.Exception.Message -Snapshot $stableCandidate
    }
}
