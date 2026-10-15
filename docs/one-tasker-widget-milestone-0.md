# OneTasker Widget Milestone 0

## Goal

Materialize the lightweight product boundary and contracts without turning the milestone into a full feature implementation.

## Decisions locked by this milestone

- product engineering identity: `OneTasker Widget`
- technical product key: `one_tasker_widget`
- repository remains `Mobiling`
- Android and iOS receive independent lightweight composition roots/targets
- shared capabilities are reused through dependency extraction, not source duplication
- V1 capture is user-initiated push-to-talk
- native/system speech-to-text is preferred
- heavyweight bundled speech models are excluded from V1
- raw voice recordings are not durably stored
- semantic need classification is separate from transcription
- `Notifying` is the V1 recognized-needs inbox
- `Delivering` and `Messaging` are optional second-level integrations, not V1 persistence owners
- `Ordering` starts only after human qualification

## Repository work packages

### Mobiling

1. Map the current full application dependency graph.
2. Identify the minimum reusable set for auth, identity, config, HTTP/session, security, push, analytics, and UI primitives.
3. Define the Android `one-tasker-widget` application module/composition root.
4. Define the iOS `OneTaskerWidget` application target/composition root.
5. Define the provider-neutral speech recognition contract.
6. Define the semantic need-classification contract.
7. Define the Widget navigation contract: Listen, Needs, Profile/Settings.
8. Define the mobile-edge contract required to consume/create recognized-need notifications without bypassing existing mobile boundaries.

### Notifying

1. Keep Objecting as the canonical owner of adopted system field packs.
2. Eliminate duplicate local storage for fields already represented by Objecting.
3. Ensure `NotificationEntity.title()` is a business alias over Objecting `firstTitle` rather than a parallel `title` field.
4. Keep the recognized need as notification semantics (`topic = one_tasker.need`) rather than introducing a new backend aggregate in V1.
5. Preserve recipient inbox/read/ack/archive behavior as the durable need-list substrate.

## Proposed recognized-need contract

```text
sourceComponent = one_tasker_ai
eventName       = need.recognized
topic           = one_tasker.need
title           = concise summary
body            = bounded context
actionUrl       = Call me deep link/action
payload:
  type
  category
  confidence
  source
  locale
  classifierVersion
```

## Acceptance criteria

Milestone 0 is complete when:

1. architecture and privacy documents are committed in `Mobiling`;
2. the lightweight product has a declared build/composition boundary on Android and iOS;
3. no implementation plan requires copying the full OneTasker source tree;
4. speech-to-text and semantic classification are separate contracts;
5. V1 explicitly forbids durable raw-audio storage;
6. the privacy contract distinguishes on-device and cloud speech processing;
7. recognized needs map cleanly to `Notifying` without using `Delivering` or `Messaging` as persistence;
8. Notifying's Objecting dependency and consumer manifest are consistent with actual entity composition;
9. Doctrine Entity metadata expresses the canonical Objecting schema directly; migrations only converge deployed databases toward that Entity design;
10. duplicate notification title storage and legacy `object_*` system columns are removed with data-preserving, fail-fast schema convergence;
11. an executable schema-contract test prevents the Entity/Doctrine design from drifting back to duplicate or legacy system columns;
12. full OneTasker and SmartResponsor behavior remains unchanged by this documentation/convergence milestone.

## Milestone 1 implementation status

The executable vertical slice is now materially present through the classifier boundary:

```text
Authenticate (restore + minimal sign-in + logout)  materialized
  -> push-to-talk                                materialized
  -> native/system speech-to-text                materialized
  -> classify/summarize                          typed boundary materialized; product provider pending
  -> structured recognized-need submission       materialized
  -> Notifying inbox-only persistence             materialized
  -> Needs screen + structured actionable cards   materialized
  -> Call me action                               materialized
  -> correlated need.callback_requested event    materialized
  -> human qualification                          next operational integration
  -> existing Ordering lifecycle                  unchanged boundary
```

Android and iOS use independent Widget application composition roots. Both reuse only the narrow authentication/session and notification data surfaces needed by this flow.

The lightweight product shell is now materialized on both platforms as explicit `Listen`, `Needs`, and `Profile` states without importing the full-app navigation graph. `Listen` owns push-to-talk capture, `Needs` renders only canonical recognized needs and the `Call me` action, and `Profile` exposes account state plus speech/classification privacy disclosures. Privacy Policy, Terms of Service, About, Help/Support, and similar informational surfaces remain web-owned; the Widget opens configured external links instead of duplicating those pages natively.

Recognized-need persistence accepts structured fields only (`summary`, `category`, `confidence`, `source`, `locale`, `classifierVersion`, and optional `correlationId`). Raw audio and unrestricted transcript fields are not part of the persistence contract.

`Call me` is not modeled as a notification acknowledgement alone. It creates an idempotent operational notification event correlated to the originating recognized need:

```text
eventName    = need.callback_requested
topic        = one_tasker.need.callback
recipient    = system:one_tasker.operations
correlation  = need-callback:<recognized-need-id>
delivery     = inbox_only
```

The originating customer need is acknowledged only after the operational callback intent has been created.

The remaining external dependency is the actual product-inference provider. The provider-neutral `NeedClassifier` contract now has Android and iOS HTTP implementations bound to the stable mobile-edge `POST /need/classify` boundary. `mobile-edge` forwards only a bounded transcript fragment plus optional locale to a separately configured classifier upstream and returns explicit `503 need_classifier_unavailable` when no product inference backend is configured. The current local `aigateway` repository remains an operator/Codex control plane and is not used as a fake product runtime.

The native Widget flow is now wired as `push-to-talk -> native/system STT -> NeedClassifier -> if isNeed -> RecognizedNeedRequest -> POST /need/recognized -> refresh Needs`. A classifier result with `isNeed == false` does not create a recognized need. Raw audio and unrestricted transcripts remain outside persistence contracts and UI state.

Verification note: the mobile-edge contract currently passes TypeScript typecheck, build, and the mobile contract guard. Notifying remains green at 15 tests / 159 assertions and the guarded Doctrine dry-run remains 1 migration / 57 SQL queries. Native Android/iOS compilation is still not claimed: the Console MCP server reports Gradle/Xcode tools in its canonical registry, but the current ChatGPT connector schema has not exposed those callable functions after refresh.
