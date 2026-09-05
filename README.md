# SmartResponsor Mobiling

Canonical mobile workspace for Android, iOS, cross-platform contracts, and Mobile Edge.

## Active roots

- `client/android` — Android application and canonical client layers.
- `client/ios` — iOS application, `MobileClient` framework, and mirrored core frameworks.
- `client/contract` — cross-platform contracts and ownership metadata.
- `mobile-edge` — Fastify mobile boundary and runtime services.

## Ownership

`Dashboard` is the only application root. `Auth` is an entry flow. Persistent sections are `Catalog`, `Message`, and `Vendor`. `Vendor` owns `Product`, `Order`, `Project`, and `Profile`. `Order` owns `Shipment` and embeds `Taxation`. `Identity` is internal.

Machine-readable ownership is stored in `client/contract/navigation/ownership.json`.

This baseline materializes build and runtime structure only. Product visual direction remains intentionally undecided.

The preparatory product-configuration foundation and preserved defaults are documented in `docs/product-configuration.md`.

## Local Android runtime

The local runtime chain is intentionally split across the emulator and the Windows host:

- Android emulator -> `http://10.0.2.2:8080` (Mobile Edge on the host).
- Mobile Edge -> `http://127.0.0.1:8000` (local Symfony Host App).
- The Host App authentication probe is `GET /api/access/session`.

`client/android/app/src/main/java/app/mobiling/client/MobileClientRuntimeConfig.kt` contains the emulator-visible Mobile Edge URL. Do not replace it with `127.0.0.1`; inside the Android emulator that address points back to the emulator itself.

Before restarting Mobile Edge, start the Host App on `http://127.0.0.1:8000`. Then run:

```powershell
Set-Location mobile-edge
npm run dev:restart
```

The restart command builds Mobile Edge and performs a fail-fast preflight against `http://127.0.0.1:8000/api/access/session`. It stops when the backend is unavailable, returns `404`/`5xx`, or redirects HTTP to HTTPS.

For an end-to-end authentication check, run from the repository root:

```powershell
.\tool\mobile-live-auth-smoke.ps1
```

The default smoke account is `demo@smartresponsor.local` with password `AccessingDemo123!`.
