# Mobile Edge 0.4.10 Project Route Handler Alignment Milestone

- Purpose: align project route modules with the route handler context pattern.
- Replaced legacy `Router()` exports with handler function exports.
- `projectListingRoute` now replaces `projectListingRouter`.
- `projectDetailRoute` now replaces `projectDetailRouter`.
- Active Fastify `app.ts` route registration remains unchanged.
- Not included: project route deletion, Fastify rewrite, persistence changes, or project business logic changes.