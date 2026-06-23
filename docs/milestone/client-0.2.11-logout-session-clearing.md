# Client 0.2.11 Logout Session Clearing Milestone

- Purpose: clear Accessing-owned access session from Android and iOS client shells.
- Added Android `LogoutAuthUseCase`.
- Added Android `logoutAuth` gateway method.
- Added Android `AuthFeatureBridge.logout`.
- Android verification and second-factor use-different-access actions now call logout and return to guest welcome.
- Added iOS `LogoutAuthUseCase`.
- Added iOS `logoutAuth` gateway method.
- Added iOS `AuthFeatureBridge.logout`.
- iOS verification and second-factor use-different-access actions now call logout and return to guest welcome.
- Not included: verification code submission, second-factor submission, recovery/reset, or network implementation changes.