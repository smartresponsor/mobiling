# Client/Edge 0.2.13 Second-Factor Proxy Contour Milestone

- Purpose: add second-factor client contour without moving auth logic into Mobiling.
- Android adds `VerifySecondFactorRequest`.
- Android adds `ChallengeSecondFactorUseCase`.
- Android adds `VerifySecondFactorUseCase`.
- Android gateway and bridge expose `challengeSecondFactor` and `verifySecondFactor`.
- iOS adds `VerifySecondFactorRequest`.
- iOS adds `ChallengeSecondFactorUseCase`.
- iOS adds `VerifySecondFactorUseCase`.
- iOS gateway and bridge expose `challengeSecondFactor` and `verifySecondFactor`.
- Not included: second-factor code generation, delivery, validation, recovery/reset, Accessing business rules, or mobile-edge route expansion.