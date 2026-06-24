public struct ResetRecoveryUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: ResetRecoveryRequest) async throws -> AuthSessionPayload {
        try await gateway.resetRecovery(request: request)
    }
}
