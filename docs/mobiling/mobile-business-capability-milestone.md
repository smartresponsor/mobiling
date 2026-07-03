# Mobiling mobile business capability milestone

## Scope

This milestone defines how Mobiling turns Android and iOS clients into business-capable mobile applications for SmartResponsor.

The document is intentionally contract-first. Runtime code must follow this contract instead of adding local navigation or per-screen business shortcuts.

## Current repository state observed before this milestone

- Mobiling repository branch: `master`.
- Active mobile clients: `client/android` and `client/ios`.
- Legacy or reference clients: `mobile-client/android` and `mobile-client/ios`.
- Mobile edge baseline: `mobile-edge v5.15` flag canary engine.
- Android active shell: `client/android/app/src/main/java/app/mobiling/client/access/MobilingAppShell.kt` and `client/android/app/src/main/java/app/mobiling/client/dashboard/DashboardMobileShell.kt`.
- iOS active shell: `client/ios/App/Access/MobilingAppShell.swift` and `client/ios/App/Dashboard/MobileDashboardShellView.swift`.

## Related component facts

### Navigating

Navigating owns the published mobile shell. Mobiling must render and execute that publication; Mobiling must not treat fallback shell definitions as product truth.

Observed Navigating source of truth:

- `config/navigation.mobile.yaml`
- `GET /api/navigation/mobile/shell`
- `NavigationMobileShellPayloadProvideService`
- schema: `smartresponsor.navigation.mobile.shell.v1`
- channel: `mobile`

Published mobile locations:

- `mobile.bottom.primary`
- `mobile.account.quick`
- `mobile.more.drawer`
- `mobile.vendor.context`

Navigation item metadata relevant to Mobiling:

- `mobile_route`
- `mobile_platforms`
- `mobile_disabled_label`
- `namespace_provider`
- `domain`
- `resource`
- `operation`

### Cruding

Cruding owns CRUD route execution semantics. Mobile edge must not create per-resource mega-controllers that bypass URI-derived granular entrypoint behavior.

Cruding constraints that affect mobile edge:

- CRUD entrypoints are URI-derived and granular.
- Components own small entrypoint classes.
- Missing entrypoints fail soft with diagnostics.
- Runtime diagnostics must keep `entrypointTrace` visible.
- Operation and method parity must be preserved.
- Status strings must use canonical Cruding constants, not ad-hoc mobile strings.

## Non-goal: snackbar-driven auth

Sign in and sign out must not create a snackbar or toast business branch.

Correct auth lifecycle:

```text
sign in request
  -> auth session payload
  -> access route decision
  -> target screen
```

```text
sign out action
  -> session cleared
  -> access state reset
  -> welcome or sign-in screen
```

Rejected pattern:

```text
sign in success snackbar
sign out success snackbar
snackbar as routing state
snackbar as business confirmation layer
```

Allowed error pattern:

```text
sign in failure
  -> inline form error
  -> field error
  -> blocking auth state
```

## Ownership model

Mobiling follows `client/contract/navigation/ownership.json`.

Canonical ownership summary:

```text
dashboard
  -> auth as entry flow
  -> catalog, message, vendor as primary sections
  -> vendor owns product, order, project, profile
  -> order owns shipment
  -> order embeds taxation
  -> identity remains internal
```

This means product, order, shipment, and taxation must not be promoted to unrelated flat primary tabs unless Navigating explicitly publishes them that way.

## Current active Navigating-backed routes

These routes are currently treated as active shell routes:

- `dashboard`
- `vendor`
- `more`
- `vendor/profile`
- `vendor/summary`
- `vendor/statement`
- `vendor/payout`
- `vendor/transaction`
- `access/sign-out` as action `access.sign_out`

These routes are visible but disabled or coming soon in the current publication:

- `access/password`
- `access/verification`
- `catalog`
- `message`
- `attachment`

## Route and action handling rules

### Active route

If a navigation item has `enabled=true` and a known `route`, Mobiling resolves it through a platform route resolver and displays the matching screen.

### Disabled route

If a navigation item has `enabled=false`, Mobiling renders it as visible but inert. It may show `badge`, `status`, or `disabledReason`; it must not navigate.

### Action item

If a navigation item has `action=access.sign_out`, Mobiling must call the auth logout flow and reset access state. It must not treat the action as a content route.

### Unknown route

Unknown routes are ignored safely or rendered as disabled. They must not crash the app.

## Required route resolver milestone

### Android target

Add a route foundation under:

```text
client/android/app/src/main/java/app/mobiling/client/navigation/
```

Expected files:

- `MobileRoute.kt`
- `MobileLink.kt`
- `MobileRouteResolver.kt`
- `MobileLinkParser.kt`

### iOS target

Add a route foundation under:

```text
client/ios/App/Navigation/
```

Expected files:

- `MobileRoute.swift`
- `MobileLink.swift`
- `MobileRouteResolver.swift`
- `MobileLinkParser.swift`

### Required resolver behavior

The resolver must understand these route families:

- access routes
- dashboard routes
- vendor routes
- catalog routes
- message routes
- attachment routes
- cart routes when published
- vendor-owned product routes when published
- vendor-owned order routes when published
- order-owned shipment routes when published
- order-embedded taxation routes when published

## Canonical route table

### Access

- `access/sign-in`
- `access/register`
- `access/recovery/request`
- `access/recovery/reset`
- `access/verification`
- `access/password`
- `access/sign-out`

`access/sign-out` is an action route and must be handled by the auth lifecycle.

### Dashboard

- `dashboard`
- `more`

### Vendor

- `vendor`
- `vendor/profile`
- `vendor/summary`
- `vendor/statement`
- `vendor/payout`
- `vendor/transaction`
- `vendor/attachment`

### Catalog

- `catalog`
- `catalog/browse`
- `catalog/node/{catalogNodeId}`
- `catalog/search`

### Message

- `message`
- `message/thread/{threadId}`

### Attachment

- `attachment`
- `attachment/{attachmentId}`

### Cart

- `cart`
- `cart/checkout`
