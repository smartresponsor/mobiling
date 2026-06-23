# Mobile Client 0.2.7 Access Identity Vendor Principal Parity Milestone

- Purpose: align Android and iOS session identity contracts with mobile-edge access identity semantics.
- `vendorId` is now the client access identity principal.
- Removed `userId` from Android session identity payload.
- Removed `userId` from iOS session identity payload.
- Not included: UI changes, network path changes, register routing, session restore, or logout wiring.