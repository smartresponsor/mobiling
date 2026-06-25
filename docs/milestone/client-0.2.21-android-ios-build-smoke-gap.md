# Client 0.2.21 Android/iOS Build Smoke Gap

- Purpose: record client-side build-smoke readiness after recovery/reset UI wiring.
- Workspace: `D:\PhpstormProjects\www\Mobiling`.
- Baseline status contained only `.codebase-memory/` and `AGENTS.md`.

## Android project evidence

- `client/android/settings.gradle.kts` includes `:app`.
- `client/android/build.gradle.kts` configures the Android Gradle plugin.
- `client/android/app/build.gradle.kts` configures the application module.
- Recovery request/reset UI files are under the active Android application source tree.
- Recovery request/reset routes are wired through `MobilingAppShell`.

## iOS project evidence

- `client/ios/project.yml` includes `App/Access` in the `MobileClient` target sources.
- `RecoveryRequestView.swift` and `RecoveryResetView.swift` are under `client/ios/App/Access`.
- Recovery request/reset routes are wired through `MobilingAppShell.swift`.
- The iOS application target depends on `MobileClient`.

## Build-smoke gap

- Android native build was not executed in this pass.
- iOS native build was not executed in this pass.
- Next native evidence should run Android Gradle and macOS `xcodebuild` checks.
