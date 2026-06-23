# Mobile Edge 0.4.17 Remove Mobile HTTP Prefix Milestone

- Purpose: remove redundant `/mobile` prefix from Mobiling HTTP public API.
- Access routes moved from `/mobile/access/*` to `/access/*`.
- Config route moved from `/mobile/config` to `/config`.
- Entitlement route moved from `/mobile/entitlement` to `/entitlement`.
- Push route moved from `/mobile/push/register` to `/push/register`.
- Receipt route moved from `/mobile/receipt/verify` to `/receipt/verify`.
- Analytic routes moved from `/mobile/analytic/*` to `/analytic/*`.
- Sync route moved from `/mobile/sync/event` to `/sync/event`.
- Internal source organization under `routes/mobile` and `contract/mobile` is unchanged.
- Not included: auth logic changes, Accessing behavior changes, or mobile-client UI changes.