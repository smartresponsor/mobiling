# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$AvdName = "Mobiling_Light_API_34",
    [string]$TestClass = "app.mobiling.client.access.AccessBehaviorTest",
    [string]$OutputRoot = "build/test-result"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Remove-DirectoryWithRetry {
    param(
        [Parameter(Mandatory)]
        [string]$Path,
        [int]$Attempts = 5,
        [int]$DelayMilliseconds = 500
    )

    if (-not (Test-Path $Path)) {
        return
    }

    for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
        try {
            Remove-Item -Recurse -Force -Path $Path
            return
        }
        catch {
            if ($attempt -eq $Attempts) {
                throw "Unable to remove stale Android test output at $Path after $Attempts attempts: $($_.Exception.Message)"
            }

            Start-Sleep -Milliseconds $DelayMilliseconds
        }
    }
}

function Invoke-AdbDiagnostic {
    param(
        [Parameter(Mandatory)]
        [string[]]$Arguments,
        [Parameter(Mandatory)]
        [string]$OutputPath
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $nativeCommandPreferenceWasSet = Test-Path variable:PSNativeCommandUseErrorActionPreference
    if ($nativeCommandPreferenceWasSet) {
        $previousNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
        $PSNativeCommandUseErrorActionPreference = $false
    }

    try {
        $ErrorActionPreference = "Continue"
        & adb -s $DeviceSerial @Arguments 2>&1 | Out-File -Encoding utf8 $OutputPath
        $diagnosticExitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
        if ($nativeCommandPreferenceWasSet) {
            $PSNativeCommandUseErrorActionPreference = $previousNativeCommandPreference
        }
    }

    if ($diagnosticExitCode -ne 0) {
        Add-Content -Encoding utf8 -Path $OutputPath -Value "`nDiagnostic command exited with code ${diagnosticExitCode}: adb -s $DeviceSerial $($Arguments -join ' ')"
    }
}

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
    $devices = @(
        & adb devices |
            Select-Object -Skip 1 |
            Where-Object { $_ -match "\tdevice$" } |
            ForEach-Object { ($_ -split "\s+")[0] }
    )

    if (-not [string]::IsNullOrWhiteSpace($AvdName)) {
        foreach ($serial in ($devices | Where-Object { $_ -like "emulator-*" })) {
            $runningAvdName = ((& adb -s $serial emu avd name 2>$null | Select-Object -First 1) | Out-String).Trim()
            if ($runningAvdName -eq $AvdName) {
                $DeviceSerial = $serial
                break
            }
        }

        if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
            throw "Android AVD '$AvdName' is not ready. Start it before testing."
        }
    }
    elseif ($devices.Count -eq 1) {
        $DeviceSerial = $devices[0]
    }
    else {
        throw "Specify -DeviceSerial when zero or multiple Android devices are available."
    }
}

$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$outputDirectory = Join-Path $repositoryRoot (Join-Path $OutputRoot $runId)
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$instrumentationLog = Join-Path $outputDirectory "instrumentation.txt"
$commandLog = Join-Path $outputDirectory "command.txt"
$logcatFull = Join-Path $outputDirectory "logcat-full.txt"
$logcatCrash = Join-Path $outputDirectory "logcat-crash-buffer.txt"
$logcatEvents = Join-Path $outputDirectory "logcat-events.txt"
$deviceInfo = Join-Path $outputDirectory "device.txt"
$packageInfo = Join-Path $outputDirectory "packages.txt"
$appPackageInfo = Join-Path $outputDirectory "package-app.txt"
$testPackageInfo = Join-Path $outputDirectory "package-test.txt"
$instrumentationInfo = Join-Path $outputDirectory "instrumentation-registered.txt"
$activityProcesses = Join-Path $outputDirectory "activity-processes.txt"
$activityServices = Join-Path $outputDirectory "activity-services.txt"
$dropboxInfo = Join-Path $outputDirectory "dropbox-history.txt"
$dropboxRelevantInfo = Join-Path $outputDirectory "dropbox-relevant.txt"
$tombstoneInfo = Join-Path $outputDirectory "tombstones.txt"
$windowInfo = Join-Path $outputDirectory "window.txt"
$hierarchy = Join-Path $outputDirectory "hierarchy.xml"
$screenshot = Join-Path $outputDirectory "screenshot.png"

$gradleArguments = @(
    ":app:connectedDebugAndroidTest"
    "--console=plain"
    "--info"
    "--no-daemon"
    "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"
)

@(
    "timestamp=$(Get-Date -Format o)"
    "device=$DeviceSerial"
    "avdName=$AvdName"
    "testClass=$TestClass"
    "workingDirectory=$androidRoot"
    "command=$gradleWrapper $($gradleArguments -join ' ')"
) | Out-File -Encoding utf8 $commandLog

& adb -s $DeviceSerial wait-for-device
& adb -s $DeviceSerial shell settings put global window_animation_scale 0
& adb -s $DeviceSerial shell settings put global transition_animation_scale 0
& adb -s $DeviceSerial shell settings put global animator_duration_scale 0
Invoke-AdbDiagnostic -Arguments @("shell", "getprop") -OutputPath $deviceInfo
Invoke-AdbDiagnostic -Arguments @("shell", "pm", "list", "packages", "-f") -OutputPath $packageInfo
Invoke-AdbDiagnostic -Arguments @("shell", "pm", "dump", "app.mobiling.client") -OutputPath $appPackageInfo
Invoke-AdbDiagnostic -Arguments @("shell", "pm", "dump", "app.mobiling.client.test") -OutputPath $testPackageInfo
Invoke-AdbDiagnostic -Arguments @("shell", "pm", "list", "instrumentation") -OutputPath $instrumentationInfo
& adb -s $DeviceSerial logcat -c

