# Client 0.2.17 Recovery Reset UI Wiring

- Purpose: wire recovery request and recovery reset UI into the access entry flow.
- Android adds recovery request and reset screens.
- Android sign-in routes secondary access action to recovery.
- Android app shell routes recovery request/reset payloads through AuthFeatureBridge.
- iOS adds recovery request and reset views.
- iOS sign-in routes secondary access action to recovery.
- iOS app shell routes recovery request/reset payloads through AuthFeatureBridge.
- Recovery request/reset returns AuthSessionPayload and reuses access session routing.
- Not included: recovery delivery, code validation, password policy, or Accessing business rules.