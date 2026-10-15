# OneTasker Widget architecture

## Product identity

`OneTasker Widget` is the engineering identity for a separately shippable lightweight OneTasker application composition inside the existing `Mobiling` repository.

- technical product key: `one_tasker_widget`
- public brand: `OneTasker`
- repository: `Mobiling`
- backend host: existing Symfony Host App
- mobile boundary: existing `mobile-edge`

`Widget` is an engineering/product-shape term. The public store name may remain `OneTasker` and is not an architecture concern.

## Core decision

The Widget is not a hidden-screen mode of the full application and is not a separate repository. It is an independent native application composition that reuses shared capabilities without depending on the full OneTasker composition.

Target dependency direction:

```text
shared mobile capabilities
        ^
        |
  +-----+----------------+
  |                      |
full OneTasker      OneTasker Widget
```

Shared capability candidates include authentication, identity, configuration, networking/session, security, push, analytics, selected UI primitives, and cross-platform contracts. The milestone must extract or reuse these capabilities rather than copy source files into a second implementation tree.

## Proposed repository shape

The exact module names may be refined during implementation, but ownership should converge toward this shape:

```text
Mobiling/
  client/
    contract/
    android/
      app/                  # existing full application
      one-tasker-widget/    # lightweight application composition
      ...shared modules...
    ios/
      App/                  # existing full composition
      OneTaskerWidget/      # lightweight target/composition
      ...shared frameworks/modules...
  mobile-edge/
```

Do not create a parallel mobile repository. Do not duplicate shared source merely to obtain a small application. Dependency isolation must be enforced by build composition.

## User experience boundary

The first product surface is intentionally small and is now materialized as explicit lightweight native state on both platforms:

```text
Listen
Needs
Profile / Settings
```

A successful recognized-need capture transitions directly from `Listen` to `Needs`, where only canonical `need.recognized` / `one_tasker.need` inbox entries are rendered. Each actionable need card projects the already-persisted structured payload (`category`, `confidence`, and `locale`) from Notifying through mobile-edge; no parallel mobile persistence or second need model is introduced. `Profile / Settings` remains intentionally minimal and exposes only true mobile/account controls rather than importing the full-app profile graph. The current native surface includes authenticated session restore, minimal email/password sign-in, gateway-backed sign-out, the active device locale used by speech/classification and persisted recognized-need metadata, and an OS-owned notification-settings link. Credentials live only in transient UI state and the password is cleared after each sign-in attempt. If Accessing requires account verification or a second factor, the Widget reports that state instead of pretending authentication succeeded; broader registration/recovery/verification UI remains outside the lightweight V1 composition. Heavyweight informational surfaces such as Privacy Policy, Terms of Service, About, Help/Support, and similar policy/content pages are web-owned: the Widget exposes configured external links and does not duplicate those pages natively. Current canonical Paging-backed destinations are `/page/privacy-policy`, `/page/terms-of-service`, and `/page/about` on the existing SmartResponsor web host; additional destinations follow the same configurable-link pattern once their canonical public route exists.

The primary loop is:

```text
Speak
  -> speech-to-text
  -> need classification
  -> actionable notification
  -> Call me
  -> human qualification
  -> existing Ordering lifecycle
```

There is no contractor marketplace, bidding flow, lead resale, or second CRM in this product.

## Need representation

V1 does not introduce a dedicated backend Need aggregate. A recognized need is represented by `Notifying` as a specialized actionable notification.

Recommended notification semantics:

```text
sourceComponent = one_tasker_ai
eventName       = need.recognized
topic           = one_tasker.need
title           = short human summary
body            = concise recognized context
actionUrl       = widget/deep-link action for Call me
payload         = structured need metadata
```

Recommended payload fields are deliberately narrow:

```text
type
category
confidence
source
locale
classifierVersion
```

Raw audio is not a business object and must not be stored in `Notifying`.

## Component boundaries

`Notifying` owns the recognized-needs inbox, recipient state, read/ack/archive behavior, action metadata, and notification semantics.

`Delivering` remains a second-level transport capability for push/SMS/provider delivery. It is not the Need store.

`Messaging` remains a conversation/thread capability. It may be integrated later if a recognized need evolves into a real conversation, but V1 does not model needs as chat messages.

`Ordering` begins after human qualification. A recognition such as "we should fix the faucet" is not an Order and must not weaken Order invariants or bypass required commercial fields.

## Call me boundary

The `Call me` action is the explicit transition from passive recognition to operational intent. V1 expresses this as an idempotent action/event correlated to the originating notification instead of introducing a separate `CallbackRequest` aggregate.

The materialized V1 transition is:

```text
customer need.recognized inbox entry
  -> ownership validation
  -> need.callback_requested
  -> system recipient one_tasker.operations
  -> inbox_only operational handoff
  -> acknowledge originating customer need
```

The callback event correlation key is derived from the recognized-need notification id, so repeated `Call me` submissions converge on the same operational intent rather than creating duplicate callback work.

If callback handling later acquires independent lifecycle requirements such as assignment, SLA, scheduling, retry, escalation, and outcome tracking, a dedicated callback aggregate can be extracted then.

## Human qualification handoff

V1 does not create a second CRM or convert a recognized need directly into an Order. The existing `Relating` component owns lead capture and qualification.

The operational handoff is:

```text
need.callback_requested
  -> system:one_tasker.operations inbox
  -> human/operator accepts the callback work
  -> Relating /relating/lead/capture
  -> Relating /relating/lead/qualify
  -> qualified commercial intent
  -> existing Ordering lifecycle when order invariants are satisfied
```

The lead-capture mapping should remain narrow:

```text
source_code = one_tasker_widget
payload.recognized_need_notification_id
payload.recognized_need_recipient_entry_id
payload.callback_notification_id
payload.requester_recipient_key
payload.category
payload.summary
```

`Relating` already accepts arbitrary business payload on `CaptureLeadCommand`, so V1 requires no new Lead entity fields for this source. Lead scoring/temperature remain human qualification inputs to the existing `QualifyLeadCommand`; they must not be guessed by Notifying or the Widget.

## Privacy invariants

1. Raw microphone audio is transient and is not retained by OneTasker after recognition.
2. Unrelated speech is discarded.
3. The application stores or submits only the minimum recognized need information required for the product flow.
4. Analytics must not contain raw audio or unrestricted transcripts.
5. Cloud speech or cloud semantic processing must be disclosed when used.
6. On-device processing should be preferred whenever available and sufficiently accurate.

## Non-goals for V1

- continuous hidden ambient listening
- bundled heavyweight speech models
- permanent voice recording storage
- cloud household-memory archive
- automatic Need-to-Order conversion