$previousAndroidSerial = $env:ANDROID_SERIAL
$env:ANDROID_SERIAL = $DeviceSerial
$testExitCode = -1

try {
    Push-Location $androidRoot
    try {
        $connectedResultDirectory = Join-Path $androidRoot "app/build/outputs/androidTest-results/connected/debug"

        $cleanupErrorActionPreference = $ErrorActionPreference
        $cleanupNativeCommandPreferenceWasSet = Test-Path variable:PSNativeCommandUseErrorActionPreference
        if ($cleanupNativeCommandPreferenceWasSet) {
            $cleanupPreviousNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
            $PSNativeCommandUseErrorActionPreference = $false
        }

        try {
            $ErrorActionPreference = "Continue"
            & $gradleWrapper --stop 2>&1 | Out-Null
        }
        finally {
            $ErrorActionPreference = $cleanupErrorActionPreference
            if ($cleanupNativeCommandPreferenceWasSet) {
                $PSNativeCommandUseErrorActionPreference = $cleanupPreviousNativeCommandPreference
            }
        }

        Remove-DirectoryWithRetry -Path $connectedResultDirectory -Attempts 10 -DelayMilliseconds 1000

        $previousErrorActionPreference = $ErrorActionPreference
        $nativeCommandPreferenceWasSet = Test-Path variable:PSNativeCommandUseErrorActionPreference
        if ($nativeCommandPreferenceWasSet) {
            $previousNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
            $PSNativeCommandUseErrorActionPreference = $false
        }

        try {
            $ErrorActionPreference = "Continue"
            $instrumentationTemporaryLog = "${instrumentationLog}.tmp"
            & $gradleWrapper @gradleArguments 2>&1 | Tee-Object -FilePath $instrumentationTemporaryLog
            $testExitCode = $LASTEXITCODE
            Get-Content -Raw -Path $instrumentationTemporaryLog |
                Set-Content -Encoding utf8 -Path $instrumentationLog
            Remove-Item -Force -Path $instrumentationTemporaryLog
        }
        finally {
            $ErrorActionPreference = $previousErrorActionPreference
            if ($nativeCommandPreferenceWasSet) {
                $PSNativeCommandUseErrorActionPreference = $previousNativeCommandPreference
            }
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    $env:ANDROID_SERIAL = $previousAndroidSerial

    Invoke-AdbDiagnostic -Arguments @("logcat", "-d", "-v", "threadtime") -OutputPath $logcatFull
    Invoke-AdbDiagnostic -Arguments @("logcat", "-b", "crash", "-d", "-v", "threadtime") -OutputPath $logcatCrash
    Invoke-AdbDiagnostic -Arguments @("logcat", "-b", "events", "-d", "-v", "threadtime") -OutputPath $logcatEvents
    Invoke-AdbDiagnostic -Arguments @("shell", "dumpsys", "activity", "processes") -OutputPath $activityProcesses
    Invoke-AdbDiagnostic -Arguments @("shell", "dumpsys", "activity", "services") -OutputPath $activityServices
    Invoke-AdbDiagnostic -Arguments @("shell", "dumpsys", "dropbox", "--print") -OutputPath $dropboxInfo
    Get-Content -Path $dropboxInfo |
        Select-String -Pattern "Process: app\.mobiling\.client|Package: app\.mobiling\.client" -Context 8,24 |
        ForEach-Object { $_.ToString() } |
        Set-Content -Encoding utf8 -Path $dropboxRelevantInfo

    & adb -s $DeviceSerial shell test -r /data/tombstones 2>$null
    if ($LASTEXITCODE -eq 0) {
        Invoke-AdbDiagnostic -Arguments @("shell", "ls", "-la", "/data/tombstones") -OutputPath $tombstoneInfo
    }
    else {
        "Tombstone listing is unavailable without readable /data/tombstones access." |
            Set-Content -Encoding utf8 -Path $tombstoneInfo
    }
    Invoke-AdbDiagnostic -Arguments @("shell", "dumpsys", "window") -OutputPath $windowInfo

    try {
        & adb -s $DeviceSerial shell uiautomator dump /sdcard/window_dump.xml | Out-Null
        & adb -s $DeviceSerial pull /sdcard/window_dump.xml $hierarchy | Out-Null
    }
    catch {
        "UI hierarchy capture failed: $($_.Exception.Message)" | Out-File -Encoding utf8 $hierarchy
    }

    try {
        & adb -s $DeviceSerial exec-out screencap -p > $screenshot
    }
    catch {
        "Screenshot capture failed: $($_.Exception.Message)" | Out-File -Encoding utf8 (Join-Path $outputDirectory "screenshot-error.txt")
    }
}

if ($testExitCode -ne 0) {
    throw "Android access tests failed with exit code $testExitCode. Artifacts: $outputDirectory"
}

Write-Host "Android access tests passed. Artifacts: $outputDirectory"
