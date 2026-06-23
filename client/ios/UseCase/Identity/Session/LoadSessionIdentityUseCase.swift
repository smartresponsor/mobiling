public struct LoadSessionIdentityUseCase {
    private let gateway: SessionIdentityGateway

    public init(gateway: SessionIdentityGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws -> SessionIdentityPayload {
        try await gateway.loadSessionIdentity()
    }
}
