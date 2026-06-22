# Mobile Edge 0.4.3 Retire Local Session Route Milestone

- Purpose: remove the second mobile authentication authority from runtime behavior.
- Decision: Accessing-owned `/mobile/access/*` session transport is the canonical mobile authentication path.
- Retired behavior:
  - local `/mobile/session` token minting
  - local `/mobile/session/refresh` token refresh
  - hardcoded development identity token flow
- Implementation:
  - `mobile-edge/src/routes/session.ts` remains as a compatibility module
  - the module no longer registers local token routes
  - the module no longer imports local JWT signing or verification helpers
- Remaining cleanup:
  - mark or remove legacy `/mobile/session` OpenAPI entries when connector patching allows it
- Verification target: `npm run typecheck` and `npm run build` in `mobile-edge`.
