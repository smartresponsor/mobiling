# Attaching Mobile Availability ERC

Date: 2026-07-03

Owner workspace: `D:\PhpstormProjects\www\Mobiling`

Related component workspace: `D:\PhpstormProjects\www\Attaching`

## Purpose

This evidence release candidate materializes the mobile availability milestone for the Attaching application capability.

The mobile responsibility is not to duplicate Attaching business logic. Mobiling owns the mobile transport, native screen composition, route resolution, and mobile-edge normalization layer.

## Boundary chain

```text
Navigating
  -> mobile shell menu item route: attachment
Mobiling Android / iOS
  -> native route: attachment
Mobiling mobile-edge
  -> GET /attachment
  -> POST /attachment/link
Attaching Symfony component
  -> GET /attachment
  -> POST /attachment/attach
```

## Current RC evidence

Latest commits in this slice:

```text
c095f6c Add attachment handoff transport
83f8a96 Document attachment handoff route
149580c Add attachment upload handoff
22571ad Document attachment upload handoff
54a3258 Add native attachment handoff actions
a4b3bda Fix catalog mobile screen syntax
```

Current mobile-edge gates:

```text
npm run typecheck: PASS
npm run test: PASS
Mobile contract guard passed: 30 routes, 13 mobile route files.
```

Native Android smoke:

```text
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace: PASS
```

Native iOS caveat:

```text
iOS compile requires macOS, Xcode, and XcodeGen. It is not executable from the current Windows console path.
```

## Current materialized surfaces

### mobile-edge

Current mobile routes:

```text
GET  /attachment
POST /attachment/link
POST /attachment/detach
GET  /attachment/file/:attachmentId
POST /attachment/upload-handoff
```

Current upstream mapping:

```text
GET  /attachment                    -> GET  /attachment
POST /attachment/link               -> POST /attachment/attach
POST /attachment/detach             -> POST /attachment/detach
GET  /attachment/file/:attachmentId -> GET  /attachment/{attachmentId}/download
POST /attachment/upload-handoff     -> POST /attachment/upload
```

The mobile-edge layer normalizes Attaching payloads into mobile payloads:

```text
attachmentId = attachmentId || id
linkId       = linkId || id
ownerType    = ownerType || unknown
ownerId      = ownerId || unknown
```

### Android

Materialized Android surfaces:

```text
client/android/app/src/main/java/app/mobiling/client/attachment/AttachmentFeatureBridge.kt
client/android/app/src/main/java/app/mobiling/client/attachment/AttachmentMobileScreen.kt
client/android/app/src/main/java/app/mobiling/client/dashboard/DashboardMobileShell.kt
client/android/app/src/main/java/app/mobiling/client/navigation/MobileRoute.kt
client/android/app/src/main/java/app/mobiling/client/navigation/MobileRouteResolver.kt
client/android/Data/Attachment/AttachmentHttpGateway.kt
client/android/Contract/Attachment/*.kt
client/android/UseCase/Attachment/*.kt
```

Android availability:

* `attachment` is a known route.
* `attachment` is currently renderable.
* dashboard shell renders `AttachmentMobileScreen` for the `attachment` route.
* screen loads vendor-owned attachment through `AttachmentFeatureBridge`.
* screen exposes safe `Prepare Upload` handoff action.
* screen exposes safe `Prepare File` handoff action.
* Android compile smoke is green for `:app:assembleDebug`.

### iOS

Materialized iOS surfaces:

```text
client/ios/App/Attachment/MobileAttachmentView.swift
client/ios/App/Dashboard/MobileDashboardShellView.swift
client/ios/App/Navigation/MobileRoute.swift
client/ios/App/Navigation/MobileRouteResolver.swift
client/ios/Data/Attachment/HttpAttachmentGateway.swift
client/ios/Contract/Attachment/*.swift
```

iOS availability:

* `attachment` is a known route.
* `attachment` is currently renderable.
* dashboard shell renders `MobileAttachmentView` for the `attachment` route.
* view loads vendor-owned attachment through `AttachmentFeatureBridge`.
* view exposes safe `Prepare Upload` handoff action.
* view exposes safe `Prepare File` handoff action.
* iOS compile remains a macOS/Xcode validation step outside the current Windows console path.

## Navigating menu availability

Current mobile menu locations that can expose Attaching:

| Location | Label | Route | Status |
| --- | --- | --- | --- |
| account quick | My Attachment | `attachment` | active |
| more drawer active | Attachment | `attachment` | active |
| vendor context | My Attachment | `attachment` | active |
| more drawer coming soon | Attachment | `attachment` | hidden/disabled fallback |

Current web menu candidates remain Navigating-owned:

| Menu | Path | Owner |
| --- | --- | --- |
