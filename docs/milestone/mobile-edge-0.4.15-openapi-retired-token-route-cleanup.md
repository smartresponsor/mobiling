# Mobile Edge 0.4.15 OpenAPI Retired Token Route Cleanup Milestone

- Purpose: align OpenAPI with runtime source after local token routes were retired.
- Removed `/mobile/session` from OpenAPI because runtime source no longer exposes it.
- Removed `/oauth/token` from OpenAPI because runtime source no longer exposes it.
- `/mobile/access/*` remains the documented mobile access/session transport surface.
- Active Fastify `app.ts` route registration remains unchanged.
- Not included: runtime route changes, token generation, authentication logic, or Accessing behavior changes.