public struct LogoutAuthUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws {
        try await gateway.logoutAuth()
    }
}