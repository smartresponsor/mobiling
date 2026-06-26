# Mobile Edge 0.4.16 Access Identity Vendor Principal Milestone

- Purpose: align mobile access identity with platform semantics.
- `vendorId` is the canonical identity principal for mobile access.
- Removed `userId` from the mobile access identity contract.
- Mobile-edge emits `vendorId` only; when Accessing exposes a root user identifier, the adapter maps that source value into `vendorId` without exposing `userId` in the mobile contract.
- `accountId` remains nullable compatibility context.
- OpenAPI and source JSON schema now require `vendorId`.
- Not included: vendor lookup, authorization policy, token generation, or Accessing behavior changes.