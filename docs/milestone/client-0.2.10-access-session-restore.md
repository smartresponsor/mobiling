# Client 0.2.10 Access Session Restore Milestone

- Purpose: restore access session state on Android and iOS app shell startup.
- Added Android `RestoreAuthUseCase`.
- Added Android `restoreAuth` gateway method.
- Added Android `AuthFeatureBridge.restore`.
- Android app shell now restores session and routes by `AuthSessionPayload.toAccessScreen`.
- Added iOS `RestoreAuthUseCase`.
- Added iOS `restoreAuth` gateway method.
- Added iOS `AuthFeatureBridge.restore`.
- iOS app shell now restores session and routes by `AuthSessionPayload.toAccessScreen`.
- Not included: logout wiring, verification submission, second-factor submission, recovery/reset, or network implementation changes.