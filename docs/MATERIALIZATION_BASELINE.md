# Repository materialization baseline

This baseline converts the curated donor slice into explicit build and runtime surfaces.

## Materialized state

- Android layer folders participate in one Gradle graph.
- Kotlin imports use canonical type casing.
- iOS exposes a `MobileClient` framework boundary, mirrored core frameworks, Swift Package metadata, and an XcodeGen application definition.
- Mobile Edge registers implemented public, system, and protected admin routes from one entry point.
- Mobile Edge uses one complete process-local storage contract for the baseline; durable storage selection is deferred until runtime requirements are fixed.
- Ownership is machine-readable in `mobile-client/contract/navigation/ownership.json`.

## Deliberately deferred

No final visual language, component system, screen sequence, database engine, or deployment topology is selected by this wave. Android and iOS show only an ownership/materialization diagnostic surface, not product UI.
