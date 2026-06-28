# Mobile API Contract

Status: active baseline.

This document fixes the current Mobiling API direction before the route surface grows. The goal is enterprise-grade stability: mobile clients must be able to depend on route names, payload names, ownership, and migration rules without hidden renames between releases.

## Public route convention

Current mobile-edge public routes stay as they are today:

```text
/access/...
/navigation/mobile/shell
/vendor/...
```

The source location `mobile-edge/src/routes/mobile/**` marks the route as mobile-facing. It does not require a `/mobile` URL prefix. Renaming the public surface would be a contract migration, not a cleanup.

## Authority boundary

```text
Native Android/iOS client
  -> mobile-edge contract route
  -> mobile-edge BFF normalization
  -> Symfony component-owned API
```

mobile-edge owns:

- session/header forwarding for mobile transport;
- screen-ready response shape;
- upstream response normalization;
- mobile-specific fixtures and contract checks;
- defensive error normalization.

mobile-edge must not own:

- core business rules;
- CRUD ownership;
- domain state transitions;
- direct database access;
- hidden mock-only screens.

## Contract files

```text
mobile-edge/openapi/openapi.yaml
mobile-edge/contract/mobile-route-contract.json
mobile-edge/src/contract/mobile/<component>/**
mobile-edge/tools/mobile-contract-guard.mjs
```

Every mobile route must be represented in all required layers:

| Layer | Requirement |
|---|---|
| Fastify source | Route exists under `src/routes/mobile/**` |
| Manifest | Route exists in `contract/mobile-route-contract.json` |
| OpenAPI | Route exists in `openapi/openapi.yaml` |
| Schema | Route references a named response schema |
| Fixture | Screen route has a stable fixture payload |
| Owner | Route declares the upstream component that owns business facts |
| Contract source | Screen route declares `contractFile` for fixture validation |

## Current mobile route surface

| Method | Path | Owner | Class | Screen |
|---|---|---|---|---|
| POST | `/access/signin` | Accessing | transport | - |
| POST | `/access/register` | Accessing | transport | - |
| POST | `/access/logout` | Accessing | transport | - |
| GET | `/access/session` | Accessing | transport | - |
| POST | `/access/verification/resend` | Accessing | transport | - |
| POST | `/access/verification/confirm` | Accessing | transport | - |
| POST | `/access/second-factor/challenge` | Accessing | transport | - |
| POST | `/access/second-factor/verify` | Accessing | transport | - |
| POST | `/access/recovery/request` | Accessing | transport | - |
| POST | `/access/recovery/reset` | Accessing | transport | - |
| GET | `/navigation/mobile/shell` | Navigating | screen | `mobile.shell` |
| GET | `/vendor/profile/:vendorId` | Vendoring | screen | `vendor.profile` |
| GET | `/vendor/summary/:vendorId` | Vendoring | screen | `vendor.summary` |
| GET | `/vendor/statement/:vendorId` | Vendoring | screen | `vendor.statement` |
| GET | `/vendor/payout/:vendorId` | Vendoring | screen | `vendor.payout` |
| GET | `/vendor/transaction/:vendorId` | Vendoring | screen | `vendor.transaction` |

## Migration rule

No route is renamed or removed silently. Any public route change requires:

1. new route added first;
2. OpenAPI updated;
3. manifest updated;
4. fixture added or migrated;
5. compatibility note in `docs/milestone/**`;
6. old route retained until a deliberate deprecation window is documented.

## New route checklist

Before adding a new mobile route:

1. choose upstream owner: Accessing, Navigating, Vendoring, or another Symfony component;
2. add a named response schema;
3. add the OpenAPI path;
4. add the manifest row;
5. add a fixture if it is a screen route;
6. run `npm --prefix mobile-edge run quality:gate`.
