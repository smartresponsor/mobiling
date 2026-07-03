# Mobile Client Tree Naming Convention

Date: 2026-07-03

Owner workspace: `D:\PhpstormProjects\www\Mobiling`

## Purpose

This release-candidate convention records the canonical mobile client tree and naming rules before any broad rename or filesystem normalization pass.

The goal is to keep Android, iOS, and mobile-edge capability slices comparable across neighboring features such as Attachment, Cart, Catalog, Message, Order, Product, Profile, Project, Shipment, and Taxation.

## Canonical client root

Canonical active root:

```text
client/
```

Canonical platform roots:

```text
client/android
client/ios
client/contract
```

Legacy or stale root:

```text
mobile-client/
```

The `mobile-client/` tree is not a canonical implementation source. It may exist as a local filesystem residue or historical reference after the `client/` rename milestone. New code, audits, and release evidence must use `client/` as the source of truth.

## Android canonical layers

Android runtime UI and shell code live under the Android app package tree:

```text
client/android/app/src/main/java/app/mobiling/client/<feature>
```

Android feature boundary layers are represented by package names:

```text
app.mobiling.client.contract.<feature>
app.mobiling.client.data.<feature>
app.mobiling.client.usecase.<feature>
```

The long-term filesystem target should match package casing and use lowercase directory names:

```text
client/android/contract/<feature>
client/android/data/<feature>
client/android/usecase/<feature>
```

Current RC code may still contain PascalCase filesystem directories such as `Contract`, `Data`, and `UseCase`. Those directories are accepted only as transitional filesystem shape. Any future broad rename must be isolated into a dedicated Android tree casing slice and validated with Android compile.

## iOS canonical layers

iOS code uses explicit layer directories:

```text
client/ios/App/<Feature>
client/ios/Contract/<Feature>
client/ios/Data/<Feature>
client/ios/UseCase/<Feature>
```

iOS keeps Swift type names in UpperCamelCase. For protocol existentials, new code should prefer explicit `any` style when the project language level supports it:

```swift
private let reader: any AttachmentReader
private let writer: any AttachmentWriter
```

## Feature tree depth

Simple feature surfaces should stay flat until a capability split is materially useful:

```text
contract/attachment
data/attachment
usecase/attachment
```

Tree-shaped features may use a second-level capability folder:

```text
contract/catalog/browse
contract/catalog/detail
data/catalog/browse
data/catalog/detail
usecase/catalog/browse
usecase/catalog/detail
```

The second-level capability folder should represent a stable product capability, not a temporary method name.

## Bridge naming

Every mobile feature should expose one app-level bridge:

```text
<Feature>FeatureBridge
```

Examples:

```text
AttachmentFeatureBridge
CartFeatureBridge
CatalogFeatureBridge
MessageFeatureBridge
OrderFeatureBridge
```

The bridge is the app/shell entry point and delegates to data/usecase layers. It should not own backend-specific serialization when a gateway already owns that concern.

## UseCase naming

Preferred cross-platform naming order for new use cases:

```text
<Verb><Feature><Object>UseCase
```

Examples:

```text
LoadAttachmentListUseCase
AttachAttachmentUseCase
PrepareAttachmentFileHandoffUseCase
