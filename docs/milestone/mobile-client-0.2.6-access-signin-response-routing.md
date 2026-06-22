# Mobile Client 0.2.6 Access Signin Response Routing Milestone

- Purpose: connect signin response payloads to passive access state routing.
- Boundary decision: Mobiling still does not implement authentication business logic.
- Android `MobilingAppShell` now accepts an optional `AuthFeatureBridge`.
- Android `SignInScreen` now builds `StartAuthRequest` and routes returned `AuthSessionPayload` through `toAccessScreen()`.
- iOS `MobilingAppShell` now accepts an optional `AuthFeatureBridge`.
- iOS `SignInView` now builds `StartAuthRequest` and routes returned `AuthSessionPayload` through `toAccessScreen()`.
- If no bridge is supplied, both platforms keep the existing unavailable-state behavior.
- Routing behavior remains backend-owned:
  - `requiresVerification` routes to verification-required UI.
  - `requiresSecondFactor` routes to second-factor-required UI.
  - unauthenticated state routes to signin UI.
- Temporary authenticated route remains welcome until authenticated workspace shell is materialized.
- Register flow was not changed because mobile-client does not yet expose a register gateway contract.
- Not included: password validation, registration policy, verification code validation, second-factor validation, token generation, or session authority.