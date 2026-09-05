# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$AvdName = "Mobiling_Light_API_34",
    [string]$PackageName = "app.mobiling.client.onetasker",
    [string]$ActivityName = "app.mobiling.client.MainActivity",
    [string]$ScreenshotName = "latest",
    [switch]$SkipBuild,
    [switch]$CleanBuild,
    [switch]$SkipLaunch,
    [switch]$ResetAppData,
    [switch]$DisableDeviceAnimations
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

function Dismiss-SystemUiAnrIfPresent {
    param([string]$Serial)

    $devicePath = "/sdcard/mobiling-window-dump.xml"
    $safeSerial = $Serial -replace "[^a-zA-Z0-9._-]", "-"
    $hostPath = Join-Path $env:TEMP "mobiling-window-dump-$safeSerial.xml"

    cmd.exe /c "adb -s $Serial shell uiautomator dump $devicePath >nul 2>nul"
    if ($LASTEXITCODE -ne 0) {
        return
    }

    cmd.exe /c "adb -s $Serial pull $devicePath `"$hostPath`" >nul 2>nul"
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $hostPath)) {
        return
    }

    $dump = Get-Content $hostPath -Raw
    if ($dump -match "System UI isn(&apos;|')t responding" -or $dump -match "android:id/aerr_wait") {
        # The light AVD can show a platform ANR while boot settles. Select Wait before install/launch.
        & adb -s $Serial shell input tap 540 1128 2>$null | Out-Null
        Start-Sleep -Seconds 2
    }
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot "client/android"
$gradleWrapper = Join-Path $androidRoot "gradlew.bat"
$apkPath = switch ($PackageName) {
    "app.mobiling.client.smartresponsor" { Join-Path $androidRoot "app/build/outputs/apk/smartResponsor/debug/app-smartResponsor-debug.apk" }
    "app.mobiling.client.onetasker" { Join-Path $androidRoot "app/build/outputs/apk/oneTasker/debug/app-oneTasker-debug.apk" }
    default { throw "Unsupported Android package '$PackageName'." }
}
$runtimeRoot = Join-Path $repositoryRoot "watchdog/runtime/device"
$historyRoot = Join-Path $runtimeRoot "history"

if (-not (Test-Path $gradleWrapper)) {
    throw "Gradle wrapper was not found at $gradleWrapper"
}

$resolvedSerial = Resolve-AdbDeviceSerial -RequestedSerial $DeviceSerial -RequestedAvdName $AvdName
& adb -s $resolvedSerial wait-for-device
if ($DisableDeviceAnimations) {
    & adb -s $resolvedSerial shell settings put global window_animation_scale 0
    & adb -s $resolvedSerial shell settings put global transition_animation_scale 0
    & adb -s $resolvedSerial shell settings put global animator_duration_scale 0
} else {
    & adb -s $resolvedSerial shell settings put global window_animation_scale 1
    & adb -s $resolvedSerial shell settings put global transition_animation_scale 1
    & adb -s $resolvedSerial shell settings put global animator_duration_scale 1
}
Start-Sleep -Seconds 2
Dismiss-SystemUiAnrIfPresent -Serial $resolvedSerial

if (-not $SkipBuild) {
    Push-Location $androidRoot
    try {
        if ($CleanBuild) {
            & $gradleWrapper --stop | Out-Null
            & $gradleWrapper clean --console=plain --stacktrace --no-daemon
            if ($LASTEXITCODE -ne 0) {
                throw "Gradle clean failed with exit code $LASTEXITCODE."
            }
        }
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

if ($ResetAppData) {
    & adb -s $resolvedSerial uninstall $PackageName 2>$null | Out-Null
}

Dismiss-SystemUiAnrIfPresent -Serial $resolvedSerial
$siblingPackage = switch ($PackageName) {
    "app.mobiling.client.onetasker" { "app.mobiling.client.smartresponsor" }
    "app.mobiling.client.smartresponsor" { "app.mobiling.client.onetasker" }
}
if (-not [string]::IsNullOrWhiteSpace($siblingPackage)) {
    & adb -s $resolvedSerial uninstall $siblingPackage 2>$null | Out-Null
}
& adb -s $resolvedSerial install -r -t $apkPath
if ($LASTEXITCODE -ne 0) {
    throw "APK installation failed with exit code $LASTEXITCODE."
}

if (-not $SkipLaunch) {
    & adb -s $resolvedSerial shell am force-stop $PackageName | Out-Null
    Start-Sleep -Milliseconds 500
    & adb -s $resolvedSerial shell am start -W -n "$PackageName/$ActivityName"
    if ($LASTEXITCODE -ne 0) {
        throw "Application launch command failed with exit code $LASTEXITCODE."
    }
}

$foregroundDeadline = (Get-Date).AddSeconds(35)
$foregroundReady = $false
do {
    Start-Sleep -Milliseconds 500
    $focusedWindow = ((& adb -s $resolvedSerial shell dumpsys activity activities 2>$null | Select-String "mResumedActivity|topResumedActivity|ResumedActivity") | Out-String)
    if ($focusedWindow -match [regex]::Escape($PackageName)) {
        $foregroundReady = $true
        break
    }
} while ((Get-Date) -lt $foregroundDeadline)

$processId = ((& adb -s $resolvedSerial shell pidof $PackageName) | Out-String).Trim()
if ([string]::IsNullOrWhiteSpace($processId)) {
    throw "Application process '$PackageName' is not running after installation and launch."
}
if (-not $foregroundReady) {
    throw "Application '$PackageName' did not reach the foreground within 35 seconds."
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

$uiDumpDevicePath = "/sdcard/window_dump.xml"
$latestUiDump = Join-Path $runtimeRoot "current-ui.xml"
& adb -s $resolvedSerial shell uiautomator dump $uiDumpDevicePath | Out-Null
if ($LASTEXITCODE -eq 0) {
    & adb -s $resolvedSerial pull $uiDumpDevicePath $latestUiDump | Out-Null
}

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
