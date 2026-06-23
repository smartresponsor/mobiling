# Mobile Edge 0.4.13 Neutralize Express Wording Milestone

- Purpose: remove stale Express wording from route-layer comments.
- `mobile-edge/src/routes` no longer imports Express or creates Express routers.
- Route manifest comments now refer to legacy route islands without naming an inactive framework.
- Active Fastify `app.ts` route registration remains unchanged.
- Not included: runtime changes, route registration changes, or business logic changes.