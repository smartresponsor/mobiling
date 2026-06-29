# Attachment Flow RC Evidence

Date: 2026-06-29

Owner workspace: `D:\PhpstormProjects\www\Mobiling`

Related backend workspace: `D:\PhpstormProjects\www\Attaching`

## Scope

This evidence memo records the current release-candidate status for the mobile Attachment flow:

```text
Attaching backend -> mobile-edge -> Android -> iOS
```

The target surface is the mobile attachment bridge for vendor-owned attachments, using the canonical mobile route names and the existing Mobile Dashboard shell.

## Canonical decisions

- Attaching remains independent from Administering and AdministrationConfigTool.
- Mobile Edge owns the mobile HTTP boundary.
- Android and iOS expose Attachment as a vendor context surface.
- No fake upload action is exposed in the mobile clients.
- No App-side direct Twig, Response, or frontend rendering is introduced by this flow.

## Committed slices

### Attaching backend

Repository:

```text
D:\PhpstormProjects\www\Attaching
```

Commit:

```text
ab52be5 Clean Attaching backend integration surface
```

Evidence:

```text
composer validate: OK
PHPUnit: OK (20 tests, 191 assertions)
```

Confirmed backend properties:

- No Attaching -> Administering dependency.
- No AdministrationConfigTool coupling.
- Download controller uses constructor dependency injection.
- Attachment voter and architecture tests are present.
- Primary attachment slot resolution is covered by tests.

### mobile-edge contract and runtime surface

Repository:

```text
D:\PhpstormProjects\www\Mobiling\mobile-edge
```

Commit:

```text
bb97b9d Add mobile attaching contract surface
```

Routes:

```text
GET /attachment
POST /attachment/link
```

Upstream mapping:

```text
GET /attachment -> GET /attachments
POST /attachment/link -> POST /attachments/attach
```

Evidence:

```text
npm run typecheck: OK
npm run build: OK
npm run test: OK
Mobile contract guard passed: 21 routes, 10 mobile route files.
```

Local live-smoke evidence after restart:

```text
npm run dev:restart: OK
mobile-edge restarted on http://127.0.0.1:8080
npm run smoke: OK
Mobile live smoke passed against http://127.0.0.1:8080 using vendor vendor-demo-001.
```

Observed stale-server condition and resolution:

```text
Before restart, the old mobile-edge process returned 404 for GET /attachment.
After npm run dev:restart, /attachment was present and live smoke passed.
```

### Android client surface

Repository:

```text
D:\PhpstormProjects\www\Mobiling
```
