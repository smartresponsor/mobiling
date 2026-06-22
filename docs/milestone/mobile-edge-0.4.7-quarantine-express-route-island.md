# Mobile Edge 0.4.7 Quarantine Express Route Island Milestone

- Purpose: stop manifest exports from exposing legacy Express route islands in a Fastify runtime.
- Active Fastify `app.ts` route registration remains unchanged.
- `/mobile/access/*` session transport remains untouched.
- Removed account/profile/project manifest exports for legacy Express routers.
- Removed vendor manifest re-exports that pointed at legacy project/profile Express routers.
- Legacy files remain in place because connector policy does not allow file deletion.
- Not included: route deletion, Fastify route rewrite, authentication logic, or session authority changes.