public protocol AuthSessionGateway {
    func startAuth(request: StartAuthRequest) async throws -> AuthSessionPayload
    func registerAuth(request: RegisterAuthRequest) async throws -> AuthSessionPayload
    func restoreAuth() async throws -> AuthSessionPayload
    func logoutAuth() async throws
    func resendVerification() async throws -> AuthSessionPayload
    func confirmVerification(request: ConfirmVerificationRequest) async throws -> AuthSessionPayload
    func challengeSecondFactor() async throws -> AuthSessionPayload
    func verifySecondFactor(request: VerifySecondFactorRequest) async throws -> AuthSessionPayload
}