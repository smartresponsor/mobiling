# Client 0.2.22 Native Build Command Pack

- Purpose: define repeatable native build-smoke commands for the active `client/` tree.
- Workspace: `D:\PhpstormProjects\www\Mobiling`.
- Active Android path: `client/android`.
- Active iOS path: `client/ios`.
- Scope: command pack only; no feature code changes.

## Android smoke command pack

Run from Windows PowerShell:

```powershell
cd D:\PhpstormProjects\www\Mobiling\client\android

.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace
```

Optional stronger check:

```powershell
cd D:\PhpstormProjects\www\Mobiling\client\android

.\gradlew.bat check --no-daemon --stacktrace
```

Expected artifact boundary:

- Android build output under `client/android/**/build/` is generated output.
- Do not commit generated build output.

## iOS smoke command pack

Run from macOS shell with Xcode and XcodeGen installed:

```bash
cd /path/to/Mobiling/client/ios

xcodegen generate
xcodebuild -project Mobiling.xcodeproj -scheme Mobiling -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

Expected artifact boundary:

