# Mobile Edge 0.4.4 Access Session State Contract Milestone

- Purpose: align mobile-edge access session payload with client Android/iOS session-state contracts.
- Boundary decision: Accessing owns authentication and session transport.
- Removed token-owned response fields from mobile-edge access session payload:
  - `accessToken`
  - `refreshToken`
  - `expiresAt`
- Mobile-edge access session response now exposes passive session state only:
  - `status`
  - nullable `identity`
  - `requiresVerification`
  - `requiresSecondFactor`
- Cookie/session transport remains forwarded from Accessing via `set-cookie` passthrough.
- Normalizer no longer derives or emits local mobile token fields.
- OpenAPI schema now matches client access session payload shape.
- Not included: token generation, password validation, verification code validation, second-factor validation, or session authority.
