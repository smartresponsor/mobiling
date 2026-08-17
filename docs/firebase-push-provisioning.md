# Firebase push provisioning

Mobiling uses Firebase only for Android push transport through Firebase Cloud Messaging (FCM). Application data, authentication, notification policy, and delivery state remain owned by SmartResponsor components.

## Firebase project topology

Use one Firebase project for the current platform and register both Android applications in that project:

| Mobile product | Android package | SmartResponsor appKey |
| --- | --- | --- |
| OneTasker | `app.mobiling.client.onetasker` | `one_tasker` |
| SmartResponsor | `app.mobiling.client.smartresponsor` | `platform` |

This matches the current Delivering model, which uses one FCM service-account credential and maps application keys to Firebase project IDs.

## Android configuration files

Download the Firebase Android configuration for each registered application and place it in its matching product-flavor source set:

```text
client/android/app/src/oneTasker/google-services.json
client/android/app/src/smartResponsor/google-services.json
```

The Google Services Gradle plugin remains disabled until both flavor files are present. A root `client/android/app/google-services.json` is also supported when it contains matching client entries for both package names.

`google-services.json` contains Firebase project/application identifiers. It is not the server credential used by Delivering. Mobiling keeps these files out of Git (`**/google-services.json`) so Firebase environment wiring remains deployment/local configuration rather than repository state.

## Delivering server credential

The Host supplies the FCM HTTP v1 credential to Delivering through production secrets:

```text
DELIVERING_FCM_SERVICE_ACCOUNT_JSON=<complete service-account JSON>
DELIVERING_FCM_PROJECT_MAP={"one_tasker":"<firebase-project-id>","platform":"<firebase-project-id>"}
```

When both Android applications belong to one Firebase project, both map entries use the same project ID.

Never put the service-account private key in the mobile repository or in `google-services.json`.

## Runtime chain

```text
FirebaseMessagingService
→ AndroidPushTokenStore
→ AndroidPushTokenEvents
→ authenticated notification subscription sync
→ Notifying
→ Delivering
→ FCM HTTP v1
→ Android device
```

Before enabling Firebase on a workstation or build host, verify both flavor configuration files exist, that each contains its expected Android package name, and that both report the same Firebase `project_id`. Do not print or log service-account credentials during this check.
