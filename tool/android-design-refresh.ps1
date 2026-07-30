# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$AvdName = "Mobiling_Light_API_34",
    [string]$PackageName = "app.mobiling.client",
    [string]$ActivityName = ".MainActivity",
    [string]$ScreenshotName = "latest",
    [switch]$SkipBuild,
    [switch]$SkipLaunch
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-AdbDeviceSerial {
    param(
        [string]$RequestedSerial,
        [string]$RequestedAvdName
    )

    $adb = Get-Command adb -ErrorAction SilentlyContinue
    if ($null -eq $adb) {
        throw "adb was not found on PATH."
    }

    $available = @(
        & adb devices |
            Select-Object -Skip 1 |
            Where-Object { $_ -match "\tdevice$" } |
            ForEach-Object { ($_ -split "\s+")[0] }
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedSerial)) {
        if ($available -notcontains $RequestedSerial) {
            throw "Requested Android device '$RequestedSerial' is not ready. Available: $($available -join ', ')"
        }

        return $RequestedSerial
    }

    if (-not [string]::IsNullOrWhiteSpace($RequestedAvdName)) {
        foreach ($serial in ($available | Where-Object { $_ -like "emulator-*" })) {
            $runningAvdName = ((& adb -s $serial emu avd name 2>$null | Select-Object -First 1) | Out-String).Trim()
            if ($runningAvdName -eq $RequestedAvdName) {
                return $serial
            }
        }

        throw "Android AVD '$RequestedAvdName' is not ready. Start it before refresh."
    }

    $emulator = @($available | Where-Object { $_ -like "emulator-*" } | Select-Object -First 1)
    if ($emulator.Count -eq 1) {
        return $emulator[0]
    }

    if ($available.Count -eq 1) {
        return $available[0]
    }

    if ($available.Count -eq 0) {
        throw "No ready Android device was found. Start the emulator and verify 'adb devices'."
    }

    throw "Multiple Android devices are ready. Pass -DeviceSerial. Available: $($available -join ', ')"
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot "client/android"
$gradleWrapper = Join-Path $androidRoot "gradlew.bat"
$apkPath = Join-Path $androidRoot "app/build/outputs/apk/debug/app-debug.apk"
$runtimeRoot = Join-Path $repositoryRoot "watchdog/runtime/device"
$historyRoot = Join-Path $runtimeRoot "history"

if (-not (Test-Path $gradleWrapper)) {
    throw "Gradle wrapper was not found at $gradleWrapper"
}

$resolvedSerial = Resolve-AdbDeviceSerial -RequestedSerial $DeviceSerial -RequestedAvdName $AvdName
& adb -s $resolvedSerial wait-for-device
& adb -s $resolvedSerial shell settings put global window_animation_scale 0
& adb -s $resolvedSerial shell settings put global transition_animation_scale 0
& adb -s $resolvedSerial shell settings put global animator_duration_scale 0

if (-not $SkipBuild) {
    Push-Location $androidRoot
    try {
        & $gradleWrapper :app:assembleDebug --console=plain --stacktrace --no-daemon
        if ($LASTEXITCODE -ne 0) {
            throw "Debug APK build failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}

if (-not (Test-Path $apkPath)) {
    throw "Debug APK was not found at $apkPath"
}

& adb -s $resolvedSerial install -r -t $apkPath
if ($LASTEXITCODE -ne 0) {
    throw "APK installation failed with exit code $LASTEXITCODE."
}

if (-not $SkipLaunch) {
    & adb -s $resolvedSerial shell am start -W -n "$PackageName/$ActivityName"
    if ($LASTEXITCODE -ne 0) {
        throw "Application launch command failed with exit code $LASTEXITCODE."
    }
}

Start-Sleep -Milliseconds 500
$processId = ((& adb -s $resolvedSerial shell pidof $PackageName) | Out-String).Trim()
if ([string]::IsNullOrWhiteSpace($processId)) {
    throw "Application process '$PackageName' is not running after installation and launch."
}

New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
New-Item -ItemType Directory -Force -Path $historyRoot | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$safeName = ($ScreenshotName -replace "[^a-zA-Z0-9._-]", "-").Trim("-")
if ([string]::IsNullOrWhiteSpace($safeName)) {
    $safeName = "latest"
}

$latestScreenshot = Join-Path $runtimeRoot "latest.png"
$historyScreenshot = Join-Path $historyRoot "$timestamp-$safeName.png"

Start-Sleep -Milliseconds 700
& adb -s $resolvedSerial exec-out screencap -p > $latestScreenshot
if ($LASTEXITCODE -ne 0) {
    throw "Screenshot capture failed with exit code $LASTEXITCODE."
}

Copy-Item -Force -Path $latestScreenshot -Destination $historyScreenshot

$result = [ordered]@{
    ok = $true
    deviceSerial = $resolvedSerial
    avdName = $AvdName
    processId = $processId
    packageName = $PackageName
    activityName = $ActivityName
    apkPath = $apkPath
    latestScreenshot = $latestScreenshot
    historyScreenshot = $historyScreenshot
    generatedAt = (Get-Date).ToString("o")
}

$result | ConvertTo-Json -Depth 4
