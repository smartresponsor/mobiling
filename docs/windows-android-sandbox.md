# Windows Android sandbox

Windows remains a first-class supported Mobiling development/runtime sandbox alongside
[Ubuntu/Linux](ubuntu-android-sandbox.md). Neither platform replaces or deprecates the other.
Use an x64 Windows host with Java 17 and supported emulator acceleration. The canonical Android project, SDK/API requirements and emulator host-gateway semantics are
shared. Use Windows PowerShell and Windows SDK executables on Windows; Bash/KVM instructions
apply to Linux only.

The current branch retains `client/android/gradlew.bat`, `materialize-mobiling.ps1` and the
PowerShell-based `mobile-edge/tools/mobile-edge-restart-local.mjs` launcher. Specialized
`tool/android-*.ps1` helpers from earlier revisions are absent from this branch; their older
flavor/package assumptions are not the current client contract. The native operational
commands below cover the current single debug variant. The existing
[native build command pack](milestone/client-0.2.22-native-build-command-pack.md) remains valid
when its example workspace is replaced with your actual clone path.

From the repository root in Windows PowerShell, collect the active Windows environment:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\android\snapshot.ps1
```

Read `.codebase-memory/environments/windows/environment-snapshot.json` to verify tool paths,
Java 17, SDK, adb and emulator acceleration. Detect Windows before using this snapshot;
refresh only the Windows snapshot when missing, inconsistent or older than seven days.
The Linux snapshot is independent and is not overwritten.

Use the Windows Android SDK command-line tools on PATH (`ANDROID_HOME` and
`ANDROID_SDK_ROOT` must identify the same SDK). Required components and AVD:

```powershell
sdkmanager --licenses
sdkmanager 'platform-tools' 'emulator' 'platforms;android-34' 'build-tools;34.0.0' 'system-images;android-34;google_apis;x86_64'
emulator -accel-check
emulator -list-avds
# Create only if the named AVD is absent; otherwise reuse its API 34 image/userdata.
'no' | avdmanager create avd --name Mobiling_Light_API_34 --package 'system-images;android-34;google_apis;x86_64' --device pixel_2
```

Use the host's supported Windows acceleration backend, not `/dev/kvm`. Do not start a second
instance of the same AVD. In a separate PowerShell session, start it (headless is optional):

```powershell
emulator -avd Mobiling_Light_API_34 -port 5554 -no-window -no-audio -no-snapshot
```

In the working session, verify boot, build, install and launch:

```powershell
$serial = 'emulator-5554' # confirm with adb devices and adb -s $serial emu avd name
adb devices -l
adb -s $serial wait-for-device
$deadline = (Get-Date).AddMinutes(5)
while ((adb -s $serial shell getprop sys.boot_completed).Trim() -ne '1') {
    if ((Get-Date) -gt $deadline) { throw 'Android boot timed out.' }
    Start-Sleep -Seconds 2
}
adb -s $serial shell pm path android
Push-Location .\client\android
try {
    .\gradlew.bat :app:assembleDebug --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'Android build failed.' }
} finally { Pop-Location }
$metadataPath = '.\client\android\app\build\outputs\apk\debug\output-metadata.json'
$metadata = Get-Content -LiteralPath $metadataPath -Raw | ConvertFrom-Json
$apk = Join-Path (Split-Path $metadataPath) $metadata.elements[0].outputFile
$package = $metadata.applicationId
adb -s $serial install -r $apk
if ($LASTEXITCODE -ne 0) { throw 'APK install failed.' }
# Optional: adb -s $serial shell pm clear $package
# Optional: set window_animation_scale, transition_animation_scale and animator_duration_scale to 0.
$activity = (adb -s $serial shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER $package | Select-Object -Last 1).Trim()
adb -s $serial shell am start -W -n $activity
adb -s $serial shell pidof $package
adb -s $serial shell dumpsys activity activities | Select-String 'mResumedActivity|topResumedActivity'
```

Default debug and release builds keep `http://10.0.2.2:8080`. That alias is the Android
emulator's host gateway, not a machine-specific IP. `127.0.0.1` inside Android is the device.
If a development host needs another Edge port, set `PORT` for the development Edge process and
pass `-PmobilingDevEdgeUrl=http://10.0.2.2:<port>` to the **debug** Gradle build. This property
does not alter release behavior. Configure actual upstream APIs independently.

In another PowerShell window, run `npm --prefix mobile-edge run build`, check its exit code,
then `npm --prefix mobile-edge start`. The foreground process reports port conflicts without
stopping existing listeners. In the working session:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/health
# Emulator gateway health: send HTTP through Android's toybox nc.
"GET /health HTTP/1.0`r`nHost: 10.0.2.2`r`n`r`n" | adb -s $serial shell 'toybox nc -w 5 10.0.2.2 8080'
```

To verify responsiveness, open Sign in, Recover access, return through Sign in to welcome,
open Create access and return to welcome. Upstream HTTP 503 responses with unconfigured
Accessing/Navigating/Cataloging URLs are a separate upstream configuration task, not an
Android sandbox failure. Authenticated business verification requires actual upstreams and
an existing test account.

Capture into the same visual contract as Ubuntu. Pull PNG files rather than redirecting
binary adb output through Windows PowerShell 5.1:

```powershell
$evidence = '.\watchdog\runtime\device'
New-Item -ItemType Directory -Force "$evidence\history" | Out-Null
adb -s $serial shell uiautomator dump /sdcard/mobiling-window.xml
adb -s $serial pull /sdcard/mobiling-window.xml "$evidence\current-ui.xml"
adb -s $serial shell screencap -p /sdcard/mobiling-screen.png
adb -s $serial pull /sdcard/mobiling-screen.png "$evidence\latest.png"
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
Copy-Item "$evidence\latest.png" "$evidence\history\$stamp-capture.png"
Copy-Item "$evidence\current-ui.xml" "$evidence\history\$stamp-capture.xml"
adb -s $serial shell dumpsys window | Out-File "$evidence\current-window.txt" -Encoding utf8
adb -s $serial emu kill
```

Generated evidence, APKs, SDK state, `local.properties`, credentials and platform snapshots
are local and Git-ignored. Check native command exit codes when diagnosing failures. Reuse
healthy AVDs; stop the named emulator before recovering stale locks. Never delete userdata
or stop unrelated host processes as routine recovery. This document is a Windows workflow;
Ubuntu-only validation does not establish Windows runtime verification.
