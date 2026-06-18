public protocol AuthSessionGateway {
    func startAuth(request: StartAuthRequest) async throws -> AuthSessionPayload
}
