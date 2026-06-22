# Mobile Edge 0.4.6 Retire Legacy Auth Route Milestone

- Purpose: quarantine the unregistered legacy Express-style auth session route.
- Boundary decision: mobile auth is served through Accessing-owned `/mobile/access/*` session transport.
- Removed legacy auth-session route exports from auth/account manifests.
- Legacy `routes/auth/session.ts` now returns a retired `410` response if imported and mounted elsewhere.
- Legacy `startAuth` no longer returns any session payload and points callers to `/mobile/access/*`.
- Legacy auth/session contracts are marked as retired compatibility contracts.
- Active Fastify runtime registration remains unchanged and still uses `routeMobileAccess`.
- Not included: Accessing authentication logic, token generation, password validation, verification code validation, or second-factor validation.