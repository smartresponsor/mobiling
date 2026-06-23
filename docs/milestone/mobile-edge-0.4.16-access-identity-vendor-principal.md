# Mobile Edge 0.4.16 Access Identity Vendor Principal Milestone

- Purpose: align mobile access identity with platform semantics.
- `vendorId` is the canonical identity principal for mobile access.
- Removed `userId` from the mobile access identity contract.
- Mobile-edge normalizes `identity.vendorId` from Accessing and does not derive or mint it.
- `accountId` remains nullable compatibility context.
- OpenAPI and source JSON schema now require `vendorId`.
- Not included: vendor lookup, authorization policy, token generation, or Accessing behavior changes.