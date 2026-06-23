# iOS 0.2.1 Access Entry XcodeGen Aligned Milestone

- Purpose: align the XcodeGen source graph with the Swift Package source graph for the iOS access entry shell.
- Changed surface: `client/ios/project.yml` now includes `App/Access` in the `MobileClient` target sources.
- Reason: `ContentView` imports `MobileClient` and renders `MobilingAppShell`, which is declared under `App/Access`.
- Existing Swift Package parity: `client/ios/Package.swift` already includes `App/Access` in the `MobileClient` target sources.
- Runtime status: no iOS Simulator runtime smoke is claimed in this milestone.
- Environment note: iOS runtime verification still requires macOS with Xcode and an available iOS Simulator device.
- Grammar note: Access mobile/backend routes use `signin` spelling.
- Verification level:
  - source graph alignment reviewed
  - XcodeGen project generation not executed on this Windows workstation
  - iOS build/runtime smoke deferred to MacBook/Xcode
- Next recommended milestone:
  - `ios-0.2.2-access-entry-runtime-smoke`
