import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for auth entry flow.
public struct AuthFeatureBridge: Sendable {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func start(request: StartAuthRequest) async throws -> AuthSessionPayload {
        try await StartAuthUseCase(gateway: gateway)(request: request)
    }

    public func register(request: RegisterAuthRequest) async throws -> AuthSessionPayload {
        try await RegisterAuthUseCase(gateway: gateway)(request: request)
    }

    public func restore() async throws -> AuthSessionPayload {
        try await RestoreAuthUseCase(gateway: gateway)()
    }

    public func logout() async throws {
        try await LogoutAuthUseCase(gateway: gateway)()
    }

    public func resendVerification() async throws -> AuthSessionPayload {
        try await ResendVerificationUseCase(gateway: gateway)()
    }

    public func confirmVerification(request: ConfirmVerificationRequest) async throws -> AuthSessionPayload {
        try await ConfirmVerificationUseCase(gateway: gateway)(request: request)
    }

    public func challengeSecondFactor() async throws -> AuthSessionPayload {
        try await ChallengeSecondFactorUseCase(gateway: gateway)()
    }

    public func verifySecondFactor(request: VerifySecondFactorRequest) async throws -> AuthSessionPayload {
        try await VerifySecondFactorUseCase(gateway: gateway)(request: request)
    }
}