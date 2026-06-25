public protocol AuthRecoverySessionGateway: AuthSessionGateway {
    func requestRecovery(request: RequestRecoveryRequest) async throws -> AuthSessionPayload
    func resetRecovery(request: ResetRecoveryRequest) async throws -> AuthSessionPayload
}

public extension AuthSessionGateway {
    func requestRecovery(request: RequestRecoveryRequest) async throws -> AuthSessionPayload {
        guard let gateway = self as? AuthRecoverySessionGateway else {
            throw AuthRecoverySessionGatewayError.unsupportedGateway
        }

        return try await gateway.requestRecovery(request: request)
    }

    func resetRecovery(request: ResetRecoveryRequest) async throws -> AuthSessionPayload {
        guard let gateway = self as? AuthRecoverySessionGateway else {
            throw AuthRecoverySessionGatewayError.unsupportedGateway
        }

        return try await gateway.resetRecovery(request: request)
    }
}

public enum AuthRecoverySessionGatewayError: Error { case unsupportedGateway }
