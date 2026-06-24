public struct RequestRecoveryUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: RequestRecoveryRequest) async throws -> AuthSessionPayload {
        try await gateway.requestRecovery(request: request)
    }
}
