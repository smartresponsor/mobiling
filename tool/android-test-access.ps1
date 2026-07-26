# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$DeviceSerial,
    [string]$TestClass = "app.mobiling.client.access.AccessBehaviorTest",
    [string]$OutputRoot = "build/test-result"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-AdbDiagnostic {
    param(
        [Parameter(Mandatory)]
        [string[]]$Arguments,
        [Parameter(Mandatory)]
        [string]$OutputPath
    )

    try {
        & adb -s $DeviceSerial @Arguments 2>&1 | Out-File -Encoding utf8 $OutputPath
    }
    catch {
        "Diagnostic command failed: adb -s $DeviceSerial $($Arguments -join ' ')`n$($_.Exception.Message)" |
            Out-File -Encoding utf8 $OutputPath
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
    $devices = @(& adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" })
    if ($devices.Count -ne 1) {
        throw "Specify -DeviceSerial when zero or multiple Android devices are available."
    }

    $DeviceSerial = ($devices[0] -split "\s+")[0]
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
$dropboxInfo = Join-Path $outputDirectory "dropbox.txt"
$tombstoneInfo = Join-Path $outputDirectory "tombstones.txt"
$windowInfo = Join-Path $outputDirectory "window.txt"
$hierarchy = Join-Path $outputDirectory "hierarchy.xml"
$screenshot = Join-Path $outputDirectory "screenshot.png"

$gradleArguments = @(
    ":app:connectedDebugAndroidTest"
    "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"
)

@(
    "timestamp=$(Get-Date -Format o)"
    "device=$DeviceSerial"
    "testClass=$TestClass"
    "workingDirectory=$androidRoot"
    "command=$gradleWrapper $($gradleArguments -join ' ')"
) | Out-File -Encoding utf8 $commandLog

& adb -s $DeviceSerial wait-for-device
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
        $previousErrorActionPreference = $ErrorActionPreference
        $nativeCommandPreferenceWasSet = Test-Path variable:PSNativeCommandUseErrorActionPreference
        if ($nativeCommandPreferenceWasSet) {
            $previousNativeCommandPreference = $PSNativeCommandUseErrorActionPreference
            $PSNativeCommandUseErrorActionPreference = $false
        }

        try {
            $ErrorActionPreference = "Continue"
            & $gradleWrapper @gradleArguments 2>&1 | Tee-Object -FilePath $instrumentationLog
            $testExitCode = $LASTEXITCODE
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
    Invoke-AdbDiagnostic -Arguments @("shell", "ls", "-la", "/data/tombstones") -OutputPath $tombstoneInfo
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
