# Mobile Edge 0.4.5 Retire Legacy Auth Token Stub Milestone

- Purpose: remove the remaining legacy local token stub from mobile-edge auth/session code.
- Boundary decision: Accessing owns authentication and session transport.
- Legacy `startAuth` no longer emits synthetic token values.
- Removed legacy stub output fields: `accessToken`, `refreshToken`, and `expiresAt`.
- Legacy auth/session payload now returns passive unauthenticated session state.
- Fastify `app.ts` already uses `/mobile/access/*` for mobile access and does not register this Express-style legacy route.
- Not included: token generation, password validation, verification code validation, second-factor validation, or session authority.
