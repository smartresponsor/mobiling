# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial = "emulator-5554",
    [int]$TailLines = 500
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$outputDirectory = Join-Path $repositoryRoot "build/runtime-log"
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputPath = Join-Path $outputDirectory "$timestamp-$DeviceSerial.txt"

& adb -s $DeviceSerial logcat -d -v threadtime |
    Select-String -Pattern "app\.mobiling\.client|MobileClient|OkHttp|Retrofit|session|access|Unable|Exception|HTTP" |
    Select-Object -Last $TailLines |
    ForEach-Object { $_.Line } |
    Set-Content -Encoding utf8 -Path $outputPath

Write-Host $outputPath
