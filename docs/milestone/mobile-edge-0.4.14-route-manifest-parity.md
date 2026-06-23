# Mobile Edge 0.4.14 Route Manifest Parity Milestone

- Purpose: align route module export names with route manifest exports.
- Fixed taxation route naming mismatch used by commerce/order manifests.
- `taxSummaryRoute` now exists in `routes/taxation/summary.ts`.
- `taxDetailRoute` now exists in `routes/taxation/detail.ts`.
- Active Fastify `app.ts` route registration remains unchanged.
- Not included: runtime route changes, taxation business logic changes, or OpenAPI changes.