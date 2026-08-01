[CmdletBinding()]
param(
    [string]$DeviceSerial = "emulator-5554",
    [Parameter(Mandatory = $true)][int]$X,
    [Parameter(Mandatory = $true)][int]$Y,
    [string]$ScreenshotName = "tap"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$runtimeRoot = Join-Path $repositoryRoot "watchdog/runtime/device"
$historyRoot = Join-Path $runtimeRoot "history"

& adb -s $DeviceSerial shell input tap $X $Y
if ($LASTEXITCODE -ne 0) { throw "Android tap failed." }
Start-Sleep -Milliseconds 1200

New-Item -ItemType Directory -Force -Path $historyRoot | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$safeName = ($ScreenshotName -replace "[^a-zA-Z0-9._-]", "-").Trim("-")
$latestScreenshot = Join-Path $runtimeRoot "latest.png"
$historyScreenshot = Join-Path $historyRoot "$timestamp-$safeName.png"

& adb -s $DeviceSerial exec-out screencap -p > $latestScreenshot
if ($LASTEXITCODE -ne 0) { throw "Screenshot capture failed." }
Copy-Item -Force -Path $latestScreenshot -Destination $historyScreenshot
$uiDumpDevicePath = "/sdcard/window_dump.xml"
$uiDumpPath = Join-Path $runtimeRoot "current-ui.xml"
& adb -s $DeviceSerial shell uiautomator dump $uiDumpDevicePath | Out-Null
if ($LASTEXITCODE -eq 0) { & adb -s $DeviceSerial pull $uiDumpDevicePath $uiDumpPath | Out-Null }

[ordered]@{
    ok = $true
    deviceSerial = $DeviceSerial
    x = $X
    y = $Y
    screenshot = $historyScreenshot
} | ConvertTo-Json
