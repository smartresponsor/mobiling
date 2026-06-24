# Mobile Edge 0.4.18 Access Verification and Second-Factor Proxy Routes

- Purpose: complete mobile-edge proxy routes for Accessing-owned verification and second-factor flows.
- Added Accessing API client methods for verification resend and confirm.
- Added Accessing API client methods for second-factor challenge and verify.
- Added request schemas for verification confirm and second-factor verify.
- Added `/access/verification/resend`.
- Added `/access/verification/confirm`.
- Added `/access/second-factor/challenge`.
- Added `/access/second-factor/verify`.
- Mobile-edge forwards session transport and normalizes Accessing session/error payloads.
- Not included: verification code generation, second-factor generation, delivery, validation, recovery/reset, or Accessing business rules.