# Mobile Quality Gate

Status: active baseline.

The mobile track must stay contract-first and enterprise-oriented. The quality gate is intentionally small now, but it blocks the highest-risk drift while the route surface is still compact.

## Command

```powershell
npm --prefix .\mobile-edge run quality:gate
```

Expanded form:

```powershell
npm --prefix .\mobile-edge run typecheck; npm --prefix .\mobile-edge run build; npm --prefix .\mobile-edge test
```

## Gate layers

| Layer | Command | Purpose |
|---|---|---|
| TypeScript typecheck | `npm run typecheck` | Prevent type drift in source contracts and routes |
| Build | `npm run build` | Ensure runtime JS is generated from the current TypeScript surface |
| Mobile contract guard | `npm test` | Block undocumented routes, missing OpenAPI paths, missing fixtures, and BFF boundary violations |

## Red flags blocked by guard

The guard in `mobile-edge/tools/mobile-contract-guard.mjs` currently blocks:

- mobile route implemented but not declared in `contract/mobile-route-contract.json`;
- manifest route missing from OpenAPI;
- manifest route not pointing to its named response schema;
- manifest response schema names that are absent from OpenAPI `components.schemas`;
- screen route without fixture;
- mobile route without upstream owner;
- `PUT`, `PATCH`, or `DELETE` in the current mobile surface;
- CRUD-like URL fragments in mobile routes: `/crud`, `/create`, `/update`, `/delete`, `/edit`, `/remove`;
- retired `/mobile/session` transport;
- direct database access imports from mobile route files.
- inline mobile response payload schemas inside route files.
- flat vendor contract imports from mobile routes instead of `src/contract/mobile/vendor/**`.
- flat access and navigation contract imports from mobile routes instead of component-scoped contract folders.
- screen fixture payloads that do not satisfy top-level source schema fields.

## Red flags that remain policy-level for now

These are documented as policy and should become automated checks when the surface grows:

- fixture schema validation against OpenAPI;
- snapshot diff approval for screen fixtures;
- route-level smoke tests against a running local edge server;
- upstream component contract checks against Symfony app endpoints;
- mobile client generated API client checks;
- Android/iOS compile gates.

## Architecture decision

The accepted direction is not a loose MVP. The baseline is:

```text
contract-first mobile route
  -> stable response schema
  -> screen fixture
  -> BFF normalization only
  -> upstream Symfony component authority
```

Performance discipline for native Android/iOS remains outside this immediate gate. It should be introduced after the API contract and screen fixtures stop drifting.

## Adding a route safely

1. Add the source route under `mobile-edge/src/routes/mobile/**`.
2. Add or reuse a named contract schema.
3. Add the OpenAPI path.
4. Add the route to `mobile-edge/contract/mobile-route-contract.json`.
5. Add a fixture under `mobile-edge/fixture/mobile/**` when it is a screen route.
6. Run the quality gate.

## Release expectation

No mobile release candidate should be cut while this command is red:

```powershell
npm --prefix .\mobile-edge run quality:gate
```
