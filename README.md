# Mobile 001 Initial Curated Slice

This workspace is the first curated repository slice reconstructed from the uploaded Mobile archive set.

It intentionally excludes archive warehouses, release bundles, dated snapshot folders, IDE/temp folders,
embedded private signing material, and shadow duplicate roots.

Canonical working roots in this slice:
- `mobile-client/`
- `mobile-edge/`

Main donor tree: `Mobile/mobile-src`

Important constraints:
- donor Git history was not preserved as canonical product history;
- `mobile/mobile/edge` was excluded as a shadow duplicate root;
- `Mobile/signatures/mobile-key-signing-private.pem` was excluded and should be treated as compromised.
