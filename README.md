# SmartResponsor Mobiling

Canonical mobile workspace for Android, iOS, cross-platform contracts, and Mobile Edge.

## Active roots

- `mobile-client/android` — Android application and canonical client layers.
- `mobile-client/ios` — iOS application, `MobileClient` framework, and mirrored core frameworks.
- `mobile-client/contract` — cross-platform contracts and ownership metadata.
- `mobile-edge` — Fastify mobile boundary and runtime services.

## Ownership

`Dashboard` is the only application root. `Auth` is an entry flow. Persistent sections are `Catalog`, `Message`, and `Vendor`. `Vendor` owns `Product`, `Order`, `Project`, and `Profile`. `Order` owns `Shipment` and embeds `Taxation`. `Identity` is internal.

Machine-readable ownership is stored in `mobile-client/contract/navigation/ownership.json`.

This baseline materializes build and runtime structure only. Product visual direction remains intentionally undecided.
