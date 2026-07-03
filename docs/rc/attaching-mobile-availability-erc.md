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
  -> GET /attachments
  -> POST /attachments/attach
```

## Current materialized surfaces

### mobile-edge

Current mobile routes:

```text
GET /attachment
POST /attachment/link
```

Current upstream mapping:

```text
GET /attachment      -> GET /attachments
POST /attachment/link -> POST /attachments/attach
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
* screen loads vendor-owned attachments through `AttachmentFeatureBridge`.

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
* view loads vendor-owned attachments through `AttachmentFeatureBridge`.

## Navigating menu availability

Current mobile menu locations that can expose Attaching:

| Location | Label | Route | Status |
| --- | --- | --- | --- |
| account quick | My Attachments | `attachment` | active |
| more drawer active | Attachments | `attachment` | active |
| vendor context | My Attachments | `attachment` | active |
| more drawer coming soon | Attachments | `attachment` | hidden/disabled fallback |

Current web menu candidates remain Navigating-owned:

| Menu | Path | Owner |
| --- | --- | --- |
