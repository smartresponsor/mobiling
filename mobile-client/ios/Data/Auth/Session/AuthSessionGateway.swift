public protocol AuthSessionGateway {
    func startAuth(request: StartAuthRequest) async throws -> AuthSessionPayload
    func registerAuth(request: RegisterAuthRequest) async throws -> AuthSessionPayload
}
