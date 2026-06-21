# iOS 0.2.0 Access Entry Materialized Milestone

- Purpose: replace the technical baseline screen with a guest-facing SwiftUI access entry surface for SmartResponsor Mobile.
- Visible SwiftUI guest-facing views: `MobilingAppShell`, `AccessWelcomeView`, `SignInView`, `RegisterAccessView`.
- Android parity: mirrors `0.2.0-access-entry-materialized` behavior by using a local guest entry shell, no fake success, and the same unavailable submit message.
- Limitation: no real backend auth, session, token, or mobile-edge integration is connected yet.
- Verification: local iOS compile verification was unavailable on this Windows environment because Swift/Xcode tooling is not installed; source-level verification was performed only.
- Next recommended milestone: `0.3.0-mobile-edge-auth-contract`.
