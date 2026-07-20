# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$TestClass = "app.mobiling.client.access.AccessBehaviorTest",
    [string]$OutputRoot = "build/test-result"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot "client/android"
$gradleWrapper = Join-Path $androidRoot "gradlew.bat"

if (-not (Test-Path $gradleWrapper)) {
    throw "Gradle wrapper was not found at $gradleWrapper"
}

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($null -eq $adbCommand) {
    throw "adb was not found on PATH. Install Android SDK Platform Tools and reopen the terminal."
}

if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
    $devices = & adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
    if ($devices.Count -ne 1) {
        throw "Specify -DeviceSerial when zero or multiple Android devices are available."
    }

    $DeviceSerial = ($devices[0] -split "\s+")[0]
}

$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$outputDirectory = Join-Path $repositoryRoot (Join-Path $OutputRoot $runId)
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$instrumentationLog = Join-Path $outputDirectory "instrumentation.txt"
$logcatFull = Join-Path $outputDirectory "logcat-full.txt"
$logcatCrash = Join-Path $outputDirectory "logcat-crash.txt"
$deviceInfo = Join-Path $outputDirectory "device.txt"
$packageInfo = Join-Path $outputDirectory "packages.txt"
$windowInfo = Join-Path $outputDirectory "window.txt"
$hierarchy = Join-Path $outputDirectory "hierarchy.xml"
$screenshot = Join-Path $outputDirectory "screenshot.png"

& adb -s $DeviceSerial wait-for-device
& adb -s $DeviceSerial shell getprop | Out-File -Encoding utf8 $deviceInfo
& adb -s $DeviceSerial shell pm list packages | Out-File -Encoding utf8 $packageInfo
& adb -s $DeviceSerial logcat -c

$previousAndroidSerial = $env:ANDROID_SERIAL
$env:ANDROID_SERIAL = $DeviceSerial

try {
    Push-Location $androidRoot
    try {
        & $gradleWrapper :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass" 2>&1 |
            Tee-Object -FilePath $instrumentationLog
        $testExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
finally {
    $env:ANDROID_SERIAL = $previousAndroidSerial

    & adb -s $DeviceSerial logcat -d | Out-File -Encoding utf8 $logcatFull
    & adb -s $DeviceSerial logcat -d "AndroidRuntime:E" "DEBUG:E" "*:S" | Out-File -Encoding utf8 $logcatCrash
    & adb -s $DeviceSerial shell dumpsys window | Out-File -Encoding utf8 $windowInfo
    & adb -s $DeviceSerial shell uiautomator dump /sdcard/window_dump.xml | Out-Null
    & adb -s $DeviceSerial pull /sdcard/window_dump.xml $hierarchy | Out-Null
    & adb -s $DeviceSerial exec-out screencap -p > $screenshot
}

if ($testExitCode -ne 0) {
    throw "Android access tests failed with exit code $testExitCode. Artifacts: $outputDirectory"
}

Write-Host "Android access tests passed. Artifacts: $outputDirectory"
