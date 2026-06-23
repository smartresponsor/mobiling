public struct ConfirmVerificationUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: ConfirmVerificationRequest) async throws -> AuthSessionPayload {
        try await gateway.confirmVerification(request: request)
    }
}