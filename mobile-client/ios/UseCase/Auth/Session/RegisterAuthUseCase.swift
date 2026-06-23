public struct RegisterAuthUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: RegisterAuthRequest) async throws -> AuthSessionPayload {
        try await gateway.registerAuth(request: request)
    }
}