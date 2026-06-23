public struct StartAuthUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: StartAuthRequest) async throws -> AuthSessionPayload {
        try await gateway.startAuth(request: request)
    }
}
