# Mobile Client 0.2.8 Register Response Routing Milestone

- Purpose: route registration responses on Android and iOS through the same access session decision path as sign in.
- Added register auth request contracts for Android and iOS.
- Added register auth use cases for Android and iOS.
- Added `registerAuth` to Android and iOS auth session gateways.
- Added `register` to Android and iOS auth feature bridges.
- Android `RegisterAccessScreen` now submits registration input and routes returned `AuthSessionPayload`.
- iOS `RegisterAccessView` now submits registration input and routes returned `AuthSessionPayload`.
- Not included: verification submission, second-factor submission, recovery/reset, session restore, or logout wiring.