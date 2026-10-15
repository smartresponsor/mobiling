# OneTasker Widget speech and privacy architecture

## Purpose

This document defines the V1 speech-to-need pipeline for OneTasker Widget. It deliberately separates speech recognition from semantic need recognition so that the mobile binary can remain small and privacy behavior remains explicit.

## Pipeline

```text
microphone
  -> transient audio buffer
  -> speech-to-text
  -> transcript fragment
  -> need classifier / semantic compressor
  -> structured need
  -> Notifying notification intent
```

Speech-to-text and need classification are separate capabilities and may use different providers or execution locations.

## Speech-to-text strategy

V1 must prefer native/system recognition instead of bundling a heavyweight speech model inside the application.

Resolution order:

```text
1. on-device system speech recognition, when available
2. system recognition with its documented platform behavior, when explicitly allowed
3. cloud speech fallback, only under the configured privacy/consent policy
```

The application-level contract should expose a provider-neutral `SpeechRecognition` capability. Platform implementations may select native on-device or cloud-backed recognition without changing the Need flow.

The V1 binary must not embed a large Whisper-class model merely to guarantee local transcription.

## Semantic recognition

After speech-to-text, a small semantic classifier or inexpensive LLM may evaluate only the relevant transcript fragment and return a narrow structured result such as:

```json
{
  "isNeed": true,
  "category": "plumbing",
  "summary": "Repair leaking kitchen faucet",
  "confidence": 0.94
}
```

The semantic model is not the speech recognizer. This separation keeps audio handling narrow, allows independent provider replacement, and reduces token/cost usage.

## No voice-record storage

Raw audio is transient processing material, not a durable business entity.

The architecture must not introduce a durable `SpeechRecord`, audio library, or voice-history aggregate for V1.

Required lifecycle:

```text
capture audio
  -> transcribe
  -> discard audio
  -> classify text
  -> retain only recognized need data required by product policy
```

If a cloud speech provider is used, the application/privacy policy must disclose that audio may leave the device for transcription. OneTasker itself should not retain the raw audio after processing.

## Transcript retention

The default product objective is data minimization. The durable artifact is the concise structured need and human-readable summary, not an unrestricted conversation transcript.

If a short source fragment is retained for explainability or correction, it must be intentionally bounded and treated as user content. Unrelated surrounding speech must be discarded.

## On-device versus cloud behavior

### On-device path

```text
microphone
  -> OS speech engine on device
  -> text
  -> local or cloud semantic classifier
```

If semantic classification is also local, no speech content needs to leave the device until a product action requires backend persistence.

If semantic classification is cloud-based, only the minimum transcript fragment should be sent.

### Cloud speech fallback

```text
microphone
  -> encrypted transport
  -> configured speech provider
  -> transcript
  -> semantic classifier
```

Cloud speech must be separately visible in privacy disclosures because voice audio leaves the device during processing.

## Privacy-policy requirements

The public privacy policy and in-product permission text must distinguish these facts clearly:

- microphone access is used to recognize possible home-service needs;
- on-device speech recognition is preferred where supported;
- some devices/configurations may use a remote speech-processing provider;
- raw voice recordings are not retained by OneTasker after processing;
- recognized text may be processed by an AI service to classify and summarize a possible service need;
- unrelated speech is not intended to be retained;
- recognized needs may be stored as notification/inbox items associated with the authenticated customer;
- operational actions such as `Call me` may send the structured need into the OneTasker service workflow.

The product must never claim that voice "never leaves the device" unless the active runtime configuration can guarantee that statement.

## Security and observability

Logs, crash reports, traces, and analytics must not capture raw audio. Full unrestricted transcripts must not be emitted to telemetry.

Provider requests should use correlation identifiers and technical metadata rather than user speech content in logs.

## Background listening

Continuous or ambient listening is outside V1. The first implementation is push-to-talk / user-initiated capture.

Any later background-listening capability requires a separate milestone covering platform constraints, visible user state, consent, battery impact, retention, revocation, and store-review compatibility.

## Acceptance invariants

The V1 speech implementation is acceptable only when all of the following are demonstrably true:

1. the mobile app does not bundle a heavyweight speech model;
2. raw voice audio is not durably stored by OneTasker;
3. unrelated speech is discarded;
4. speech-to-text and semantic classification are replaceable boundaries;
