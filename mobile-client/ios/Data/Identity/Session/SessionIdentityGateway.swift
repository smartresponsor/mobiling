public protocol SessionIdentityGateway {
    func loadSessionIdentity() async throws -> SessionIdentityPayload
}
